import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;


public class DecadeWord1Word2 implements WritableComparable<DecadeWord1Word2> {

    private IntWritable decade;
    private Text word1;
    private Text word2;

    public DecadeWord1Word2(){
        decade = new IntWritable(0);
        word1 =  new Text();
        word2 =  new Text();

    } // empty constructor

    public DecadeWord1Word2(int decade, String word1, String word2){ //constructor
        this.decade = new IntWritable(decade);
        this.word1 = new Text(word1);
        this.word2 = new Text(word2);
    }


    @Override
    public int compareTo(DecadeWord1Word2 other) { // compareTo
        //____the order of importance in this compare is (1)decade (2)word1 (3)word2
        int decadeCompare = this.decade.get() - other.decade.get();
        if (decadeCompare == 0) {
            int word1Compare = this.word1.toString().compareTo(other.word1.toString());
            if(word1Compare != 0)
                return word1Compare;
            else return this.word2.toString().compareTo(other.word2.toString());

        }
        return decadeCompare;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException { // how to write this object
        decade.write(dataOutput);
        word1.write(dataOutput);
        word2.write(dataOutput);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {// how to read this object
        this.decade.readFields(dataInput);
        this.word1.readFields(dataInput);
        this.word2.readFields(dataInput);
    }

    public IntWritable getDecade() {
        return decade;
    }

    public void setDecade(IntWritable decade) {
        this.decade = decade;
    }

    public Text getWord1() {
        return word1;
    }

    public Text getWord2() {
        return word2;
    }

    public void setWord1(Text word1) {
        this.word1 = word1;
    }

    public void setWord2(Text word2) {
        this.word2 = word2;
    }

    @Override
    public String toString() {
        return decade.toString() + " " + word1.toString() + " " + word2.toString();
    }
}
