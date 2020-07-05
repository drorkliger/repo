import java.io.*;
import java.nio.file.FileSystems;

import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
public class CountEngWordsByDecade {

public static class EngSingleWordsPartitioner extends Partitioner <DecadeWord1Word2, LongWritable>{
    @Override
    public int getPartition(DecadeWord1Word2 key, LongWritable value, int numPartitions) {
        return Math.abs(key.getWord1().hashCode()) % numPartitions;
    }
}


    public static void main(String[] args) {

        Configuration conf = new Configuration();
        conf.set("stop_words",args[3]); //adding variable to the environment of mapper

        Job job = null;
        try {
            job = Job.getInstance(conf, "CountEngWordsByDecade");

            job.setJarByClass(CountEngWordsByDecade.class);
            job.setMapperClass(EngSingleWordsMapper.class);
            job.setPartitionerClass(EngSingleWordsPartitioner.class);
            job.setCombinerClass(EngSingleWordsCombiner.class);
            job.setReducerClass(EngSingleWordsReducer.class);
            job.setMapOutputKeyClass(DecadeWord1Word2.class);
            job.setMapOutputValueClass(LongWritable.class);
            job.setOutputKeyClass(DecadeWord1Word2.class);
            job.setOutputValueClass(C1C12.class);
            FileInputFormat.addInputPath(job, new Path(args[0]));
            FileInputFormat.addInputPath(job, new Path(args[1]));
            FileOutputFormat.setOutputPath(job, new Path(args[2]));
            job.setInputFormatClass(SequenceFileInputFormat.class); // TODO change to sequence when run  not localy GOTO "Reading the n-grams File" in assignment page
            job.setOutputFormatClass(TextOutputFormat.class);
            System.exit(job.waitForCompletion(true) ? 0 : 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void stopWordsInit(String [] arr, String filName){
        String filePath = FileSystems.getDefault().getPath(filName).toAbsolutePath().toString().replace("\\","\\\\")+".txt";
        File f=new File(filePath);

        BufferedReader reader = null;
        String word="";
        try {
            reader = new BufferedReader(new FileReader(f));
            word = null;
            word = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //___________as long as the line is not null, we haven't finished______________
        int i = 0;

        while (word != null) {
            arr[i] = word;
            try {
                word = reader.readLine();
            }catch(Exception e){
                e.printStackTrace();
            }
            i++;
        }
    }
}
