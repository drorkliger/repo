import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class SortForTop100Mapper extends Mapper<Text, Text, DecadeLogResult, DecadeWord1Word2> {
    @Override
    protected void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        String[] keys = key.toString().split(" ");
        String[] values = value.toString().split(" ");

        int decade = Integer.parseInt(keys[0]);
        String word1 = keys[1];
        String word2 = keys[2];

        double logResult = Double.parseDouble(values[0]);

        context.write(new DecadeLogResult(decade,logResult), new DecadeWord1Word2(decade, word1, word2));
    }
}
