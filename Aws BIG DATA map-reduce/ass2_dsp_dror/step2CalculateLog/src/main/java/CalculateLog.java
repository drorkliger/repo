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

public class CalculateLog {

    public static class CalculateLogPartitioner extends Partitioner<DecadeWord1Word2, C1C12>{
        @Override
        public int getPartition(DecadeWord1Word2 key, C1C12 value, int numPartitions) {
            return Math.abs(key.getDecade().hashCode()) % numPartitions;
        }
    }


    public static void main(String[] args) {

        Configuration conf = new Configuration();

        Job job = null;
        try {
            job = Job.getInstance(conf, "CalculateLog");

            job.setJarByClass(CalculateLog.class);
            job.setMapperClass(CalculateLogMapper.class);
            job.setPartitionerClass(CalculateLogPartitioner.class);
            job.setReducerClass(CalculateLogReducer.class);
            job.setMapOutputKeyClass(DecadeWord1Word2.class);
            job.setMapOutputValueClass(C1C12.class);
            job.setOutputKeyClass(DecadeWord1Word2.class);
            job.setOutputValueClass(DoubleWritable.class);
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
