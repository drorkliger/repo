import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MsgToTask implements Runnable {

    private Message message;
    private AmazonS3 s3Client;
    private String managerBucket;
    private String manager_To_workersSQS;
    private String locals_managerSQS;
    private ConcurrentHashMap<String,InputFileInfo> inputNameToInputInfo;
    private InputFileInfo inputFileInfo;

    private AmazonSQS sqsClient;


    private Debugger debugger;


    public MsgToTask (Message message, AmazonS3 s3Client, String managerBucket,
                       String manager_To_workersSQS, AmazonSQS sqsClient,
                      ConcurrentHashMap <String,InputFileInfo> inputNameToInputInfo,String locals_managerSQS)
    {
        this.message=message;
        this.s3Client=s3Client;
        this.managerBucket=managerBucket;
        this.manager_To_workersSQS=manager_To_workersSQS;
        this.sqsClient=sqsClient;
        this.inputNameToInputInfo=inputNameToInputInfo;
        this.locals_managerSQS=locals_managerSQS;
        this.debugger = new Debugger("MsgToTask");
    }

    public void run() {

        //_________get message info_________________
        Map <String,MessageAttributeValue> msgAttributes = message.getMessageAttributes();
        MessageAttributeValue messageAttributeValue=msgAttributes.get("inputFile");
        String inputFile = messageAttributeValue.getStringValue();

        String outputFile = message.getMessageAttributes().get("outputFile").getStringValue();
        String outputQueueURL = message.getMessageAttributes().get("outputQueueURL").getStringValue();


        if(inputNameToInputInfo.get(inputFile)==null) {//________check whether this message was resent_________________

            inputFileInfo = new InputFileInfo(message, inputFile, outputFile, outputQueueURL);
            //_____________add this input data to the main hashmap for tracking_________________
            inputNameToInputInfo.put(inputFile, inputFileInfo);

            //_________reading the file, line by line
            S3Object s3Object = s3Client.getObject(new GetObjectRequest(managerBucket, inputFile));
            S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();


            BufferedReader reader = new BufferedReader(new InputStreamReader(s3ObjectInputStream));
            String line = null;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //___________as long as the line is not null, we haven't finished______________
            int i = 1;
            // todo get rid of the previous line and all lines with this indicator
            while (line != null) {
                debugger.printHere("reading the " + i + "th line of the input file", "not null line while");

                //__________parse the command line____________
                String[] splittedLine = line.split("\\t+");
                String command = splittedLine[0];
                String pdfURL = splittedLine[1];
                // todo delete the next line
                i++;

                //______________save this task for later check______________
                TaskForWorker task = new TaskForWorker(command, pdfURL, inputFile, outputFile, outputQueueURL);
                inputFileInfo.getTasks().add(task);
                inputFileInfo.getNumOfTasks().incrementAndGet();


                //_____________send the task to the Workers queue_____________
                Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
                messageAttributes.put("command", new MessageAttributeValue().withDataType("String").withStringValue(command));
                messageAttributes.put("inputFile", new MessageAttributeValue().withDataType("String").withStringValue(inputFile));
                messageAttributes.put("pdfURL", new MessageAttributeValue().withDataType("String").withStringValue(pdfURL));
                messageAttributes.put("outputFile", new MessageAttributeValue().withDataType("String").withStringValue(outputFile));
                messageAttributes.put("outputQueueURL", new MessageAttributeValue().withDataType("String").withStringValue(outputQueueURL));

                SendMessageRequest sendMessageReq = new SendMessageRequest().withQueueUrl(manager_To_workersSQS)
                        .withMessageBody("a")
                        .withMessageAttributes(messageAttributes);
                sqsClient.sendMessage(sendMessageReq);


                //___________________read next line_______________________
                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //__________updating inputFileInfo that this thread has finished sending files________________
            inputFileInfo.getIsDoneSendingTasks().set(true);

        }
    }
    private void messageDelete(Message msg, String queueName)
    {
        String messageRecieptHandle = msg.getReceiptHandle();
        sqsClient.deleteMessage(new DeleteMessageRequest(queueName, messageRecieptHandle));
    }

}
