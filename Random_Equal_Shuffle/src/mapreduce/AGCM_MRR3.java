package mapreduce;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
//import org.apache.hadoop.mapred.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;

public class AGCM_MRR3 {
	static int numReduceTasks =7;
	static String password="xidian320";
/*job1*/
	 public static class MyMapper extends Mapper<LongWritable,Text,Text,Text>{
		@Override
		protected void setup(Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			super.setup(context);
		}
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String valueStr=value.toString();
			String [] values=valueStr.split("	");
			byte[] encryptK=JAES.encrypt(values[5], password);
			byte[] encryptV=JAES.encrypt("1", password);
			context.write(new Text(new String(JAES.parseByte2HexStr(encryptK))), new Text(new String(JAES.parseByte2HexStr(encryptV))));	
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
		protected void reduce(Text key, Iterable<Text>values,Context context) throws IOException, InterruptedException {
			int count=0;
			for(Text v:values){
				byte[] decryptV=JAES.decrypt(JAES.parseHexStr2Byte(v.toString()), password);
				String s =new String(decryptV).trim();
				count+=Integer.parseInt(s);
			}
			byte[] encryptV=JAES.encrypt(String.valueOf(count), password);
			context.write(key, new Text(new String(JAES.parseByte2HexStr(encryptV))));	
		}	
		@Override
		protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			int N=50;
			for(int i=0;i<N;i++){
				byte[] encryptK=JAES.encrypt("FAKE_"+i, password);
				byte[] encryptV=JAES.encrypt("0", password);
				context.write(new Text(new String(JAES.parseByte2HexStr(encryptK))), new Text(new String(JAES.parseByte2HexStr(encryptV))));
			}
			super.cleanup(context);
		}
	}
	static class MyCombiner extends Reducer<Text,Text,Text,Text>{
		
		@Override
		protected void reduce(Text key, Iterable<Text> values,Context context) throws IOException, InterruptedException {
			int count=0;
			for(Text v:values){
				count+=1;
			}
			byte[] encryptV=JAES.encrypt(String.valueOf(count), password);
			context.write(key, new Text(new String(JAES.parseByte2HexStr(encryptV))));
		}
	}
	static class MyPartitioner extends HashPartitioner<Text,Text>{
		@Override
		public int getPartition(Text key, Text value, int numReduceTasks) {
			int s = (int)(Math.random()*numReduceTasks);
			return s;
		}
	}
/*job2*/
	 public static class MyMapper2 extends Mapper<LongWritable,Text,Text,Text>{
			@Override
			protected void setup(Mapper<LongWritable, Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				// TODO Auto-generated method stub
				super.setup(context);
			}
			@Override
			protected void map(LongWritable key, Text value, Context context)
					throws IOException, InterruptedException {
				String[] split=value.toString().split("	");
				context.write(new Text(split[0]),new Text(split[1]));
			}
		}
		public static class ShuffleReduce2 extends Reducer<Text,Text,Text,Text>{
			@Override
			protected void setup(Reducer<Text,Text, Text, Text>.Context context)
					throws IOException, InterruptedException {
				// TODO Auto-generated method stub
				super.setup(context);
			}
			@Override
			protected void reduce(Text key, Iterable<Text>values,Context context) throws IOException, InterruptedException {
				int count=0;
				for(Text v:values){
					byte[] value=JAES.decrypt(JAES.parseHexStr2Byte(v.toString()), password);
					String valueStr=new String(value).trim();
					count+=Integer.parseInt(valueStr);
				}
				if(count!=0){
					byte[] k=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
					context.write(new Text(new String(k).trim().replace("\"", "")), new Text(String.valueOf(count)));
				}
			}
	
		}
		static class MyPartitioner2 extends HashPartitioner<Text,Text>{
			@Override
			public int getPartition(Text key, Text value, int numReduceTasks) {
				byte[] k=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
				String keyStr=new String(k).trim();
				return (keyStr.hashCode()&Integer.MAX_VALUE)%numReduceTasks;
				
				
			}
		}
		
    public static void main(String[] args) throws IOException,URISyntaxException, ClassNotFoundException, InterruptedException{
    	long startTime=System.currentTimeMillis();
    	//��ȡ���ö�����Ϣ
    	Configuration conf = new Configuration();
//job1���� 	
//    	Job job1 =Job.getInstance(conf,"job1");
    	Job job1 =new Job();
    	
    	//����job����������
    	job1.setJarByClass(AGCM_MRR3.class);
    	FileInputFormat.setInputPaths(job1, new Path(args[0]));
    	//��map�׶ν�������
    	job1.setMapperClass(MyMapper.class);
    	job1.setMapOutputKeyClass(Text.class);
    	job1.setMapOutputValueClass(Text.class);
    	//Partition
    	job1.setCombinerClass(MyCombiner.class);
    	job1.setPartitionerClass(MyPartitioner.class);
    	//��reduce�׶����á�
    	job1.setReducerClass(ShuffleReduce.class);
    	job1.setOutputKeyClass(Text.class);
    	job1.setNumReduceTasks(7);//reduce �����趨
    	job1.setOutputValueClass(Text.class);
    	//�ύjob
    	FileOutputFormat.setOutputPath(job1, new Path(args[1]));
    	//����������
        ControlledJob ctrlJob1= new ControlledJob(conf);
        ctrlJob1.setJob(job1);
//job2����
//        Job job2 =Job.getInstance(conf,"job2");
        Job job2 =new Job();
    	//����job����������
        job2.setJarByClass(AGCM_MRR3.class);
    	FileInputFormat.setInputPaths(job2, new Path(args[1]));
    	//��map�׶ν�������
    	job2.setMapperClass(MyMapper2.class);
    	job2.setMapOutputKeyClass(Text.class);
    	job2.setMapOutputValueClass(Text.class);
    	//Partition
    	job2.setPartitionerClass(MyPartitioner2.class);
    	//��reduce�׶����á�
    	job2.setReducerClass(ShuffleReduce2.class);
    	job2.setOutputKeyClass(Text.class);
    	job2.setNumReduceTasks(7);//reduce �����趨
    	job2.setOutputValueClass(Text.class);
    	//�ύjob
    	FileOutputFormat.setOutputPath(job2, new Path(args[2]));
    	//����������
        ControlledJob ctrlJob2= new ControlledJob(conf);
        ctrlJob2.setJob(job2);
// //������ҵ֮���ϵ��job2����job1���
//        ctrlJob2.addDependingJob(ctrlJob1);
// //������������������job1��job2������ҵ
//        JobControl jobCtrl= new JobControl("myCtrl");
//        jobCtrl.addJob(ctrlJob1);
//        jobCtrl.addJob(ctrlJob2);
//    	
//        //���߳�������
//        Thread thread = new Thread(jobCtrl);
//        thread.start();
//        while (true) {
//            if (jobCtrl.allFinished()) {
//                System.out.println(jobCtrl.getSuccessfulJobList());
//                jobCtrl.stop();
//                break;
//            }
//        }
        if (job1.waitForCompletion(true)) {
            System.exit(job2.waitForCompletion(true) ? 0 : 1);
 	       }
    	long endTime=System.currentTimeMillis();
    	System.out.println("����ʱ�䣺"+(endTime-startTime)+"ms");
    }
}
