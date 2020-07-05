import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import java.io.IOException;


/*
Gets a line number as a key and a word and stats about it as a value.
Returns a pair of decade and word as a key and a value of the number
of occurrences in this decade (if not a stop word).
 */

public class EngSingleWordsMapper extends Mapper<LongWritable,Text, DecadeWord1Word2,LongWritable> {
    private Configuration conf;
    private String stop_words;
    private String [] stop_words_array;


    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        this.conf = context.getConfiguration();
        this.stop_words = conf.get("stop_words");
        this.stop_words_array = stop_words.split("\\t+"); // initiating stop words array
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String [] valueArr = value.toString().split("\\t+"); // valueArr = [word , year, num_Of_Occurrences, ....NOT RELEVANT]
        String [] singleOrPairWOrd = valueArr[0].split(" ");

        for (String w: singleOrPairWOrd) {
            if(w.equals("**") || w.equals("***")) //delete all the ** *** words in the corpus because we use these for the N counters and the 1gram words
                return;
            for(String stopWord: stop_words_array) {//______ this for run on 1 or 2 words depends on 1gram or 2gram input_____
                if (stopWord.equals(w)) //_______make sure this isn't a stop word__________
                    return;
            }
        }

        int decadeVal = Integer.parseInt(valueArr[1]) / 10;
        String word1Val = singleOrPairWOrd[0];
        String word2Val = "***";

        if (singleOrPairWOrd.length == 2)
            word2Val = singleOrPairWOrd[1];

        //______________add the current word-decade_____________
        DecadeWord1Word2 decadeWordPair = new DecadeWord1Word2(decadeVal, word1Val, word2Val);
        context.write(decadeWordPair, new LongWritable(Long.parseLong(valueArr[2])));

        if(singleOrPairWOrd.length == 1) {
            //_____________add occurNumber for this word-decade to the N - counter__________________
            DecadeWord1Word2 counter = new DecadeWord1Word2(decadeVal, "**", "**"); // make special word so there won't be any problems of duplication
            context.write(counter, new LongWritable(Long.parseLong(valueArr[2])));
        }

    }
}
