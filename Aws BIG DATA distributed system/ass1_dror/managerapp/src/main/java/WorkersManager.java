import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkersManager implements Runnable {

    private AtomicBoolean isTerminate;
    private String workers_To_managerSQS;
    private String managerBucket;
    private ExecutorService reactor;
    private AmazonSQS sqsClient;
    private AmazonS3 s3Client;
    private ConcurrentHashMap<String,InputFileInfo> inputNameToInputInfo;
    private AtomicInteger inputhandeled;
    private AtomicInteger runningWorkers;
    private AtomicBoolean everyWorkerTerminated;



    private Debugger debugger;

    public WorkersManager(AtomicBoolean isTerminate, String workers_To_managerSQS, AmazonSQS sqsClient,
                          AmazonS3 s3Client, ConcurrentHashMap<String,InputFileInfo> inputNameToInputInfo, String managerBucket,
                          AtomicInteger inputhandeled, AtomicInteger runningWorkers, AtomicBoolean everyWorkerTerminated)
    {
        this.isTerminate=isTerminate;
        this.workers_To_managerSQS=workers_To_managerSQS;
        this.s3Client=s3Client;
        this.sqsClient=sqsClient;
        this.inputNameToInputInfo=inputNameToInputInfo;
        this.inputhandeled=inputhandeled;
        this.managerBucket=managerBucket;
        this.runningWorkers=runningWorkers;
        this.everyWorkerTerminated=everyWorkerTerminated;
        this.debugger = new Debugger("WorkersManager");

    }

    public void run() {
        reactor= Executors.newFixedThreadPool(6);
        debugger.printHere("first check","beginning");


        //____________this while keeps going until every terminating msg has arrived and we are ready to terminate the instances_________________
        while (!everyWorkerTerminated.get()){

            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(workers_To_managerSQS);
            List <Message> msgsList = sqsClient.receiveMessage(receiveMessageRequest.withMessageAttributeNames("All")).getMessages();

            for(Message message: msgsList)
            {
                if(message.getBody().equals("terminating"))
                    runningWorkers.getAndDecrement();
                else {
                    LocalsSender localsSender = new LocalsSender(sqsClient, message, inputNameToInputInfo, managerBucket, s3Client, inputhandeled,workers_To_managerSQS);
                    reactor.submit(localsSender);
                }
            }
        }
    }


}
