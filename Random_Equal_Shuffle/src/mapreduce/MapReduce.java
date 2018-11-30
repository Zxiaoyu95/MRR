package mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;

public class MapReduce {

	 public static class MyMapper extends Mapper<LongWritable,Text,Text,IntWritable>{
		@Override
		protected void setup(Mapper<LongWritable, Text, Text, IntWritable>.Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			super.setup(context);
		}
		@Override
		protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, IntWritable>.Context context)
				throws IOException, InterruptedException {
			String valueStr=value.toString();
			String [] values=valueStr.split("	");
			int r=(values[0].hashCode()&Integer.MAX_VALUE)%3;
			context.write(new Text(r+"_"+values[0].replace("\"", "")), new IntWritable(1));
	
		}
	}
	public static class ShuffleReduce extends Reducer<Text,IntWritable,Text,IntWritable>{
		@Override
		protected void setup(Reducer<Text, IntWritable, Text, IntWritable>.Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			super.setup(context);
		}
		@Override
		protected void reduce(Text key, Iterable<IntWritable>values,Context context) throws IOException, InterruptedException {
			int sum=0;
			for(IntWritable i:values){
				sum+=i.get();
			}
			context.write(key, new IntWritable(sum));
		
			}
		
	}
	static class MyCombiner extends Reducer<Text,IntWritable,Text,IntWritable>{
		@Override
		protected void reduce(Text key, Iterable<IntWritable> values,Context context) throws IOException, InterruptedException {
			int count=0;
			for(IntWritable n:values){
				count+=n.get();
			}
			context.write(key, new IntWritable(count));
		}
	}
	static class MyPartitioner extends HashPartitioner<Text,IntWritable>{
		@Override
		public int getPartition(Text key, IntWritable value, int numReduceTasks) {
			String keystr=key.toString();
			return Integer.parseInt(keystr.substring(0,keystr.indexOf("_")));
			
		}
	}
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
    	long startTime=System.currentTimeMillis();
    	//��ȡ���ö�����Ϣ
    	Configuration conf = new Configuration();
    	Job job =Job.getInstance(conf,"mapreduce");
    	//����job����������
    	job.setJarByClass(MapReduce.class);
    	//��map�׶ν�������
    	job.setMapperClass(MyMapper.class);
    	job.setMapOutputKeyClass(Text.class);
    	job.setMapOutputValueClass(IntWritable.class);
    	FileInputFormat.addInputPath(job, new Path(args[0]));
    	//��reduce�׶����á�
    	job.setCombinerClass(MyCombiner.class);
    	job.setReducerClass(ShuffleReduce.class);
    	job.setPartitionerClass(MyPartitioner.class);
    	job.setOutputKeyClass(Text.class);
    	job.setNumReduceTasks(3);//reduce �����趨
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

	
