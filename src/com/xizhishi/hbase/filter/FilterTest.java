package com.xizhishi.hbase.filter;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FamilyFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;

public class FilterTest {
	public static void main(String[] args) throws Exception {
		//1. 获取连接
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "hadoop02:2181,hadoop03:2181,hadoop04:2181");
		
		Connection con = ConnectionFactory.createConnection(conf);
		
		//2.获取一张表
		Table table = con.getTable(TableName.valueOf("user_info"));
		
		//3.准备一个扫描对象和过滤器
		Scan scaner = new Scan();
		//rowkey小于zhangsan_20180701_0002
		Filter filterRow = new RowFilter(CompareOp.LESS_OR_EQUAL, new BinaryComparator("zhangsan_20150701_0002".getBytes()));
		//列簇等于base_info
		Filter filterFamily  = new FamilyFilter(CompareOp.EQUAL, new BinaryComparator("base_info".getBytes()));
		Filter filters = new FilterList(filterRow,filterFamily); 
		//设置扫描组件的过滤器
		scaner.setFilter(filters);
		
		//4.对表进行扫描得到结果集
		ResultScanner rss = table.getScanner(scaner);
		for(Result rs:rss){
			for(Cell c: rs.listCells()){
				System.out.println(Bytes.toString(c.getRow())+","+Bytes.toString(c.getFamily())+","+Bytes.toString(c.getQualifier())+","+Bytes.toString(c.getValue()));
			}
		}
		
		//5.关闭连接
		con.close();
	}
}
