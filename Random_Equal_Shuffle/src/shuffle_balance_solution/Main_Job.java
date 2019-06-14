package shuffle_balance_solution;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
public class Main_Job {
	 static Map<String,Float> PickUp_M = new HashMap<String,Float>();//pickup���ʷֲ�map
	 static Map<String,Float> PassenN_M = new HashMap<String,Float>();//passenN���ʷֲ�map
	 static Map<Integer,Float> PassenN_Reducer_M = new HashMap<Integer,Float>();//��ʼreducer��Ӧ����map
	 static Map<String,Integer> Key_Reducer_M = new HashMap<String,Integer>(); //key��Ӧreducer��map
	 static float PassenN_Max_f = (float) 0.71;
	 static String password="xidian320";
	 static int numReduceTasks = 2;
	 static String staticStr = "";
	 static int MapSum = 0;
	 static Map<String,Integer> map=new HashMap<String, Integer>();
	 static int CountAll = 0;
	 static float key_f;
	 public static class MyMapper extends Mapper<LongWritable,Text,Text,Text>{
		 @Override
		protected void setup(Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			PassenN_M.put("0",(float) 0.00);PassenN_M.put("1",(float) 0.71);PassenN_M.put("2",(float) 0.13);PassenN_M.put("3",(float) 0.04);
			PassenN_M.put("4",(float) 0.02);PassenN_M.put("5",(float) 0.06);PassenN_M.put("6",(float) 0.04);
			for(int i=0;i<numReduceTasks;i++){
				PassenN_Reducer_M.put(i,PassenN_Max_f);
			}
			Key_Reducer_M.put("key",321);
			super.setup(context);
		}
		@Override
		protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			String valueStr=value.toString();
			String [] values=valueStr.split("	");
			String keyV = values[0];
			String valuesV = values[1];	
			byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(keyV), password);
			String keyStr=new String(decryptK).trim().replace("\"", "");			
			if(Key_Reducer_M.containsKey(keyStr)){
	              
			}
			else if (!Key_Reducer_M.containsKey(keyStr)){
				key_f=PassenN_M.get(keyStr);
				for(Map.Entry<Integer, Float> entry : PassenN_Reducer_M.entrySet()){
					if(entry.getValue() >= key_f ){
						PassenN_Reducer_M.put(entry.getKey(),entry.getValue()-key_f);//����map
						Key_Reducer_M.put(keyStr,entry.getKey());
						break;
					}
					else{
						continue;
					}
				}
				
			}
			context.write(new Text(keyV), new Text(valuesV));
		}
		@Override
		protected void cleanup(Mapper<LongWritable, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			super.cleanup(context);
		}
	}
	public static class ShuffleReduce extends Reducer<Text,Text,Text,Text>{
		@Override
		protected void setup(Reducer<Text, Text, Text, Text>.Context context) throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			super.setup(context);
		}
		@Override
		protected void reduce(Text key, Iterable<Text>values,Context context) throws IOException, InterruptedException {
			int sum=0;
			for(Text i:values){
				byte[] value=JAES.decrypt(JAES.parseHexStr2Byte(i.toString()), password);
				String valueStr=new String(value).trim();
				sum+=Integer.valueOf(valueStr);
			}
			byte[] keyB=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
			String valueStr=new String(keyB).trim();
			context.write(new Text(valueStr.replace("\"", "")), new Text(String.valueOf(sum)));
			}
		@Override
		protected void cleanup(Reducer<Text, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			super.cleanup(context);
		}
		
	}
	static class MyPartitioner extends HashPartitioner<Text,Text>{
		@Override
		public int getPartition(Text key, Text value, int numReduceTasks) {
			byte[] decryptK=JAES.decrypt(JAES.parseHexStr2Byte(key.toString()), password);
			String keyStr=new String(decryptK).trim().replace("\"", "");
			int R = 0;
			//�ж��Ƿ����key
//			if(Key_Reducer_M.containsKey(keyStr)){
				R=Key_Reducer_M.get(keyStr);
//			}
			return R;
		}
	}
   public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
   	long startTime=System.currentTimeMillis();
   	//��ȡ���ö�����Ϣ
   	Configuration conf = new Configuration();
   	Job job =new Job();
   	//����job����������
   	job.setJarByClass(Main_Job.class);
   	//��map�׶ν�������
   	job.setMapperClass(MyMapper.class);
   	job.setMapOutputKeyClass(Text.class);
   	job.setMapOutputValueClass(Text.class);
   	FileInputFormat.addInputPath(job, new Path(args[0]));
   	//��reduce�׶����á�
   	job.setReducerClass(ShuffleReduce.class);
   	job.setPartitionerClass(MyPartitioner.class);
   	job.setOutputKeyClass(Text.class);
   	job.setNumReduceTasks(2);//reduce �����趨
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
