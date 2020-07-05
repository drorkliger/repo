
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class Main {

    private static String workers_To_managerSQS;
    private static String manager_To_workersSQS;
    private static String managerBucket;


    private static AmazonSQS sqsClient;
    private static AmazonS3 s3Client;

    private static boolean isTerminate;
    private static String inputFile;
    private static String outputFile;
    private static String outputQueueURL;
    private static String command;
    private static String pdfURL;
    private static String convertedFileName;


    public static void main(String[] args) {
            //_____assuming args are workers_To_managerSQS, manager_To_workersSQS, managerBucket
        workers_To_managerSQS=args[0];
        manager_To_workersSQS=args[1];
        managerBucket=args[2];


        //___________creating s3 CLIENT___________
        s3Client = AmazonS3ClientBuilder.standard()
                .withRegion("us-east-1")
                .build();

        //______________creating sqs CLIENT__________________
        sqsClient = AmazonSQSClientBuilder.standard()
                .withRegion("us-east-1")
                .build();

        //_________initiate variables________
        List<Message> incomingMsgs;
        isTerminate = false;
        convertedFileName="default";


        while (!isTerminate) {
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(manager_To_workersSQS);
            incomingMsgs = sqsClient.receiveMessage(receiveMessageRequest.withMessageAttributeNames("All")).getMessages();


            if (!incomingMsgs.isEmpty()) {
                String messageReceiptHandle = incomingMsgs.get(0).getReceiptHandle();

                Message msg = incomingMsgs.get(0);

                if(!msg.getBody().equals("terminate")) {
                    //___________the workers don't delete the terminate message____________
                    sqsClient.deleteMessage(new DeleteMessageRequest(manager_To_workersSQS, messageReceiptHandle));

                    Map <String, MessageAttributeValue> attributeValueMap=msg.getMessageAttributes();
                    inputFile = attributeValueMap.get("inputFile").getStringValue();
                    outputFile = attributeValueMap.get("outputFile").getStringValue();
                    outputQueueURL = attributeValueMap.get("outputQueueURL").getStringValue();
                    command = attributeValueMap.get("command").getStringValue();
                    pdfURL = attributeValueMap.get("pdfURL").getStringValue();

                    String outcome = "";


                    //_____________naming the file we will download_________________
                    String pdfFileName = "PDFFileDownloaded" +(UUID.randomUUID().toString());
                    if (!fileDownloader(pdfURL, pdfFileName)) {
                        outcome = "Could't download the PDF file";
                        convertedFileName="file couldn't be downloaded";
                    }
                    else {
                        outcome = "File downloaded successfully";
                        String resultFilePath = FileSystems.getDefault().getPath(pdfFileName).toAbsolutePath().toString().replace("\\","\\\\")+".pdf";

                        pdfToOtherTypes pdfToOtherTypes = new pdfToOtherTypes(resultFilePath);

                        try {
                            pdfToOtherTypes.readPDF(command);

                            convertedFileName="ConvertedFile"+pdfFileName.replace(".pdf", pdfToOtherTypes.convertCommandToType(command));

                            String convertedFileRelativePath = resultFilePath
                                    .replace("\\","\\\\")
                                    .replace(".pdf", pdfToOtherTypes.convertCommandToType(command));

                            File convertedFile = new File(convertedFileRelativePath);

                            //________________uploading the converted file to the bucket_________________
                            PutObjectRequest putObjectRequest = new PutObjectRequest(managerBucket, convertedFileName, convertedFile);
                            s3Client.putObject(putObjectRequest);

                        } catch (Exception e) {
                            e.printStackTrace();
                            outcome += " but was unable to be converted to 'non-PDF' format";
                            convertedFileName="couldn't convert";
                        }
                        //_____________set the converted file (pdf and converted has same name prefix)_____________




                    }
                    //_______________set attributes before sending message______________________
                    Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
                    messageAttributes.put("command", new MessageAttributeValue().withDataType("String").withStringValue(command));
                    messageAttributes.put("inputFile", new MessageAttributeValue().withDataType("String").withStringValue(inputFile));
                    messageAttributes.put("outputFile", new MessageAttributeValue().withDataType("String").withStringValue(outputFile));
                    messageAttributes.put("outputQueueURL", new MessageAttributeValue().withDataType("String").withStringValue(outputQueueURL));
                    messageAttributes.put("convertedFile", new MessageAttributeValue().withDataType("String").withStringValue(convertedFileName));
                    messageAttributes.put("outcome", new MessageAttributeValue().withDataType("String").withStringValue(outcome));
                    messageAttributes.put("pdfURL", new MessageAttributeValue().withDataType("String").withStringValue(pdfURL));



                    SendMessageRequest sendMessageReq = new SendMessageRequest().withQueueUrl(workers_To_managerSQS).withMessageBody("a").withMessageAttributes(messageAttributes);
                    sqsClient.sendMessage(sendMessageReq);
                }
                else
                {
                    isTerminate=true;
                    SendMessageRequest sendMessageReq = new SendMessageRequest().withQueueUrl(workers_To_managerSQS).withMessageBody("terminating");
                    sqsClient.sendMessage(sendMessageReq);
                }
            }
        }
    }



    public static boolean fileDownloader (String fileURL, String filename){
        URL url;
        try {
            url =new URL(fileURL);
            HttpURLConnection httpURLConnection= (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(2000);
            httpURLConnection.setConnectTimeout(2000);
            httpURLConnection.connect();
            InputStream inputStream= httpURLConnection.getInputStream();
            Files.copy(inputStream, Paths.get(filename + ".pdf"), StandardCopyOption.REPLACE_EXISTING);
            inputStream.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }



}
