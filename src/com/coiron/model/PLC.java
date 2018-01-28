package com.coiron.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.coiron.connections.NetConnection;
import com.coiron.utils.PropertiesUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class PLC {

	private String id = null;
	@JsonIgnore
	private String ip = null;
	@JsonIgnore
	private NetConnection webserver = null;
	private Map<String, String> variables = null;
	
	public PLC() {
		try {
			variables = new HashMap<String, String>();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
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
	public NetConnection getWebserver() {
		return webserver;
	}
	public void setWebserver(NetConnection webserver) {
		this.webserver = webserver;
	}
	
	public void login() throws Exception {
		if(ip != null)
			webserver = new NetConnection(ip + PropertiesUtils.getPLCAdminURL(), "Login_Area_Form", "Login", "Password");
	}
	
	
	public void synchronize() throws Exception {
		
//		this.variables.put("key1", "value1");
//		this.variables.put("key2", "value2");
//		this.variables.put("key3", "value3");
//		this.variables.put("key4", "value4");
		
		if( webserver == null)
			login();
		
		String html = this.webserver.getPageContent( this.ip + PropertiesUtils.getPLCWebServerURL() );

		Document doc = Jsoup.parse(html);

		Elements variableElements = doc.getElementsByTag("div");
		
		for(Element variableElement : variableElements) {
			String key = variableElement.getElementsByClass("key").get(0).text();
			String value = variableElement.getElementsByClass("value").get(0).text();
			
			this.variables.put(key, value);
		}
		
		System.out.println(this.variables.toString());
	}

	public void updateWebServer(Set<String> keys) throws Exception {
		
		if( webserver == null)
			login();
		
		String url = this.ip + PropertiesUtils.getPLCWebServerURL();
		String postParams = "";
		
		for(String key : keys) {
			
			String value = this.variables.get(key);
			postParams += key + "=" + value + "&";
		}
		
		postParams = postParams.substring(0, postParams.length() - 1);
		
		
		System.out.println("POST a " + url + " con parametros " + postParams);
		this.webserver.sendPost(url, postParams);
	}

	@Override
	public String toString() {
		return "PLC [id=" + id + ", ip=" + ip + ", webserver=" + webserver + ", variables=" + variables + "]";
	}
	
	
}
