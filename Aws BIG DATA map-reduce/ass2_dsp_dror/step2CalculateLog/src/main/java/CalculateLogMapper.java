import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class CalculateLogMapper extends Mapper<Text, Text, DecadeWord1Word2, C1C12> {

    @Override
    protected void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        String[] keys = key.toString().split(" ");
        String[] values = value.toString().split(" ");

        int decade = Integer.parseInt(keys[0]);
        String word1 = keys[1];
        String word2 = keys[2];


        if (word1.equals("**") || word1.equals("***") || word2.equals("**") || word2.equals("***")){ // in case of 1gram or N counter we just keep the line unchanged
            context.write(new DecadeWord1Word2(decade,word1,word2), new C1C12(Long.parseLong(values[0]), Long.parseLong(values[1])));
    }
        else {
            context.write(new DecadeWord1Word2(decade, word2, word1), new C1C12(Long.parseLong(values[0]), Long.parseLong(values[1])));
                            // in case we have 2gram key, we swap word1 and word2
                            // in order to make sort the 2gram words by word2 to
                            // get c2 from word2
        }
    }

}
