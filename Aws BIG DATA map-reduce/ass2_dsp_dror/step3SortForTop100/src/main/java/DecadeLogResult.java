import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DecadeLogResult implements WritableComparable<DecadeLogResult> {

    private IntWritable decade;
    private DoubleWritable logResult;

    public DecadeLogResult()
    {
        decade = new IntWritable(0);
        logResult = new DoubleWritable(0);
    }

    public DecadeLogResult (int decade,double logResult){
        this.decade = new IntWritable(decade);
        this.logResult = new DoubleWritable(logResult);
    }

    @Override
    public int compareTo(DecadeLogResult other) {
        int decadeCompare = this.decade.get() - other.decade.get();
        if (decadeCompare == 0){ // same decade -> check logResult
            return Double.compare(-this.logResult.get(), -other.logResult.get()); // minus is added in order to ger descending order
        }
        return decadeCompare;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        this.decade.write(dataOutput);
        this.logResult.write(dataOutput);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.decade.readFields(dataInput);
        this.logResult.readFields(dataInput);
    }

    @Override
    public String toString() {
        return this.decade.toString() + " " + this.logResult.toString();
    }

    public IntWritable getDecade() {
        return decade;
    }

    public DoubleWritable getLogResult() {
        return logResult;
    }

    public void setDecade(IntWritable decade) {
        this.decade = decade;
    }

    public void setLogResult(DoubleWritable logResult) {
        this.logResult = logResult;
    }
}
