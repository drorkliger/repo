import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalsManager implements Runnable {

    private String locals_managerSQS;
    private String workers_To_managerSQS;
    private String managerBucket;
    private ExecutorService reactor;
    private AtomicBoolean isTerminate;
    private AtomicInteger inputReceived;
    private AmazonSQS sqsClient;
    private AmazonS3 s3Client;
    private List <Message> localsIncomingMessages;
    private List <String> theTerminatorURL;
    private boolean isDoneLocalManger;
    private AtomicInteger inputhandeled;
    private AtomicInteger runningWorkers;


    private String manager_To_workersSQS;
    private ConcurrentHashMap <String,InputFileInfo> inputNameToInputInfo;


    private Debugger debugger;



    public LocalsManager(String managerBucket, String locals_managerSQS, String workers_To_managerSQS, AtomicBoolean isTerminate,
                         ConcurrentHashMap<String, InputFileInfo> inputNameToInputInfo, AmazonSQS sqsClient, AmazonS3 s3Client,
                         String manager_To_workersSQS, AtomicInteger inputReceived,
                         List<String> theTerminatorURL,AtomicInteger inputhandeled, AtomicInteger runningWorkers)
    {
        this.locals_managerSQS=locals_managerSQS;
        this.workers_To_managerSQS=workers_To_managerSQS;
        this.inputNameToInputInfo=inputNameToInputInfo;
        this.managerBucket=managerBucket;
        this.isTerminate=isTerminate;
        this.sqsClient=sqsClient;
        this.s3Client=s3Client;
        this.manager_To_workersSQS=manager_To_workersSQS;
        this.inputReceived=inputReceived;
        this.inputReceived.set(0);
        this.theTerminatorURL=theTerminatorURL;
        this.inputhandeled=inputhandeled;
        this.isDoneLocalManger = false;
        this.runningWorkers=runningWorkers;
        this.debugger = new Debugger("LocalsManager");
    }

    public void run() {
        reactor=Executors.newFixedThreadPool(4);
        Runtime runtime = Runtime.getRuntime();
        long ratio=100; //____100 is just bigger than 2______
        //_________runs as long as no termination has arrived_________
        while(!isDoneLocalManger)
        {
            //___________check the memory usage of the manager___________
            do {
                runtime.gc();//______garbage-collector_____
                long free = runtime.freeMemory();
                long total = runtime.totalMemory();
                ratio = total / (total - free);
                if(ratio<2) //_________means that we are using more than 50% of the memory allocated to the manager_______
                {
                    try { // wait here to make sure the manager cleans space before getting new input files
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } while(ratio < 2);

            //__________pull out all the messages that currently in the queue_____________
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(locals_managerSQS);
            localsIncomingMessages = sqsClient.receiveMessage(receiveMessageRequest.withMessageAttributeNames("All")).getMessages();

            for(Message msg :localsIncomingMessages)
            {
                if (msg.getBody().equals("terminate"))
                {//___________if terminate send a message forward to the workers (one msg for all)_________________

                    theTerminatorURL.add(msg.getMessageAttributes().get("outputQueueURL").getStringValue());
                    isTerminate.set(true);

                    while(inputReceived.get()!=inputhandeled.get()) {
                        try { // wait here to make sure the workers gets it at the end of their work
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    messageDelete(msg, locals_managerSQS); //________terminate from local_________
                    //___________sending terminate signal "runningWorker" times so workers won't have to wait for this message_____________
                    for(int j=0;j<runningWorkers.get();j++) {
                        SendMessageRequest sendMessageReq = new SendMessageRequest().withQueueUrl(manager_To_workersSQS).withMessageBody("terminate");
                        sqsClient.sendMessage(sendMessageReq);
                    }

                    isDoneLocalManger=true;
                    break;
                }
                else
                {
                    //____________making the runnable "MsgToTask"______________
                    inputReceived.incrementAndGet();
                    MsgToTask msgToTask=new MsgToTask(msg,s3Client,managerBucket,manager_To_workersSQS,
                            sqsClient,inputNameToInputInfo,locals_managerSQS);

                    reactor.submit(msgToTask);
                    messageDelete(msg, locals_managerSQS);
                }


            }
        }



    }

    private void messageDelete(Message msg, String queueName)
    {
        String messageRecieptHandle = msg.getReceiptHandle();
        sqsClient.deleteMessage(new DeleteMessageRequest(queueName, messageRecieptHandle));
    }
}








