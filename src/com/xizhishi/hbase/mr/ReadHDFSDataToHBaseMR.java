package com.xizhishi.hbase.mr;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * 从HDFS把Student文件，读数据到HBase中
 */
public class ReadHDFSDataToHBaseMR extends Configured implements Tool {
	private final String  TABLE_NAME= "student";
	public static void main(String[] args) throws Exception {
		int run = ToolRunner.run(new ReadHDFSDataToHBaseMR(), args);
		System.exit(run);
	}

	@Override
	public int run(String[] arg0) throws Exception {

		Configuration conf = HBaseConfiguration.create();
		// 设置zookeeper地址
		conf.set("hbase.zookeeper.quorum", "hadoop02:2181,hadoop03:2181,hadoop04:2181");
		// 设置高可用的hdfs地址
		conf.set("fs.defaultFS", "hdfs://myha01/");
		// 加载配置文件
		conf.addResource("config/core-site.xml");
		conf.addResource("config/hdfs-site.xml");
		// 设置hadoop用户名
		System.setProperty("HADOOP_USER_NAME", "hadoop");
		Job job = Job.getInstance(conf, "ReadHDFSDataToHBaseMR");
		job.setJarByClass(ReadHDFSDataToHBaseMR.class);

		job.setMapperClass(HBase_Mapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(NullWritable.class);
		
		
		// 以下这段代码为创建表的代码
		Connection con = ConnectionFactory.createConnection(conf);
		// 根据连接获取到一个管理员对象
		Admin admin = con.getAdmin();
		TableName tn = TableName.valueOf(TABLE_NAME);
		if(!admin.tableExists(tn)){//表不存在则创建表
			HTableDescriptor htd = new HTableDescriptor(tn);
			HColumnDescriptor cf = new HColumnDescriptor("info");
			htd.addFamily(cf);
			admin.createTable(htd);
		}

		// 等同于job.setOutputFormatClass(cls);
		// 设置TableReducerJob,如果在本地pc测试运行使用本地jar包，就需要把最后一个参数配置成false
		TableMapReduceUtil.initTableReducerJob(TABLE_NAME, HBase_Reducer.class, job, null, null, null, null, false);
		// 如在集群上跑设置以下参数就可以
		// TableMapReduceUtil.initTableReducerJob("student", HBase_Reduce.class,
		// job);
		job.setReducerClass(HBase_Reducer.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Put.class);

		FileInputFormat.addInputPath(job, new Path("/input/student/"));
		boolean isDone = job.waitForCompletion(true);
		return isDone ? 0 : 1;
	}

	public static class HBase_Mapper extends Mapper<LongWritable, Text, Text, NullWritable> {
		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			context.write(value, NullWritable.get());
		}
	}

	public static class HBase_Reducer extends TableReducer<Text, NullWritable, NullWritable> {
		@Override
		protected void reduce(Text key, Iterable<NullWritable> values, Context context)
				throws IOException, InterruptedException {
			// student的内容格式：95007,易思玲,女,19,MA
			String[] split = key.toString().split(",");
			// 设置第一列内容95007位rowKey
			Put put = new Put(split[0].getBytes());
			put.addColumn("info".getBytes(), "name".getBytes(), split[1].getBytes());
			put.addColumn("info".getBytes(), "sex".getBytes(), split[2].getBytes());
			put.addColumn("info".getBytes(), "age".getBytes(), split[3].getBytes());
			put.addColumn("info".getBytes(), "department".getBytes(), split[4].getBytes());
			context.write(NullWritable.get(), put);
		}
	}
}
