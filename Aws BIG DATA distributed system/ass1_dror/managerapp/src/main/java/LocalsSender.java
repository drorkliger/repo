import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalsSender implements Runnable{

    private AmazonSQS sqsClient;
    private Message message;
    private ConcurrentHashMap<String,InputFileInfo> inputNameToInputInfo;
    private String managerBucket;
    private String workers_To_managerSQS;
    private AmazonS3 s3Client;
    private AtomicInteger inputhandeled;
    private boolean resent;



    private Debugger debugger;

    public LocalsSender(AmazonSQS sqsClient, Message message, ConcurrentHashMap<String,InputFileInfo> inputNameToInputInfo,
                        String managerBucket, AmazonS3 s3Client, AtomicInteger inputhandeled, String workers_To_managerSQS)
    {
        this.sqsClient=sqsClient;
        this.message=message;
        this.inputNameToInputInfo=inputNameToInputInfo;
        this.managerBucket=managerBucket;
        this.inputhandeled=inputhandeled;
        this.s3Client=s3Client;
        this.resent=false;
        this.debugger=new Debugger("LocalsSender");
        this.workers_To_managerSQS = workers_To_managerSQS;
    }


    public void run() {
        debugger.printHere("started localsenderRunnable","beginning");
        String command = message.getMessageAttributes().get("command").getStringValue();
        String inputFile = message.getMessageAttributes().get("inputFile").getStringValue();
        String outputFile = message.getMessageAttributes().get("outputFile").getStringValue();
        String outputQueueURL = message.getMessageAttributes().get("outputQueueURL").getStringValue();
        String convertedFile = message.getMessageAttributes().get("convertedFile").getStringValue();
        String outcome= message.getMessageAttributes().get("outcome").getStringValue();
        String pdfURL = message.getMessageAttributes().get("pdfURL").getStringValue();



        String convertedFileURL= "https://s3.us-east-1.amazonaws.com/"+ managerBucket+ "/"+ convertedFile;

        InputFileInfo inputFileInfo = inputNameToInputInfo.get(inputFile);

        ConcurrentLinkedQueue <TaskForWorker> tasks= inputFileInfo.getTasks();

        for (TaskForWorker task: tasks) {
            if (task.getPdfURL().equals(pdfURL)) {
                    task.getIsDone().set(true);
                    inputFileInfo.getNumOfFinishedTasks().incrementAndGet();
                    break;
            }
        }
        //if(!resent) {

        String taskSummary = "<u><b>The command was</u></b>: " + command + "&emsp;&emsp;<u><b>" + "The original PDF-URL is</b></u>: " + pdfURL + "<br><u><b>The convertedFileURL is</b></u>: " + convertedFileURL + "\n";
        //_____________check if there was an error
        if (!outcome.equals("File downloaded successfully"))
            taskSummary = "<u><b>The command was</u></b>: " + command + "&emsp;&emsp;<u><b>" + "The original PDF-URL is</b></u>: " + pdfURL + "<br><u><b>The convertedFileURL is</b></u>: " + outcome + "\n";

        inputFileInfo.getSummary().add(taskSummary);
        messageDelete(message, workers_To_managerSQS);

        //__________handle the case when we haven't sent all the tasks yet____________
        // _________but we check if sent and processed tasks have the same amount_______________


        debugger.printHere("numOfTasks: " + inputFileInfo.getNumOfTasks().get() + "\t\t"
                + "numOfFinishedTasks: " + inputFileInfo.getNumOfFinishedTasks().get()
                + "\t\tISDONE: " +inputFileInfo.getIsDoneSendingTasks().get(), "line 97");

        //______________in this case we are finished with this input file____________________
        if (inputFileInfo.getNumOfTasks().get() == inputFileInfo.getNumOfFinishedTasks().get()
                & inputFileInfo.getIsDoneSendingTasks().get()) {



            //________________writing a summary file_____________________
            File output = new File(outputFile);
            try {
                PrintWriter writer = new PrintWriter(output);

                for (String line : inputFileInfo.getSummary()) {
                    writer.println(line);
                }
                writer.close();

                //__________uploading the file to s3_____________________
                PutObjectRequest putObjectRequest = new PutObjectRequest(managerBucket, outputFile, output);

                s3Client.putObject(putObjectRequest);

                //_________________sending the file to the local__________________
                Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
                messageAttributes.put("inputFile", new MessageAttributeValue().withDataType("String").withStringValue(inputFile));
                messageAttributes.put("outputFile", new MessageAttributeValue().withDataType("String").withStringValue(outputFile));
                messageAttributes.put("outcome", new MessageAttributeValue().withDataType("String").withStringValue(outcome));
                SendMessageRequest sendMessageReq = new SendMessageRequest().withQueueUrl(outputQueueURL)
                        .withMessageBody("inputFile: " + inputFile + "is fully processed, the summary file name is: " + outputFile)
                        .withMessageAttributes(messageAttributes);
                sqsClient.sendMessage(sendMessageReq);

                inputhandeled.incrementAndGet();

                //_________when done handling the input, we can delete its info to save space____________
                inputNameToInputInfo.remove(inputFile);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }



    }
    private void messageDelete(Message msg, String queueName)
    {
        String messageRecieptHandle = msg.getReceiptHandle();
        sqsClient.deleteMessage(new DeleteMessageRequest(queueName, messageRecieptHandle));
    }
}
