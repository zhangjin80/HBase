package com.xizhishi.hbase.mr;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import net.spy.memcached.transcoders.IntegerTranscoder;

/**
 *从HBase中读取Student表中的info簇，和age值，
 *求出平均age 
 */
public class ReadDataFromHBaseToHDFSMR extends Configured implements Tool{
	public static void main(String[] args) throws Exception {
		int run = ToolRunner.run(new ReadDataFromHBaseToHDFSMR(), args);
		System.exit(run);
	}
	
	@Override
	public int run(String[] arg0) throws Exception {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "hadoop02:2181,hadoop03:2181,hadoop04:2181");
		conf.set("fs.defaultFS", "hdfs://myha01/");
		conf.addResource("config/core-site.xml");
		conf.addResource("config/hdfs-site.xml");
		System.setProperty("HADOOP_USER_NAME", "hadoop");
		Job job = Job.getInstance(conf, "ReadDataFromHBaseToHDFSMR");
		job.setJarByClass(ReadDataFromHBaseToHDFSMR.class);
		
		Scan scan = new Scan();
		scan.addColumn("info".getBytes(), "age".getBytes());
		//此处设置了Mapper和输入出组件
		TableMapReduceUtil.initTableMapperJob(
				"student".getBytes(),//指定表名
				scan,//指定扫描对象
				ReadDataFromHBaseToHDFS_Mapper.class,
				Text.class,//outputKeyClass Mapper阶段输出的outKey
				IntWritable.class,//outputValueClass Mapper阶段输出的value
				job,
				false);
		/**
		 * 在当前的MR程序中， 输入的数据是来自于 HBase，  按照常理来说，需要自定义一个数据读取组件读 hbase。
		 * TableMapReduceUtil.initTableMapperJob这个方法已经把这件事做了。
		 */
		/* *
		 * 注意：以上设置已经包含以下操作不用再进行设置
		job.setMapperClass(ReadDataFromHBaseToHDFS_Mapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		*/
		
		//设置reducer类和输出路径
		job.setReducerClass(ReadDataFromHBaseToHDFS_Reducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(DoubleWritable.class);
		
		FileOutputFormat.setOutputPath(job,new Path("/output/student6/"));
		
		boolean isDone = job.waitForCompletion(true);
		return isDone?0:1;
	} 
	
	
	public class ReadDataFromHBaseToHDFS_Mapper extends TableMapper<Text, IntWritable>{
		Text outKey = new Text("age");
		@Override
		protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
			//value就是HBase中一行记录，key就是rowkey
			//判断这一行是否包含info簇和age列，如果包含取出age值，以age做key输出。
			boolean containsColumn = value.containsColumn("info".getBytes(), "age".getBytes());
			if(containsColumn){
				List<Cell> cells = value.getColumnCells("info".getBytes(), "age".getBytes());
				Cell cell = cells.get(0);
				byte[] cloneValue = CellUtil.cloneValue(cell);
				String age = new String(cloneValue);
				context.write(outKey, new IntWritable(Integer.parseInt(age)));
			}
		}
	}
	public class ReadDataFromHBaseToHDFS_Reducer extends Reducer<Text, IntWritable, Text, DoubleWritable>{
		@Override
		protected void reduce(Text key, Iterable<IntWritable> values,Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			int count = 0;
			//求平均分
			for(IntWritable age : values){
				count++;
				sum += age.get();
			}
			double average = sum*1D/count;
			context.write(new Text("average_age"), new DoubleWritable(average));
		}
	}
}
