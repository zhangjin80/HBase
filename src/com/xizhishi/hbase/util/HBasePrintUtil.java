package com.xizhishi.hbase.util;

import java.util.List;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;

public class HBasePrintUtil {

	public static void printResultScanner(ResultScanner resultScann) {
		for (Result result : resultScann) {
			printResult(result);
		}
	}

	public static void printResult(Result result) {
		List<Cell> cells = result.listCells();
		for (int i = 0; i < cells.size(); i++) {
			Cell cell = cells.get(i);
			printCell(cell);
		}
	}

	public static void printCell(Cell cell) {
		System.out.println(Bytes.toString(cell.getRow()) + "\t" + Bytes.toString(cell.getFamily()) + "\t" + Bytes.toString(cell.getQualifier())
				+ "\t" + Bytes.toString(cell.getValue()) + "\t" + cell.getTimestamp());
	}

	public static void printKeyValye(KeyValue kv) {
		System.out.println(Bytes.toString(kv.getRow()) + "\t" + Bytes.toString(kv.getFamily()) + "\t" + Bytes.toString(kv.getQualifier()) + "\t"
				+ Bytes.toString(kv.getValue()) + "\t" + kv.getTimestamp());
	}
}
