import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;

public class Main {

    private static AmazonElasticMapReduce mrClient;
    private static String bucketURL = "s3n://dror-ass2/";
    private static String logsPath = bucketURL + "logs/";
    private static String firstStepJarName = bucketURL+ "jars/" +"collocationExtraction.jar"; //TODO add this jar to s3 to s3 URL
    private static String secondStepJarName = bucketURL+ "jars/" +"step2CalculateLog.jar"; //TODO add this jar to s3 to s3 URL
    private static String thirdStepJarName = bucketURL+ "jars/" +"step3SortForTop100.jar"; //TODO add this jar to s3 to s3 URL
    private static String firstStepInput = "s3://datasets.elasticmapreduce/ngrams/books/20090715/eng-us-all/1gram/data";
    private static String firstStepInput2gram = "s3://datasets.elasticmapreduce/ngrams/books/20090715/eng-us-all/2gram/data";
     /*private static String firstStepInput = bucketURL + "inputFiles/" + "googlebooks-eng-all-1gram-20120701-z" ; // TODO change to the relevant corpus
     private static String firstStepInput2gram = bucketURL + "inputFiles/" + "googlebooks-eng-all-2gram-20120701-zy" ; // TODO change to the relevant corpus*/
  //  private static String firstStepInput = bucketURL + "inputFiles/" + "justfile2.txt" ; // TODO change to the relevant corpus
 //   private static String firstStepInput2gram = bucketURL + "inputFiles/" + "justfile2gram.txt" ; // TODO change to the relevant corpus
    private static String firstStepOutput = bucketURL + "target/1" + System.currentTimeMillis() +"/"; // TODO change to s3 URL
    private static String secondStepOutput = bucketURL + "target/2" + System.currentTimeMillis() +"/"; // TODO change to s3 URL
    private static String thirdStepOutput = bucketURL + "target/3" + System.currentTimeMillis() +"/"; // TODO change to s3 URL

    private static String [] eng_stop_words_array;
    private static String [] heb_stop_words_array;

    public static void main(String[] args) {

        //___________create new AMAZON MAP REDUCE client__________
        mrClient= AmazonElasticMapReduceClientBuilder
                .standard()
                .withRegion("us-east-1")
                .build();

        //__________initiating stop words arrays______________
        String stop_words;
        if (args[0].compareTo("eng") == 0) {
            eng_stop_words_array = new String[319];
            stop_words = stopWordsInit(eng_stop_words_array, "eng_stop_words");
        }else if(args[0].compareTo("heb") == 0) {
            heb_stop_words_array = new String [150];
            stop_words = stopWordsInit(heb_stop_words_array,"heb_stop_words");
            firstStepInput = "s3://datasets.elasticmapreduce/ngrams/books/20090715/heb-all/1gram/data";
            firstStepInput2gram = "s3://datasets.elasticmapreduce/ngrams/books/20090715/heb-all/2gram/data";
        } else {
            System.out.println("Wrong input, please enter \"heb\" or \"eng\" as an argument");
            return;
        }


        //___________configuring the first step_______________
        StepConfig step1Conf= new StepConfig()
                .withName("CountEngWordsByDecade")
                .withActionOnFailure(ActionOnFailure.TERMINATE_JOB_FLOW)
                .withHadoopJarStep(new HadoopJarStepConfig()
                        .withJar(firstStepJarName)
                        .withMainClass("CountEngWordsByDecade")
                        .withArgs(firstStepInput,firstStepInput2gram,firstStepOutput,stop_words));

        //___________configuring the second step_______________
        StepConfig step2Conf= new StepConfig()
                .withName("step2CalculateLog")
                .withActionOnFailure(ActionOnFailure.TERMINATE_JOB_FLOW)
                .withHadoopJarStep(new HadoopJarStepConfig()
                        .withJar(secondStepJarName)
                        .withMainClass("CalculateLog")
                        .withArgs(firstStepOutput,secondStepOutput));


        //___________configuring the third step_______________
        StepConfig step3Conf= new StepConfig()
                .withName("step3SortForTop100")
                .withActionOnFailure(ActionOnFailure.TERMINATE_JOB_FLOW)
                .withHadoopJarStep(new HadoopJarStepConfig()
                        .withJar(thirdStepJarName)
                        .withMainClass("SortForTop100")
                        .withArgs(secondStepOutput,thirdStepOutput));


        //_____________configuring the instances which the JOB will run on____________
        JobFlowInstancesConfig instancesConfig = new JobFlowInstancesConfig()
                .withInstanceCount(10)
                .withMasterInstanceType(InstanceType.M4Large.toString())
                .withSlaveInstanceType(InstanceType.M4Large.toString())
                .withHadoopVersion("3.2.1").withEc2KeyName("ass2-collocationExtraction")
                .withKeepJobFlowAliveWhenNoSteps(false)
                .withPlacement(new PlacementType("us-east-1a"));

        //__________run the JOB _______________
        RunJobFlowRequest runJobFlowRequest = new RunJobFlowRequest()
                .withName("ass2_dsp")
                .withInstances(instancesConfig)
                .withSteps(step1Conf, step2Conf, step3Conf)
                .withReleaseLabel("emr-5.11.0")
                .withLogUri(logsPath)
                .withServiceRole("EMR_DefaultRole")
                .withJobFlowRole("EMR_EC2_DefaultRole");

        RunJobFlowResult runJobFlowResult = mrClient.runJobFlow(runJobFlowRequest);
        String jobFlowId = runJobFlowResult.getJobFlowId();
        System.out.println("Ran job flow with id: " + jobFlowId);
    }



    private static String stopWordsInit(String [] arr, String filName){
        String filePath = FileSystems.getDefault().getPath(filName).toAbsolutePath().toString().replace("\\","\\\\")+".txt";
        File f=new File(filePath);

        BufferedReader reader = null;
        String word="";
        try {
            reader = new BufferedReader(new FileReader(f));
            word = null;
            word = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //___________as long as the line is not null, we haven't finished______________
        int i = 0;

        while (word != null) {
            arr[i] = word;
            try {
                word = reader.readLine();
            }catch(Exception e){
                e.printStackTrace();
            }
            i++;
        }
        String total="";
        for(String s: arr)
            total+=s+"\t";
        return total;
    }

}
