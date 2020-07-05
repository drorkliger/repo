import com.amazonaws.services.sqs.model.Message;


import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class InputFileInfo {

    private String inputFile;
    private String outputFile;
    private String outputQueueURL;
    private AtomicInteger numOfTasks;
    private AtomicInteger numOfFinishedTasks;
    private ConcurrentLinkedQueue <TaskForWorker> tasks;
    private Message msg;
    private ConcurrentLinkedQueue <String> summary;
    private AtomicBoolean isDoneSendingTasks;


    public InputFileInfo(Message msg, String inputFile, String outputFile, String outputQueueURL){
        this.tasks=new ConcurrentLinkedQueue<TaskForWorker>();
        this.msg=msg;
        this.numOfTasks=new AtomicInteger(0);
        this.inputFile=inputFile;
        this.outputFile=outputFile;
        this.outputQueueURL=outputQueueURL;
        this.numOfFinishedTasks=new AtomicInteger(0);
        this.isDoneSendingTasks=new AtomicBoolean(false);
        this.summary=new ConcurrentLinkedQueue<>();
    }

    public String getOutputQueueURL() {
        return outputQueueURL;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getInputFile() {
        return inputFile;
    }

    public AtomicInteger getNumOfTasks() {
        return numOfTasks;
    }

    public ConcurrentLinkedQueue<TaskForWorker> getTasks() {
        return tasks;
    }

    public AtomicInteger getNumOfFinishedTasks() {
        return numOfFinishedTasks;
    }

    public AtomicBoolean getIsDoneSendingTasks() {
        return isDoneSendingTasks;
    }

    public ConcurrentLinkedQueue<String> getSummary() {
        return summary;
    }

    @Override
    public String toString() {
        String result="";
        result = "inputFile: "+inputFile +"\t"+ "outputFile: "+outputFile+"\t"+"outputQUEUE: "+outputQueueURL;
        return result;
    }
}
