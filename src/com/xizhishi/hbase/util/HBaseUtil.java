package com.xizhishi.hbase.util;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;

public class HBaseUtil {
	private static final String ZK_CONNECT_KEY = "hbase.zookeeper.quorum";
	private static final String ZK_CONNECT_VALUE = "hadoop02:2181,hadoop03:2181,hadoop04:2181";

	public static Connection getConnenction(){
		Configuration conf = null;
		conf = HBaseConfiguration.create();
		conf.set(ZK_CONNECT_KEY, ZK_CONNECT_VALUE);
		Connection con = null;
		try {
			con = ConnectionFactory.createConnection(conf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return con;
	}
	public static Table getTable(String tableName){
		Connection con = getConnenction();
		Table table =null;
		try {
			table = con.getTable(TableName.valueOf(tableName.getBytes()));
		} catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
		}
		return table;
	}
}
