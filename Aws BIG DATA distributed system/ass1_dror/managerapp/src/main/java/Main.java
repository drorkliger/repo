import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import com.amazonaws.util.EC2MetadataUtils;

import static java.lang.System.exit;

public class Main {

    private static String locals_managerSQS;
    private static String workers_To_managerSQS;
    private static String manager_To_workersSQS;
    private static String managerBucket;
    private static boolean terminateMsg;
    private static AtomicBoolean isTerminate;
    private static AtomicBoolean everyWorkerTerminated;


    private static ConcurrentHashMap <String,InputFileInfo> inputNameToInputInfo;

    private static List <String> workersInstancesID; //all workers IDS
    private static List <String> theTerminatorURL;


    private static int n;
    private static AtomicInteger runningWorkers;
    private static AtomicInteger inputReceived;
    private static AtomicInteger inputhandeled;


    private static AmazonSQS sqsClient;
    private static AmazonS3 s3Client;
    private static AmazonEC2 ec2Client;



    public static void main(String[] args) {
        //___________assuming args are locals-sqs, n, managerBucket

        locals_managerSQS=args[0];
        //__________convert n from string to int_______
        try {
            n = Integer.parseInt(args[1]);
        }catch(NumberFormatException e)
        {
            e.printStackTrace();
            exit(1);
        }

        managerBucket = args[2];

        //____________initiating the variables__________________
        isTerminate=new AtomicBoolean(false);

        //__________EC2CLIENT to build workers instances_______
        ec2Client = AmazonEC2ClientBuilder.standard().withRegion("us-east-1").build();

        //___________creating s3 CLIENT___________
        s3Client= AmazonS3ClientBuilder.standard()
                .withRegion("us-east-1")
                .build();

        //______________SQSClient building__________________
        sqsClient= AmazonSQSClientBuilder.standard()
                .withRegion("us-east-1")
                .build();


        //_______________creating sqs queue for workers->manager communication____________
        workers_To_managerSQS = createQueue("workers_manager_queue","90");

        //workers_To_managerSQS="workers_manager_queueea643c8b-5d27-4212-bc4f-4415f4a53af6";
        System.out.println("the workers to manager is: "+workers_To_managerSQS);
        //_______________creating sqs queue for manager->workers communication____________
        manager_To_workersSQS = createQueue("manager_workers_queue","90");
        //manager_To_workersSQS= "manager_workers_queue1ebefacd-ea83-48cc-b96b-545ef2cc2481";

        //_____________initiating variables________________
        runningWorkers = new AtomicInteger(0);
        inputhandeled = new AtomicInteger(0);
        inputReceived = new AtomicInteger(0);
        inputNameToInputInfo= new ConcurrentHashMap<>();
        theTerminatorURL = new LinkedList<>();
        everyWorkerTerminated=new AtomicBoolean(false);
        inputNameToInputInfo= new ConcurrentHashMap<>();


        LocalsManager localsManager = new LocalsManager(managerBucket,locals_managerSQS,workers_To_managerSQS,isTerminate,
                inputNameToInputInfo, sqsClient, s3Client, manager_To_workersSQS, inputReceived , theTerminatorURL,inputhandeled,runningWorkers);
        WorkersManager workersManager = new WorkersManager(isTerminate, workers_To_managerSQS, sqsClient, s3Client, inputNameToInputInfo,
                managerBucket, inputhandeled, runningWorkers, everyWorkerTerminated);

        Thread LocalManagerThread=new Thread(localsManager);
        Thread WorkersManagerThread=new Thread(workersManager);
        LocalManagerThread.start();
        WorkersManagerThread.start();

        //________________starting the workers________________
        workersInstancesID=new LinkedList<>();
        String workerDataScript="#!/bin/bash\n" + "cd home/ec2-user/\n" +"wget https://drors-jars.s3.amazonaws.com/workerapp.jar\n"
                + "java -jar workerapp.jar "+workers_To_managerSQS+" "+ manager_To_workersSQS + " " + managerBucket +"\n";

        while(!isTerminate.get()){
            //____________check that we don't have too many workers or adding negative amount_________

            int shouldRunInAddition= 0;
            int massegesAmount= inputReceived.get() - inputhandeled.get();

            if(massegesAmount % n == 0)
                shouldRunInAddition = massegesAmount/n - runningWorkers.get();
            else
                shouldRunInAddition = massegesAmount/n - runningWorkers.get() +1;


            if (shouldRunInAddition + runningWorkers.get() >9) {
                shouldRunInAddition=9-runningWorkers.get();
            }
            if(shouldRunInAddition<0)
                shouldRunInAddition=0;




            for (int i=0;i<shouldRunInAddition;i++) {
                //_________configuring the instance_________
                //__________making a worker tag__________
                Tag workerTag=new Tag().withKey("worker'").withValue("worker"+ (runningWorkers.get()+1));
                TagSpecification workerTagSpec= new TagSpecification().withTags(workerTag).withResourceType("instance");

                RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
                runInstancesRequest.withImageId("ami-076515f20540e6e0b")
                        .withInstanceType(InstanceType.T2Small)
                        .withMinCount(1)
                        .withMaxCount(1)
                        .withTagSpecifications(workerTagSpec)
                        .withUserData(Base64.getEncoder().encodeToString(workerDataScript.getBytes()))
                        .withIamInstanceProfile(new IamInstanceProfileSpecification().withArn("arn:aws:iam::260587511112:instance-profile/managerworkerROLE2"));
                //_______starting the manager instance________
                RunInstancesResult result = ec2Client.runInstances(runInstancesRequest);
                workersInstancesID.add(result.getReservation().getInstances().get(0).getInstanceId());
                runningWorkers.incrementAndGet();
            }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

        }

        while (runningWorkers.get()!=0){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        everyWorkerTerminated.set(true);

            //_______________delete all workers instances_____________________
            ec2Client.terminateInstances(new TerminateInstancesRequest(workersInstancesID));

            List <String> sqsQueues= sqsClient.listQueues().getQueueUrls();
            for(String queue: sqsQueues)
            {
                if(!queue.contains(theTerminatorURL.get(0)))
                    sqsClient.deleteQueue(queue);
            }

            Map <String,MessageAttributeValue> messageAttributes= new HashMap<>();
            messageAttributes.put("managerID",new MessageAttributeValue().withDataType("String").withStringValue(EC2MetadataUtils.getInstanceId()));
            SendMessageRequest sendMessageReq = new SendMessageRequest().withQueueUrl(theTerminatorURL.get(0))
                    .withMessageBody("terminating")
                    .withMessageAttributes(messageAttributes);
            sqsClient.sendMessage(sendMessageReq);
    }


    public static String createQueue (String queueName, String visibilityInSeconds)
    {
        Map <String,String> queueAttributesMap= new HashMap<>();
        queueAttributesMap.put("VisibilityTimeout",visibilityInSeconds);
        CreateQueueRequest queueRequest=new CreateQueueRequest().withQueueName(queueName +(UUID.randomUUID().toString()));
        queueRequest.setAttributes(queueAttributesMap);
        CreateQueueResult queueResult =sqsClient.createQueue(queueRequest);
        return queueResult.getQueueUrl();
    }
}
