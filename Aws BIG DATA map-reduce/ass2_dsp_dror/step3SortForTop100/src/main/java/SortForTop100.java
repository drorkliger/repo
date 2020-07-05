import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class SortForTop100 {

    public static class SortForTop100Partitioner extends Partitioner<DecadeLogResult, DecadeWord1Word2> {
        @Override
        public int getPartition(DecadeLogResult key, DecadeWord1Word2 value, int numPartitions) {
            return Math.abs(key.getDecade().hashCode()) % numPartitions;
        }
    }

    public static void main(String[] args) {

        Configuration conf = new Configuration();

        Job job = null;
        try {
            job = Job.getInstance(conf, "SortForTop100");

            job.setJarByClass(SortForTop100.class);
            job.setMapperClass(SortForTop100Mapper.class);
            job.setPartitionerClass(SortForTop100Partitioner.class);
            job.setReducerClass(SortForTop100Reducer.class);
            job.setMapOutputKeyClass(DecadeLogResult.class);
            job.setMapOutputValueClass(DecadeWord1Word2.class);
            job.setOutputKeyClass(DecadeWord1Word2.class);  //TODO COMPLETE
            job.setOutputValueClass(DoubleWritable.class);  //TODO COMPLETE
            FileInputFormat.addInputPath(job, new Path(args[0]));
            FileOutputFormat.setOutputPath(job, new Path(args[1]));
            job.setInputFormatClass(KeyValueTextInputFormat.class);
            job.setOutputFormatClass(TextOutputFormat.class);

            System.exit(job.waitForCompletion(true) ? 0 : 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
