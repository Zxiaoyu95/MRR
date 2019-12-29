package MRR_Solution;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.Key;

import org.apache.commons.codec.binary.Base64;
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

public class AGCM_MRR2 {
	static int numReduceTasks =7;
	static String password="Xidian";
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
			byte[] encryptK=GCM.encrypt(values[5],password);
			byte[] encryptV=GCM.encrypt("1",password);
			context.write(new Text(Base64.encodeBase64String(encryptK)), new Text(Base64.encodeBase64String(encryptV)));
			
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
				byte[] decryptV=GCM.decrypt(Base64.decodeBase64(v.toString()),password);
				String s=new String(decryptV);
				count+=Integer.parseInt(s);
			}
			byte[] encryptV=GCM.encrypt(String.valueOf(count),password);
			context.write(key, new Text(Base64.encodeBase64String(encryptV)));	
		}	
	}
	static class MyCombiner extends Reducer<Text,Text,Text,Text>{
		
		@Override
		protected void reduce(Text key, Iterable<Text> values,Context context) throws IOException, InterruptedException {
			int count=0;
			for(Text v:values){
				count+=1;
			}
			byte[] encryptV=GCM.encrypt(String.valueOf(count),password);
			context.write(key, new Text(Base64.encodeBase64String(encryptV)));
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
				byte[] decryptK=GCM.decrypt(Base64.decodeBase64(split[0]),password);	
				byte[] decryptV=GCM.decrypt(Base64.decodeBase64(split[1]),password);	
				String keyStr=new String(decryptK);
				String valueStr=new String(decryptV);
				int r=(keyStr.hashCode()&Integer.MAX_VALUE)%numReduceTasks;
				int s = (int)(Math.random()*numReduceTasks);
				byte[] encryptK=GCM.encrypt(keyStr, password);
				byte[] encryptV=GCM.encrypt("TURE_"+valueStr+"#"+r, password);
				context.write(new Text(Base64.encodeBase64String(encryptK)),new Text(Base64.encodeBase64String(encryptV)));
				if(s<(numReduceTasks/2)){
					int p = (int)(Math.random()*numReduceTasks);
					byte[] encryptF=GCM.encrypt("FAKE_"+0+"#"+p, password);
					context.write(new Text(Base64.encodeBase64String(encryptK)),new Text(Base64.encodeBase64String(encryptF)));
				}
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
					byte[] value=GCM.decrypt(Base64.decodeBase64(v.toString()), password);
					String valueStr=new String(value);
					count+=Integer.parseInt(valueStr.substring(valueStr.indexOf("_")+1,valueStr.indexOf("#")));
				}
				if(count!=0){
					byte[] k=GCM.decrypt(Base64.decodeBase64(key.toString()), password);
					context.write(new Text(new String(k).replace("\"", "")), new Text(String.valueOf(count)));
				}
			}
	
		}
		static class MyPartitioner2 extends HashPartitioner<Text,Text>{
			

			@Override
			public int getPartition(Text key, Text value, int numReduceTasks) {
				byte[] v=GCM.decrypt(Base64.decodeBase64(value.toString()), password);
				String vuleStr=new String(v);
				return Integer.parseInt(vuleStr.substring(vuleStr.indexOf("#")+1,vuleStr.length()));
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
    	job1.setJarByClass(AGCM_MRR2.class);
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
        job2.setJarByClass(AGCM_MRR2.class);
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

	
