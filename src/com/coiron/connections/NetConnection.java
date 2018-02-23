package com.coiron.connections;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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
	
	private List<String> cookies = new ArrayList<String>();

	private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";
	
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

		login();
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
		if (cookies != null) {
			for (String cookie : this.cookies) {
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		if (this.cookies == null)
			this.cookies = new ArrayList<String>();

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
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		for (String cookie : this.cookies) {
			conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
		}
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));
		conn.setRequestProperty("Referer", loginURL);
		conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
		
		conn.setDoOutput(true);
		conn.setDoInput(true);
		// Send post request
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();

		this.cookies = conn.getHeaderFields().get("Set-Cookie");

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
	
	private String login() throws Exception {
		System.out.println("Login a " + loginURL);
		// make sure cookies is turn on
		CookieHandler.setDefault(new CookieManager());

		// 1. Send a "GET" request, so that you can extract the form's data.
		String page = getPageContent(loginURL);
		String postParams = getFormParams(page, username, password,
											loginFormID, usernameFormId, passwordFormId);

		// 2. Construct above post's content and then send a POST request for
		// authentication
		return sendPost(loginAction, postParams, true);
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

}