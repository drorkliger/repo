import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class CalculateLogReducer extends Reducer <DecadeWord1Word2, C1C12, DecadeWord1Word2, DoubleWritable> {
    private long c2;
    private long N;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        this.c2 = 0;
        this.N = 0;
    }

    @Override
    protected void reduce(DecadeWord1Word2 key, Iterable<C1C12> values, Context context) throws IOException, InterruptedException {
        long c1=0;
        long c12=0;
        String word1 = key.getWord1().toString();
        String word2 = key.getWord2().toString();
        int decade = key.getDecade().get();

        for(C1C12 value : values) // supposed to be only 1 value here
        {
            c1 = value.getC1().get();
            c12 = value.getC12().get();
        }

        if(word1.equals("**") || word2.equals("**")) // case of N, means we reached the decade counter
        {
            this.N = c1;

        } else if (word1.equals("***") || word2.equals("***")) // case of 1gram , means that we reached C2 word
                    this.c2 = c1;

        else{   //case of 2gram, means we can calculate the log (we have N, c1, c2, c12)

            double L1, L2, L3, L4, p, p1, p2;
            double C1 = c1;
            double C2 = c2;
            double C12 = c12;
            double NN = N;
            p = C2/NN;
            p1 = C12/C1;
            p2 = (C2-C12) / (NN-C1);

            L1 = Lcalc(C12,C1,p);
            L2 = Lcalc(C2-C12,NN-C1,p);
            L3 = Lcalc(C12, C1, p1);
            L4 = Lcalc(C2-C12, NN-C1, p2);

            double logLikeliHoodRatio = Math.log(L1) + Math.log(L2) + Math.log(L3) + Math.log(L4);


            context.write(new DecadeWord1Word2(decade, word2, word1), new DoubleWritable(logLikeliHoodRatio)); // in case we have 2gram key, we swap word1 and word2
            // in order to make sort the 2gram words by word2 to
            // get c2 from word2
        }
    }

    private double Lcalc(double k, double n, double x)
    {
        return Math.pow(x,k) * Math.pow(1-x ,n-k);
    }

}
