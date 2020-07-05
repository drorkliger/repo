import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class EngSingleWordsCombiner extends Reducer <DecadeWord1Word2, LongWritable, DecadeWord1Word2, LongWritable> {

    @Override
    protected void reduce(DecadeWord1Word2 key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
        long sum=0;
        for(LongWritable value : values)
            sum += value.get();

        context.write(key, new LongWritable(sum));
    }
}
