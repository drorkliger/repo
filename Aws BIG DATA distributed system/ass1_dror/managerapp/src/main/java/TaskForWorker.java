import java.util.concurrent.atomic.AtomicBoolean;

public class TaskForWorker {

    private String command;
    private String pdfURL;
    private String inputFile;
    private String outputFile;
    private String outputQueueURL;
    private AtomicBoolean isDone;

    public TaskForWorker(String command, String pdfURL, String inputFile, String outputFile, String outputQueueURL)
    {
        this.command=command;
        this.pdfURL=pdfURL;
        this.inputFile=inputFile;
        this.outputFile=outputFile;
        this.outputQueueURL=outputQueueURL;
        this.isDone=new AtomicBoolean(false);
    }


    public String getCommand() {
        return command;
    }

    public String getPdfURL() {
        return pdfURL;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getInputFile() {
        return inputFile;
    }

    public AtomicBoolean getIsDone() {
        return isDone;
    }
}
