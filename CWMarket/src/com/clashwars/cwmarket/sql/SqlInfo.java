package com.clashwars.cwmarket.sql;

public class SqlInfo {
	private String	address;
	private String port;
	private String	user;
	private String	pass;
	private String	db;

	public SqlInfo(String address, String port, String user, String pass, String db) {
		this.address = address;
		this.port = port;
		this.user = user;
		this.pass = pass;
		this.db = db;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}
}
