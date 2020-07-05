import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.IOException;


public class SortForTop100Reducer extends Reducer<DecadeLogResult, DecadeWord1Word2, DecadeWord1Word2, DoubleWritable> {

    private int counter;
    private int decade;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        this.counter = 1; // the current collocation , legitimate range: 1 to 100
        this.decade = 0;
    }

    @Override
    protected void reduce(DecadeLogResult key, Iterable<DecadeWord1Word2> values, Context context) throws IOException, InterruptedException {
        double logResult = key.getLogResult().get();
        int decade = key.getDecade().get();
        String word1="",word2="";

        if (this.decade != decade) {
            this.decade = decade;
            counter = 1;
        }

        if (this.counter <= 100) { // in this case we reached 100 lines for this decade, so no need to read the next values (from this decade)
            for (DecadeWord1Word2 value : values) {
                word1 = value.getWord1().toString();
                word2 = value.getWord2().toString();
                // decade = value.getDecade().get(); we can get it from here as well

                if (counter <= 100) {
                    this.counter++;
                    context.write(new DecadeWord1Word2(decade, word1, word2), new DoubleWritable(logResult));
                } else
                    return; // in this case we reached 100 lines for this decade, so no need to read the next values (from this decade that has the same logResult)
            }
        }
    }
}
