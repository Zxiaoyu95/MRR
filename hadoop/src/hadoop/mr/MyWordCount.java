package hadoop.mr;
/**���Էֲ�
 * age 0  0    *  MS 0 Married   * POB 0  U.S
 *     1  <13  *     1 Divorced  *     1  US Ter.
 *     2  <20  *     2 Widowed   *     2  Europe
 *     3  <30  *     3 Separated *     3  Asia
 *     4  <40  *     4 N.Married *     4  Americas
 *     5  <50  *                 *     5  Africa
 *     6  <65  *                 *     6  Oceania
 *     7  65+  *                 *
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

public class MyWordCount {
	
	public static class MyMapper extends Mapper<LongWritable,Text,Text,IntWritable>{
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			//��ȡ����  ÿһ�е���һ��map
			String line= value.toString();
			//�з�����
			String [] words =line.split("	");
			List<String> Age = new ArrayList<>();
			List<String>POB = new ArrayList<>();
			List<String> MS= new ArrayList<>();
			Age.add(words[0]);
			POB.add(words[1]);
			MS.add(words[2]);
			//ѭ������
			for(String word:MS){
				context.write(new Text(word), new IntWritable(1));
			}	
		}
		
	}
	public static class MyReducer extends Reducer<Text, IntWritable, Text, IntWritable>{
		@Override
		protected void reduce(Text key, Iterable<IntWritable> values,Context context) 
				throws IOException, InterruptedException {
		int counter =0;
		for(IntWritable i:values){
			counter+=i.get();
		      }
		context.write(key, new IntWritable(counter));
		}
	}
	//����
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
    	long startTime=System.currentTimeMillis();
    	//��ȡ���ö�����Ϣ
    	Configuration conf = new Configuration();
    	Job job =Job.getInstance(conf,"mywordcount");
    	//����job����������
    	job.setJarByClass(MyWordCount.class);
    	//��map�׶ν�������
    	job.setMapperClass(MyMapper.class);
    	job.setMapOutputKeyClass(Text.class);
    	job.setMapOutputValueClass(IntWritable.class);
    	FileInputFormat.addInputPath(job, new Path(args[0]));
    	//��reduce�׶����á�
    	job.setReducerClass(MyReducer.class);
    	job.setOutputKeyClass(Text.class);
    	job.setNumReduceTasks(3);
    	job.setOutputValueClass(IntWritable.class);
    	FileOutputFormat.setOutputPath(job, new Path(args[1]));
    	//�ύjob
    	int isok = job.waitForCompletion(true)? 0 : 1;
    	//�˳�
    	long endTime=System.currentTimeMillis();
    	System.out.println("����ʱ�䣺"+(endTime-startTime)+"ms");
    	System.exit(isok);
    }

}
