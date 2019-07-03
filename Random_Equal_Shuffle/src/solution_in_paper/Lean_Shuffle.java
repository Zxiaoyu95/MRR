package solution_in_paper;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;

import MRR_Solution.JAES;

public class Lean_Shuffle {
	static int numReduceTasks =7;
	static String password="xidian320";
	static byte[] encryptV=JAES.encrypt("1", password);
	static ArrayList<String> passenN = new ArrayList<String>();
	static ArrayList<String> S_passenN = new ArrayList<String>();
	 public static class MyMapper extends Mapper<LongWritable,Text,Text,Text>{
			@Override
			protected void setup(Mapper<LongWritable, Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				passenN.add("9");passenN.add("6");passenN.add("5");passenN.add("4");
				passenN.add("3");passenN.add("2");passenN.add("1");passenN.add("0");
				passenN.add("208");
				super.setup(context);
			}
			@Override
			protected void map(LongWritable key, Text value, Context context)
					throws IOException, InterruptedException {
				String valueStr=value.toString();
				String [] values=valueStr.split("	");
				context.write(new Text(values[7]), new Text(new String(JAES.parseByte2HexStr(encryptV))));	
			}
			@Override
			protected void cleanup(Mapper<LongWritable, Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				// TODO Auto-generated method stub
				super.cleanup(context);
				
			}
		}
		public static class ShuffleReduce extends Reducer<Text,Text,Text,Text>{
			@Override
			protected void setup(Reducer<Text,Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				// TODO Auto-generated method stub
				super.setup(context);
			
			}
			@Override
			protected void reduce(Text key, Iterable<Text>values,Context context) throws IOException, InterruptedException {	int count=0;
			int sum = 0;
			for(Text v:values){
				byte[] value=JAES.decrypt(JAES.parseHexStr2Byte(v.toString()), password);
				String valueStr=new String(value).trim();
				sum+=Integer.parseInt(valueStr);
			}
			if(sum !=0){
				byte[] k=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
				context.write(new Text(new String(k).trim().replace("\"", "")), new Text(String.valueOf(sum)));
				}
			
			}	
			@Override
			protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				
				super.cleanup(context);
				
			}
		}
		static class MyCombiner extends Reducer<Text,Text,Text,Text>{
			@Override
			protected void setup(Reducer<Text, Text, Text, Text>.Context context) throws IOException, InterruptedException {
				// TODO Auto-generated method stub
				super.setup(context);
				
			}
			@Override
			protected void reduce(Text key, Iterable<Text> values,Context context) throws IOException, InterruptedException {
				int count=0;
				for(Text v:values){
					count+=1;
				}
				byte[] encryptV=JAES.encrypt(String.valueOf(count), password);
				byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
				String s =new String(decryptK).trim();
				if(! S_passenN.contains(s)){
					S_passenN.add(s);
				}
				context.write(key, new Text(new String(JAES.parseByte2HexStr(encryptV))));
			}
			@Override
			protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				 for (String entry : passenN){
					if(! S_passenN.contains(entry)){
						byte[] encryptK=JAES.encrypt(entry, password);
						byte[] encryptV=JAES.encrypt("0", password);
						context.write(new Text(new String(JAES.parseByte2HexStr(encryptK))), new Text(new String(JAES.parseByte2HexStr(encryptV))));
					}
				}
				super.cleanup(context);
				
			}
		}
		static class MyPartitioner extends HashPartitioner<Text,Text>{
			@Override
			public int getPartition(Text key, Text value, int numReduceTasks) {
				byte[] k=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
				String keyStr=new String(k).trim();
				return (keyStr.hashCode()&Integer.MAX_VALUE)%numReduceTasks;
			}
		}
		   public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
		    	long startTime=System.currentTimeMillis();
		    	//��ȡ���ö�����Ϣ
		    	Configuration conf = new Configuration();
//		    	Job job =Job.getInstance(conf,"mapreduce");
		    	Job job =new Job();
		    	//����job����������
		    	job.setJarByClass(Lean_Shuffle.class);
		    	//��map�׶ν�������
		    	job.setMapperClass(MyMapper.class);
		    	job.setMapOutputKeyClass(Text.class);
		    	job.setMapOutputValueClass(Text.class);
		    	FileInputFormat.addInputPath(job, new Path(args[0]));
		    	//��reduce�׶����á�
		    	job.setCombinerClass(MyCombiner.class);
		    	job.setReducerClass(ShuffleReduce.class);
		    	job.setPartitionerClass(MyPartitioner.class);
		    	job.setOutputKeyClass(Text.class);
		    	job.setNumReduceTasks(7);//reduce �����趨
		    	job.setOutputValueClass(Text.class);
		    	FileOutputFormat.setOutputPath(job, new Path(args[1]));
		    	//�ύjob
		    	int isok = job.waitForCompletion(true)? 0 : 1;
		    	//�˳�
		    	long endTime=System.currentTimeMillis();
		    	System.out.println("����ʱ�䣺"+(endTime-startTime)+"ms");
		    	System.exit(isok);
		    }
}
