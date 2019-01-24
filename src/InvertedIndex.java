
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.*;

public class InvertedIndex {

 public static class TokenizerMapper
 extends Mapper < LongWritable, Text, Text, Text > {

  private Text idofdocuments = new Text();
  private Text word = new Text();

  public void map(LongWritable key, Text value, Context context) throws IOException,
  InterruptedException {
	 
	  String[] splitT = value.toString().split("\\t");
	  idofdocuments.set(splitT[0]);
	    String line = value.toString().toLowerCase().replaceAll("\\t", " ");
	    line = line.replaceAll("[^a-z]", " ");
	    StringTokenizer tokensfordoc = new StringTokenizer(line, " ");
   while (tokensfordoc.hasMoreTokens()) {
    word.set(tokensfordoc.nextToken());
    context.write(word, idofdocuments);
   }
  }
 }

 public static class IntSumReducer
 extends Reducer < Text, Text, Text, Text > {



  public void reduce(Text key, Iterable < Text > values,
   Context context
  ) throws IOException,
  InterruptedException {

   Iterator < Text > itr = values.iterator();
   HashMap < String, Integer > hmap = new HashMap();
   
   while (itr.hasNext()) {
	   String keyString = itr.next().toString();
    Integer hashvalue = hmap.get(keyString);
    if (hashvalue == null) {
     hmap.put(keyString, 1);
    } else {
     hmap.put(keyString, hashvalue+1);
    }
   }
   StringBuffer sb = new StringBuffer();
   Iterator it = hmap.entrySet().iterator();
   while (it.hasNext()) {
       Map.Entry pair = (Map.Entry)it.next();
       sb.append(pair.getKey() + ":" +	pair.getValue() + "\t");
       it.remove();
   }
   context.write(key, new Text(sb.toString()));
  }
 }

 public static void main(String[] args) throws Exception {
  Configuration conf = new Configuration();
  Job job = Job.getInstance(conf, "Inverted Index");
  job.setJarByClass(InvertedIndex.class);
  job.setMapperClass(TokenizerMapper.class);
  job.setReducerClass(IntSumReducer.class);
  job.setOutputKeyClass(Text.class);
  job.setOutputValueClass(Text.class);
  FileInputFormat.addInputPath(job, new Path(args[0]));
  FileOutputFormat.setOutputPath(job, new Path(args[1]));
  System.exit(job.waitForCompletion(true) ? 0 : 1);
 }
}
