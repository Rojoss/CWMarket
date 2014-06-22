package com.clashwars.cwmarket.config;

import com.clashwars.cwmarket.sql.SqlInfo;

public class Config {

	private SqlInfo sqlInfo;
	private int tradeSize;
	
	
	public SqlInfo getSqlInfo() {
		return sqlInfo;
	}

	public void setSqlInfo(SqlInfo sqlInfo) {
		this.sqlInfo = sqlInfo;
	}
	
	
	public int getTradeSize() {
		return tradeSize;
	}
	
	public void setTradeSize(int size) {
		this.tradeSize = size;
	}
}
