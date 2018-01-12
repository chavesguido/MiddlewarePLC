package com.coiron.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PLC {

	private String id;
	@JsonIgnore
	private String ip;
	private Map<String, String> variables;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Map<String, String> getVariables() {
		return variables;
	}
	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}
	
	
}
