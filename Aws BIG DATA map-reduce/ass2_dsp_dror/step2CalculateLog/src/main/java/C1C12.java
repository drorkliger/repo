import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class C1C12 implements Writable {

    private LongWritable C1;
    private LongWritable C12;

    public C1C12(){
        this.C1 = new LongWritable(0);
        this.C12 = new LongWritable(0);
    }

    public C1C12(long C1, long C12){ //constructor
        this.C1 = new LongWritable(C1);
        this.C12 = new LongWritable(C12);
    }


    @Override
    public void write(DataOutput dataOutput) throws IOException { // how to write this object
        C1.write(dataOutput);
        C12.write(dataOutput);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {// how to read this object
        this.C1.readFields(dataInput);
        this.C12.readFields(dataInput);
    }


    @Override
    public String toString() {
        return C1.toString() + " " + C12.toString();
    }


    public LongWritable getC1() {
        return C1;
    }

    public LongWritable getC12() {
        return C12;
    }

    public void setC1(LongWritable c1) {
        C1 = c1;
    }

    public void setC12(LongWritable c12) {
        C12 = c12;
    }
}

