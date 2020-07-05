import java.io.*;


import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;


import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;


import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.amazonaws.services.ec2.model.*; //tags
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.tools.PDFText2HTML;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.exit;



public class Main {
    private static AmazonS3 s3Client;
    private static AmazonEC2 ec2Client;
    private static AmazonSQS sqsClient;

    private static String local_Manager_QueueURL;
    private static String Local_QueueURL;
    private static String managerBucketName;

    private static AtomicInteger sent;
    private static AtomicInteger received;
    private static IOListener ioListener;
    private static AtomicBoolean isDoneInput;
    private static String outcome;
    private static String convertedFileURL;
    private static boolean isTerminate;
    private static String managerID;
    private static String instanceDataScript;

    public static void main(String[] args) {
        //_____checking the args_______

        int argNum=args.length;
        String n;
        isTerminate=false;
        String inputFileName="", outputFileName="";
        isDoneInput=new AtomicBoolean(false);  //object

        if(argNum<3 | argNum>4) {
            System.out.println("missing or too much arguments");
            exit(1);

        } else if(argNum==4 && args[3].equals("terminate"))
            isTerminate=true;
        else {
            System.out.println("wrong input");
            exit(1);
        }

        inputFileName= args[0];
        outputFileName= args[1];
        n= args[2];

        //_______building SQS client
        sqsClient= AmazonSQSClientBuilder.standard().withRegion("us-east-1").build();

        //building EC2 client
        ec2Client = AmazonEC2ClientBuilder.standard().withRegion("us-east-1")
                .withCredentials(new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials()))
                .build();

        //___________creating s3 CLIENT___________
        s3Client= AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials()))
                .withRegion("us-east-1")
                .build();


        //__________making a manager tag__________
        Tag managerTag=new Tag().withKey("manager").withValue("onlyOneManager");
        TagSpecification managerTagSpec= new TagSpecification().withTags(managerTag).withResourceType("instance");

        //_________looking for the manager instance__________
        boolean isManagerRunning = false;

        DescribeInstancesResult response = ec2Client.describeInstances(new DescribeInstancesRequest());
        InstanceState managerInsState=new InstanceState();

        boolean isFinished=false;
        for(Reservation reservation : response.getReservations()) {
            for(Instance instance : reservation.getInstances()) {
                if(instance.getTags().contains(managerTag) && instance.getState().getName().equals("running")) {
                    isManagerRunning = true;
                    isFinished=true;
                    break;
                }
            }
            if(isFinished)
                break;
        }


        //_______manager doesn't exist or not in "running" state_______
       if(!isManagerRunning)
        {
            //___________creating bucket____________
             managerBucketName = "managerbucket"+(UUID.randomUUID().toString());
            Bucket b=s3Client.createBucket(managerBucketName);
             //managerBucketName="managerbucket68575cd7-82f2-4896-993f-82703f5eb36b";

            //_______________creating sqs queue for locals-to-manager communication____________
            local_Manager_QueueURL = createQueue("Locals_To_Manager_Queue","60"); //_____________
            //local_Manager_QueueURL="Locals_To_Manager_Queuec27b8655-8948-453c-b4d7-9249e200e636";

            instanceDataScript="#!/bin/bash\n" + "cd home/ec2-user/\n" +"wget https://drors-jars.s3.amazonaws.com/managerapp.jar\n"
                            + "java -jar managerapp.jar "+local_Manager_QueueURL+" "+ n + " " +managerBucketName +"\n";
            //_________configuring the instance_________
           RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
            runInstancesRequest.withImageId("ami-076515f20540e6e0b")
                    .withInstanceType(InstanceType.T2Small)
                    .withMinCount(1)
                    .withMaxCount(1)
                    .withTagSpecifications(managerTagSpec)
                    .withUserData(Base64.getEncoder().encodeToString(instanceDataScript.getBytes()))
                    .setIamInstanceProfile(new IamInstanceProfileSpecification().withArn("arn:aws:iam::260587511112:instance-profile/managerworkerROLE2"));
            //_______starting the manager instance________
            RunInstancesResult result = ec2Client.runInstances(runInstancesRequest);
            //this line will get the managerID for termination result.getReservation().getInstances().get(0).getInstanceId();
        }
        else //_________manager is already running____________
        {
            List<String> queuesList=sqsClient.listQueues().getQueueUrls();
            for (String queue: queuesList)
            {//____________look for the manager queue______________
                if(queue.contains("Locals_To_Manager_Queue")) {
                    local_Manager_QueueURL = queue;
                    break;
                }
            }

            List<Bucket> buckets = s3Client.listBuckets();
            for(Bucket bucket: buckets)
            {
                if (bucket.getName().contains("managerbucket")) {
                    managerBucketName = bucket.getName();
                    break;
                }
            }
        }


        //___________from now on a manager supposed to exist and run____________

        //_____________creating sqs queue for manager_to_this_local communication____________
        Local_QueueURL = createQueue("Manager_To_Local_Queue","60"); //___________60 secs visibility, only one thread gets msgs___________
        //for debug Local_QueueURL="Manager_To_Local_Queued8dc1d43-e41a-463b-8f51-8301838199bb";


        //_____________set the input file____________
        String inputFileKey= "Input_File_From_Local_"+inputFileName+ (UUID.randomUUID().toString());
        String inputFileRelativePath= FileSystems.getDefault().getPath(inputFileName).toAbsolutePath().toString().replace("\\", "\\\\");
        File inputFile= new File(inputFileRelativePath);

        //____________set the output file_____________
        String outputFileKey= "Output_File_From_Local_"+outputFileName+ (UUID.randomUUID().toString());



        //________________uploading the input file to the bucket in S3_________________
        PutObjectRequest putObjectRequest = new PutObjectRequest(managerBucketName,inputFileKey,inputFile);
        s3Client.putObject(putObjectRequest);


        //______________sending the relevant info to the manager________________________
        Map <String,MessageAttributeValue> messageAttributes= new HashMap<>();
        messageAttributes.put("inputFile",new MessageAttributeValue().withDataType("String").withStringValue(inputFileKey));
        messageAttributes.put("outputFile",new MessageAttributeValue().withDataType("String").withStringValue(outputFileKey));
        messageAttributes.put("outputQueueURL",new MessageAttributeValue().withDataType("String").withStringValue(Local_QueueURL));
        SendMessageRequest sendMessageReq = new SendMessageRequest().withQueueUrl(local_Manager_QueueURL)
                .withMessageBody("inputFile: "+ inputFileKey + " myQueueURL: "+Local_QueueURL)
                .withMessageAttributes(messageAttributes);
        sqsClient.sendMessage(sendMessageReq);

        //____________message sent, now waiting for the manager to finish processing the message_________
        //____________meanwhile, this local still has option to send another message__________

        sent =new AtomicInteger(1);
        received =new AtomicInteger(0);
        ioListener=new IOListener(local_Manager_QueueURL,managerBucketName,s3Client,isDoneInput,sqsClient,Local_QueueURL,outputFileKey,sent);
        Thread IOThread=new Thread(ioListener);
        IOThread.start();




            while (received.get() != sent.get() | !isDoneInput.get()) //_________the order is important because the local can send between "get"s__________
        {
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(Local_QueueURL);
            List<Message> messageList = sqsClient.receiveMessage(receiveMessageRequest.withMessageAttributeNames("All")).getMessages();
            if (!messageList.isEmpty())
            {
                //______________output is ready from manager, download the file and convert it to HTML_______________
                for (Message msg:messageList) {
                    if(!msg.getBody().equals("terminated")) { //_______not supposed to be sent here, just for safty__________
                        received.incrementAndGet();
                        Map <String, MessageAttributeValue> attributeValueMap=msg.getMessageAttributes();
                        inputFileName = attributeValueMap.get("inputFile").getStringValue();
                        outputFileKey = attributeValueMap.get("outputFile").getStringValue();

                        try {
                            makeHTMLSummaryFile(outputFileKey,inputFileName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        messageDelete(msg,Local_QueueURL);
                    }
                }
            }
            else { //________the manager hasn't finished yet, LET'S WAIT 2 sec_______________________
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


        if(isTerminate)
        {
            //______________sending termination message to the manager________________________
            Map <String,MessageAttributeValue> msgAttributes= new HashMap<>();
            msgAttributes.put("outputQueueURL",new MessageAttributeValue().withDataType("String").withStringValue(Local_QueueURL));
            SendMessageRequest sendMessageRequest = new SendMessageRequest().withQueueUrl(local_Manager_QueueURL)
                    .withMessageBody("terminate")
                    .withMessageAttributes(msgAttributes);
            sqsClient.sendMessage(sendMessageRequest);

            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(Local_QueueURL);
            List <Message> localsIncomingMessages = sqsClient.receiveMessage(receiveMessageRequest.withMessageAttributeNames("All")).getMessages();

            while (localsIncomingMessages.isEmpty() || !localsIncomingMessages.get(0).getBody().equals("terminating")){
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                receiveMessageRequest = new ReceiveMessageRequest(Local_QueueURL);
                localsIncomingMessages = sqsClient.receiveMessage(receiveMessageRequest.withMessageAttributeNames("All")).getMessages();
            }


            //__________________deleting the manager instance______________________
            managerID = localsIncomingMessages.get(0).getMessageAttributes().get("managerID").getStringValue();
            List <String> list=new LinkedList<>();
            list.add(managerID);
            ec2Client.terminateInstances(new TerminateInstancesRequest(list));


            //_______________deleting all the buckets except the jars one____________________
            List<Bucket> buckets = s3Client.listBuckets();
            for(Bucket b:buckets) {
                if (!b.getName().contains("jar"))
                {
                    ObjectListing object_listing = s3Client.listObjects(b.getName());
                    Iterator<S3ObjectSummary> objIter = object_listing.getObjectSummaries().iterator();
                    while (objIter.hasNext()) {
                        s3Client.deleteObject(b.getName(), objIter.next().getKey());
                    }
                    s3Client.deleteBucket(b.getName());
                }
            }
            sqsClient.deleteQueue(Local_QueueURL);
        }



    }

    public static String createQueue (String queueName, String visibilityInSeconds)
    {
        Map <String,String> queueAttributesMap= new HashMap<>();
        queueAttributesMap.put("VisibilityTimeout",visibilityInSeconds); //TODO CHECK MAYBE CHANGE THE VISIBILITY
        CreateQueueRequest queueRequest=new CreateQueueRequest().withQueueName(queueName +(UUID.randomUUID().toString()));
        queueRequest.setAttributes(queueAttributesMap);
        CreateQueueResult queueResult =sqsClient.createQueue(queueRequest);
        return queueResult.getQueueUrl();
    }

    public static void makeHTMLSummaryFile(String outputFileKey, String inputFileName) throws IOException {
        S3Object s3Object =s3Client.getObject(new GetObjectRequest(managerBucketName,outputFileKey));
        //fileDownloader(convertedFileURL, outputFileKey);
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(s3Object.getObjectContent()));
        File file=new File(outputFileKey);
        PrintWriter writer = new PrintWriter(outputFileKey+received.get()+".html");

        writer.println("<html>\n" +
                "<body>\n" +
                "<h1 style=\"text-align:center\"> Summary file for input file:</h1>\n" +
                "<h3 style=\"text-align:center\">");
        String originalInputName=inputFileName.substring(22,inputFileName.length()-36);
        writer.println(originalInputName+"</h3>");

        String line=bufferedReader.readLine();
        while(line!=null){
            if(!line.equals(""))
                writer.println(line + "<br><br><br><br>");
            line=bufferedReader.readLine();
        }
        writer.println("</body>\n" + "</html>");
        writer.close();
    }


    public static void messageDelete(Message msg, String queueName)
    {
        String messageRecieptHandle = msg.getReceiptHandle();
        sqsClient.deleteMessage(new DeleteMessageRequest(queueName, messageRecieptHandle));
    }



}





