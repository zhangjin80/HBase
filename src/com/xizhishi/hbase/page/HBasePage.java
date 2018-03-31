package com.xizhishi.hbase.page;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import com.xizhishi.hbase.util.HBasePrintUtil;
import com.xizhishi.hbase.util.HBaseUtil;
public class HBasePage {
	public static void main(String[] args) throws Exception {
		HBasePage hBasePage = new HBasePage();
		ResultScanner pageData = hBasePage.getPageData(3, 3);
		HBasePrintUtil.printResultScanner(pageData);
	}
	
	/**
	 * 实现思路：
	 * 	输入：页码，每页条数
	 * 	处理：
	 * 		1.把页码转换成starRowkey开始的行号，
	 * 		2.使用scan的setStartRow方法
	 * 		3.设置一个过滤器来限制scan的查询条数
	 * 	返回：一个结果集
	 */
	public ResultScanner getPageData(int pageIndex,int pageSize) throws Exception{
		if(pageSize<2||pageSize>15)
			pageSize = 5;
		//获取行号
		String startRow = getStarRowtFromPageIndex(pageIndex,pageSize);
		return getPageData(startRow,pageSize);
	}
	
	
	/**
	 * 通过startRow查询出当前页
	 * @param startRow
	 * @param pageSize
	 * @throws Exception 
	 */
	private ResultScanner getPageData(String startRow, int pageSize) throws Exception {
		//获取要查询的表
		Table table = HBaseUtil.getTable("user_info");
		Scan scan = new Scan();
		if(StringUtils.isNotBlank(startRow)){
			//开始扫描的startrow
			scan.setStartRow(startRow.getBytes());
		}
		//设置page过滤器，类似于limit
		Filter pageFilter = new PageFilter(pageSize);
		scan.setFilter(pageFilter);
		return table.getScanner(scan);
	}
	
	/**
	 * 通过页码获取开始的行号实现思想：
	 * 	1. 获取上一页的最后一条记录的行号，再在这条记录的rowkey上加上一个空字节，就可以得到当前页开始的rowkey
	 * 	2. 把每页的记录数pageSize加1，得到的上一页的最后一条数据就是当前页的第一条记录。
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws Exception 
	 */
	private String getStarRowtFromPageIndex(int pageIndex, int pageSize) throws Exception {
		if(pageIndex<=1){//小于等于1就放回null，默认就从第一行开始查询
			return null;
		}
		String startRow = null;
		for(int i = 1;i<pageIndex;i++){
			//方式1
//			ResultScanner pageData = getPageData(startRow,pageSize);
			//方式2
			ResultScanner pageData = getPageData(startRow,pageSize+1);
			Iterator<Result> iterator = pageData.iterator();
			Result re=null;
			while(iterator.hasNext()){
				re = iterator.next();
			}
			//方式一
//			String endRow = new String(re.getRow());
//			byte[] add = Bytes.add(endRow.getBytes(),new byte[]{0x00});
//			startRow = new String(add);
			//方式二
			startRow = new String(re.getRow());
		}
		return startRow;
	}

}
