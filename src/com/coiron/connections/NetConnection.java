package com.coiron.connections;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.coiron.utils.PropertiesUtils;

public class NetConnection {

	private String loginURL;
	private String loginAction;
	private String loginFormID;
	private String usernameFormId;
	private String passwordFormId;
	private String username;
	private String password;
	private String ip;
	private String formAction;
	private String PLCAdminURL;
	
	private List<String> cookies = new ArrayList<String>();

	private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36";
	
	public NetConnection(String loginURL, String loginFormID,
							String username, String password, String loginAction,
							String usernameFormName, String passwordFormName) throws Exception {
		this.loginURL = loginURL;
		this.loginFormID = loginFormID;
		this.username = username;
		this.password = password;
		this.loginAction = loginAction;
		this.usernameFormId = usernameFormName;
		this.passwordFormId = passwordFormName;

		if( !loginURL.contains("localhost") && !loginURL.contains("127.0.0.1") )
			login();
	}
	
	public NetConnection(String ip, String formAction, String PLCAdminURL, String username, String password) {
		this.ip = ip;
		this.formAction = formAction;
		this.PLCAdminURL = PLCAdminURL;
		this.username = username;
		this.password = password;
		
		String data = "Redirection=&Login=" + username + "&Password=" + password;
		
		if( !ip.contains("localhost") && !ip.contains("127.0.0.1") )
			loginByCURL(ip, formAction, PLCAdminURL, data);
	}
	
	public NetConnection() {
		
	}
	
	public String getPageContent(String url) throws Exception {

		URL obj = new URL(url);
		
		HttpURLConnection conn = null;
		
		if(url.startsWith("http:"))
			conn = (HttpURLConnection) obj.openConnection();
		else if(url.startsWith("https:"))
			conn = (HttpsURLConnection) obj.openConnection();
		else throw new Exception("Not a valid HTTP URL");

		// default is GET
		conn.setRequestMethod("GET");

		conn.setUseCaches(false);

		// act like a browser
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		if (PropertiesUtils.getDebugLog()) {
			System.out.println("Cookies: " + cookies);
		}
		String cookie = "";
		for (String c : cookies) {
			cookie = cookie.concat(c + "; ");
		}
		if(cookie.length()>2) {
			cookie = cookie.substring(0, cookie.length()-2);
			conn.addRequestProperty("Cookie", cookie);
		}
		

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

//		if (this.cookies == null)
//			this.cookies = new ArrayList<String>();
//		System.out.println("\n\n\n\nRESPONSE:\n" + response.toString());
//		System.out.println("\n\n\n\n\n");

		return response.toString();
	}
	
	/**
	 * Send a HTTP POST to a url.
	 * Example of postParams format is:
	 * key1=value1&key2=value2&...&keyN=valueN
	 * 
	 */
	public String sendPost(String url, String postParams, Boolean login) throws Exception {
		System.out.println("Enviando HTTP POST.");
		URL obj = new URL(url);
		
		HttpURLConnection conn = null;

		if(url.startsWith("http:"))
			conn = (HttpURLConnection) obj.openConnection();
		else if(url.startsWith("https:"))
			conn = (HttpsURLConnection) obj.openConnection();
		else throw new Exception("Not a valid HTTP URL");

		// Acts like a browser
//			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("User-Agent", USER_AGENT);
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
			conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
//		for (String cookie : this.cookies) {
//			conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
//		}
			conn.setRequestProperty("Connection", "keep-alive");
			conn.setRequestProperty("Cache-Control", "max-age=0");
			conn.setRequestProperty("Origin", "https://192.168.0.192");//TODO HARDCODED
			conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//		conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));
			conn.setRequestProperty("Referer", "https://192.168.0.192/Portal/Portal.mwsl?PriNav=Awp");
			conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
		
		conn.setDoOutput(true);
		conn.setDoInput(true);
		// Send post request
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();
		
		System.out.println("CONNECTION OUTPUT");
//		System.out.println(readInputStreamToString(conn));

		System.out.println("HEADER FIELDS");
		System.out.println(conn.getHeaderFields().toString());
		
		this.cookies = conn.getHeaderFields().get("Set-cookie");
		System.out.println("Cookies almacenadas: ");
		if(cookies != null) {
			System.out.println(cookies);
			for(String cookie : cookies) {
				System.out.println(cookie);
			}
		}
		else {
			System.out.println("COOKIES VACIAS!");
		}
		
		System.out.println("CONNECTION OUTPUT");
//		System.out.println(readInputStreamToString(conn));

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		if(login)
			this.cookies = conn.getHeaderFields().get("Set-Cookie");
		
		System.out.println("HTTP POST enviado. Response: " + response.toString() + "\n");
		return response.toString();
	}
	
	
	
	
	
	
	private String readInputStreamToString(HttpURLConnection connection) {
	    String result = null;
	    StringBuffer sb = new StringBuffer();
	    InputStream is = null;

	    try {
	        is = new BufferedInputStream(connection.getInputStream());
	        BufferedReader br = new BufferedReader(new InputStreamReader(is));
	        String inputLine = "";
	        while ((inputLine = br.readLine()) != null) {
	            sb.append(inputLine);
	        }
	        result = sb.toString();
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	        result = null;
	    }
	    finally {
	        if (is != null) {
	            try { 
	                is.close(); 
	            } 
	            catch (IOException e) {
	            	e.printStackTrace();
	            }
	        }   
	    }

	    return result;
	}
	
	
	
	
	
	
	
	
	
	
	private void login() throws Exception {
		String data = "Redrection=&Login=" + username + "&Password=" + password;
		
		if( !ip.contains("localhost") && !ip.contains("127.0.0.1") )
			loginByCURL(ip, formAction, PLCAdminURL, data);
//		System.out.println("Login a " + loginURL);
//		// make sure cookies is turn on
//		CookieHandler.setDefault(new CookieManager());
//
//		// 1. Send a "GET" request, so that you can extract the form's data.
//		String page = getPageContent(loginURL);
//		String postParams = getFormParams(page, username, password,
//											loginFormID, usernameFormId, passwordFormId);
//
//		// 2. Construct above post's content and then send a POST request for
//		// authentication
//		return sendPost(loginAction, postParams, true);
	}

	private String getFormParams(String html, String username, String password, String formID, String usernameInputId,
			String passwordInputId) throws UnsupportedEncodingException {


		Document doc = Jsoup.parse(html);

		//Element loginform = doc.getElementById(formID);
		
		List<String> paramList = new ArrayList<String>();
		
		Element inputUser = doc.getElementById(usernameInputId);
		paramList.add( inputUser.attr("name") + "=" + URLEncoder.encode(username, "UTF-8"));
		
		Element inputPass = doc.getElementById(passwordInputId);
		paramList.add( inputPass.attr("name") + "=" + URLEncoder.encode(password, "UTF-8"));
		
		paramList.add( "Redirection=" );
		
		
//		Elements inputElements = loginform.getElementsByTag("input");
//		List<String> paramList = new ArrayList<String>();
//		for (Element inputElement : inputElements) {
//			String key = inputElement.attr("name");
//			String value = inputElement.attr("value");
//
//			if (key.equals(usernameInputName))
//				value = username;
//			else if (key.equals(passwordInputName))
//				value = password;
//			paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
//		}

		StringBuilder result = new StringBuilder();
		for (String param : paramList) {
			if (result.length() == 0) {
				result.append(param);
			} else {
				result.append("&" + param);
			}
		}
		
		System.out.println("PARAMETROS PARA LOGIN: " + result);
		return result.toString();
	}
	
	
	
	
	
	
	public void editVar(String data) {
		editVarByCURL(ip, PropertiesUtils.getPLCWebServerURL(), data);
	}
	
	
	
	
	/**
	 * ip: https://192.168.0.192
	 * plcWebServerURL: /awp//index.htm
	 * PLCAdminURL: /Portal/Portal.mwsl
	 * data: %22DiSbAltSH%22.Ti2Start=622&%22DiSbBajSH%22.Ti2Start=123
	 *
	 **/
	public void editVarByCURL(String ip, String plcWebServerURL, String data) {
		if (PropertiesUtils.getDebugLog()) {
			System.out.println("Cookies: " + cookies);
		}
		String cookie = "";
		for (String c : cookies) {
			cookie = cookie.concat(c + "; ");
		}
		if(cookie.length()>2) {
			cookie = cookie.substring(0, cookie.length()-2);
		}
		String curl = "curl-edit-var.bat"
				+ " \"" + ip + plcWebServerURL+ "\""
				+ " -H \"Cookie: " + cookie + "\""
				+ " -H \"Origin: " + ip + "\""
				+ " -H \"Referer: " + ip + plcWebServerURL + "\""
				+ " --data \"" + data + "\"";
		System.out.println("Executing cURL:\n" + curl);
		
		try {
			Process process = Runtime.getRuntime().exec(curl);
//			process.waitFor(); SE TILDA ESTO.
//			System.out.println("Valor de Salida: " + process.exitValue());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Object obj = new Object();
		synchronized (obj) {
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Done");
		
	}
	/**
	 * ip: https://192.168.0.192
	 * formAction: /FormLogin
	 * PLCAdminURL: /Portal/Portal.mwsl
	 * data: Redirection=&Login=admin&Password=admin
	 *
	 **/
	public void loginByCURL(String ip, String formAction, String PLCAdminURL, String data) {
		String curl = "curl-login.bat \"" + ip + formAction+ "\""
				+ " -H \"Origin: " + ip + "\""
				+ " -H \"Referer: " + ip + PLCAdminURL + "\""
				+ " --data \"" + data + "\"";
		System.out.println("Executing cURL:\n" + curl);
		
		try {
			Runtime.getRuntime().exec(curl).waitFor();

			Object obj = new Object();
			synchronized (obj) {
				try {
					System.out.println("Logueandose al WebServer...");
					TimeUnit.SECONDS.sleep(3);
					System.out.println("Recuperando Cookies...");
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			
			this.cookies = getCookiesFromTxtOutput("tempLogin.txt");
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	private List<String> getCookiesFromTxtOutput(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		List<String> cookies = new ArrayList<String>();
		boolean invalidLogin = false;
		try {
		    String line = br.readLine();

		    while (line != null) {
		    	if(line.contains("Set-cookie:")){
		    		String cookie = line.substring(	line.indexOf("Set-cookie: ") + "Set-cookie: ".length()
		    										,line.indexOf("; path="));
	    			cookies.add(cookie);
	    			System.out.println("Cookie: " + cookie);
		    	}
		    	if(line.contains("?InvalidLogin=true")){
		    		invalidLogin = true;
		    	}
		        line = br.readLine();
		    }
		} finally {
		    br.close();
		}
		if(invalidLogin) {
			System.out.println("Login incorrecto. Asegurese que las credenciales de ingreso son las correctas."
					+ " Si el problema persiste, pruebe conectarse de nuevo en unos minutos, o desconectarse del Webserver"
					+ " en caso de que este conectado por otro medio.");
		}
		
		return cookies;
	}
	
	private String execute(String... command) throws IOException, InterruptedException {
		ProcessBuilder builder = new ProcessBuilder(command);
		Process p = builder.start();
		
		
		
		StringBuffer output = new StringBuffer();
//		Process p = Runtime.getRuntime().exec(command);
//	    p.waitFor();

		InputStream is = p.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line;

		while ((line = br.readLine()) != null) {
			output.append(line + "\n");
	    	System.out.println(line);
		}

	    
	    return output.toString();
	}
	
	

}