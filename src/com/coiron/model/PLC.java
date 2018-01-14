package com.coiron.model;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.coiron.connections.NetConnection;
import com.coiron.utils.PropertiesUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class PLC {

	private String idPlc;
	@JsonIgnore
	private String ip;
	@JsonIgnore
	private NetConnection webserver;
	private Map<String, String> variables;
	
	public PLC() {
		variables = new HashMap<String, String>();
	}
	
	
	public String getIdPlc() {
		return idPlc;
	}
	public void setIdPlc(String idPlc) {
		this.idPlc = idPlc;
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
	public NetConnection getWebserver() {
		return webserver;
	}
	public void setWebserver(NetConnection webserver) {
		this.webserver = webserver;
	}
	public void synchronize() throws Exception {
		this.variables.put("key1", "value1");
		this.variables.put("key2", "value2");
		this.variables.put("key3", "value3");
		this.variables.put("key4", "value4");
		
		
//		String html = this.webserver.getPageContent( this.ip + PropertiesUtils.getPLCWebServerURL() );
//
//		Document doc = Jsoup.parse(html);
//
//		Elements variableElements = doc.getElementsByTag("div");
//		
//		for(Element variableElement : variableElements) {
//			String key = variableElement.getElementsByClass("key").get(0).text();
//			String value = variableElement.getElementsByClass("value").get(0).text();
//			
//			this.variables.put(key, value);
//		}
		
		
	}

	public void updateWebServer(String key) throws Exception {
		String value = this.variables.get(key);
		
		String url = this.ip + PropertiesUtils.getPLCWebServerURL();
		String postParams = key + "=" + value;
		
		this.webserver.sendPost(url, postParams);
	}
	
	
}
