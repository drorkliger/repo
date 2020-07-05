import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class EngSingleWordsReducer extends Reducer<DecadeWord1Word2, LongWritable, DecadeWord1Word2,C1C12>{
    private long c1;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        this.c1 = 0;
    }

    @Override
    protected void reduce(DecadeWord1Word2 key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
        long sum=0;
        long c12=0;

        for(LongWritable value : values)
            sum += value.get();

        if(key.getWord2().toString().equals("***")) // case of 1gram
            this.c1 = sum;
        else                    //case of 2gram
            c12 = sum;

        if(key.getWord1().toString().equals("**"))
            context.write(key, new C1C12(sum,sum));   //N counter
        else
            context.write(key, new C1C12(c1,c12));   //1gram: c1 ,0      2gram: c1,c12


    }
}
