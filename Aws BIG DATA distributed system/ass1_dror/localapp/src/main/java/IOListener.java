import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class IOListener implements Runnable {
        private Scanner in;
        private String input;
        private String local_Manager_QueueURL;
        private AmazonS3 s3Client;
        private String managerBucketName;
        private AtomicBoolean isDoneInput;
        private AmazonSQS sqsClient;
        private String Local_QueueURL;
        private String outputFileName;
        private AtomicInteger sent;


    public IOListener(String local_Manager_QueueURL, String managerBucketName ,AmazonS3 s3Client , AtomicBoolean isDoneInput
                    ,AmazonSQS sqsClient, String Local_QueueURL, String outputFileName, AtomicInteger sent){
         this.in= new Scanner(System.in);
         this.input="";
         this.local_Manager_QueueURL=local_Manager_QueueURL;
         this.s3Client=s3Client;
         this.managerBucketName=managerBucketName;
         this.isDoneInput=isDoneInput;
         this.sqsClient=sqsClient;
         this.Local_QueueURL=Local_QueueURL;
         this.outputFileName=outputFileName;
         this.sent=sent;
    }

    public void run() {
        //________endless loop that stops only when the thread stops________
        while(!this.isDoneInput.get())
        {
            System.out.println("For another input, please enter 'y', enter 'n' if you're done");
            String check=in.nextLine();
            if(check.compareTo("y")==0) {
                System.out.println("Please enter the file name");
                input = in.nextLine();
                //_____________set the input file____________
                String inputFileKey = "Input_File_From_Local_" + input + (UUID.randomUUID().toString());
                try{
                String inputFilePath = FileSystems.getDefault().getPath(input).toAbsolutePath().toString().replace("\\","\\\\");
                File inputFile = new File(inputFilePath);

                PutObjectRequest putObjectRequest = new PutObjectRequest(managerBucketName, inputFileKey, inputFile);
                s3Client.putObject(putObjectRequest);

                //______________sending the relevant info to the manager________________________
                Map<String, MessageAttributeValue> messageAttributes= new HashMap<>();
                messageAttributes.put("inputFile",new MessageAttributeValue().withDataType("String").withStringValue(inputFileKey));
                messageAttributes.put("outputFile",new MessageAttributeValue().withDataType("String").withStringValue(outputFileName));
                messageAttributes.put("outputQueueURL",new MessageAttributeValue().withDataType("String").withStringValue(Local_QueueURL));
                SendMessageRequest sendMessageReq = new SendMessageRequest().withQueueUrl(local_Manager_QueueURL)
                        .withMessageBody("inputFile: "+ inputFileKey + " myQueueURL: "+Local_QueueURL)
                        .withMessageAttributes(messageAttributes);
                sqsClient.sendMessage(sendMessageReq);
                sent.incrementAndGet();}
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else if(check.compareTo("n")==0)
                this.isDoneInput.set(true);
            else System.out.println("Wrong input, please choose again");
        }

    }
}
