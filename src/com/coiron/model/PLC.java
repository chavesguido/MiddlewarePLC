package com.coiron.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
	
	public PLC(String ip, String protocol) {
		try {
			variables = new HashMap<String, String>();
			this.ip = protocol + ip;
			login();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public PLC(){}

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
			webserver = new NetConnection(	ip ,
											PropertiesUtils.getFormAction(),
											PropertiesUtils.getPLCAdminURL(),
											PropertiesUtils.getUsernamePLC(),
											PropertiesUtils.getPasswordPLC());
//			webserver = new NetConnection(	ip + PropertiesUtils.getPLCAdminURL(),
//					PropertiesUtils.getFormId(),
//					PropertiesUtils.getUsernamePLC(),
//					PropertiesUtils.getPasswordPLC(),
//					ip + PropertiesUtils.getFormAction(),
//					PropertiesUtils.getUsernameFormId(),
//					PropertiesUtils.getPasswordFormId());
	}
	
	
	public void synchronize() throws Exception {
		
//		this.variables.put("Temperatura", String.valueOf( (Math.random() * 10) + 12 ));
//		this.variables.put("Luz", String.valueOf( (Math.random() * 50) + 250 ));
//		this.variables.put("Evaporación mínima", String.valueOf( (Math.random() * 100) + 800 ));
//		this.variables.put("Evaporación máxima", String.valueOf( (Math.random() * 100) + 400 ));
		
		System.out.println("sincronizando plc " + this.ip);
		if(webserver == null) {
			System.out.println("PLC sin conexión. Logueando...");
			login();
		}
		
		String html = this.webserver.getPageContent( this.ip + PropertiesUtils.getPLCWebServerURL() );
		
		if( !html.contains("PLC WebServer") ) {
			throw new Exception("DELETE PLC");
		}

		Document doc = Jsoup.parse(html);
		
		Elements variableElements = doc.getElementsByTag("div");
		
		for(Element variableElement : variableElements) {
			String key = variableElement.getElementsByClass("key").get(0).text();
			String value;
			
			if( !ip.contains("localhost") && !ip.contains("127.0.0.1") )
				value = variableElement.getElementsByClass("value").get(0).text();
			
			else //Si el plc esta en localhost es MOCK, genero valores aleatorios
				try {
					value = String.valueOf(
											Integer.parseInt(
													variableElement.getElementsByClass("value").get(0).text()
												) + new Random().nextInt(20)
							);
				} catch(NumberFormatException e) {
					value = variableElement.getElementsByClass("value").get(0).text();
				}
			
			this.variables.put(key, value);
		}
		
		
		
		//System.out.println(this.variables.toString());
		
	}

	public void updateWebServer(Map<String, String> vars) throws Exception {
		
		if( webserver == null)
			login();
		
		String url = this.ip + PropertiesUtils.getPLCWebServerURL();
		String postParams = "";
		
		for(String key : vars.keySet()) {
			
			String value = vars.get(key);
			postParams += formatVariableName(key) + "=" + value + "&";
		}
		
		postParams = postParams.substring(0, postParams.length() - 1);
		
		
		System.out.println("Editando variables: POST a " + ip + " con parametros " + postParams);
//		this.webserver.sendPost(url, postParams, false);
		webserver.editVar(postParams);
	}

	private String formatVariableName(String key) {
		String[] keySplitted = key.split("\\.");
		return "%22" + keySplitted[0] + "%22." + keySplitted[1];
	}

	@Override
	public String toString() {
		return "PLC [id=" + id + ", ip=" + ip + ", webserver=" + webserver + ", variables=" + variables + "]";
	}
	
	
}
