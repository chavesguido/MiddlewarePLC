package com.coiron.connections;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
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

	private String loginURL = "https://www.facebook.com/login.php";
	private String loginFormID = "login_form";
	private String usernameFormName = "email";
	private String passwordFormName = "pass";
	
	private List<String> cookies = new ArrayList<String>();
	private HttpsURLConnection conn;

	private final String USER_AGENT = "Mozilla/5.0";
	
	public NetConnection(String loginURL, String loginFormID,
							String usernameFormName, String passwordFormName) throws Exception {
		this.loginURL = loginURL;
		this.loginFormID = loginFormID;
		this.usernameFormName = usernameFormName;
		this.passwordFormName = passwordFormName;

//		login();
	}
	
	public String getPageContent(String url) throws Exception {

		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();

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
	
	private String login() throws Exception {
		// make sure cookies is turn on
		CookieHandler.setDefault(new CookieManager());

		// 1. Send a "GET" request, so that you can extract the form's data.
		String page = getPageContent(loginURL);
		String postParams = getFormParams(page, PropertiesUtils.getUsernamePLC(), PropertiesUtils.getPasswordPLC(),
											loginFormID, usernameFormName, passwordFormName);

		// 2. Construct above post's content and then send a POST request for
		// authentication
		return sendPost(loginURL, postParams);
	}

	private String sendPost(String url, String postParams) throws Exception {

		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();

		// Acts like a browser
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		for (String cookie : this.cookies) {
			conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
		}
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));

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

		this.cookies = conn.getHeaderFields().get("Set-Cookie");
		
		return response.toString();
	}

	private String getFormParams(String html, String username, String password, String formID, String usernameInputName,
			String passwordInputName) throws UnsupportedEncodingException {


		Document doc = Jsoup.parse(html);

		Element loginform = doc.getElementById(formID);
		Elements inputElements = loginform.getElementsByTag("input");
		List<String> paramList = new ArrayList<String>();
		for (Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");

			if (key.equals(usernameInputName))
				value = username;
			else if (key.equals(passwordInputName))
				value = password;
			paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
		}

		StringBuilder result = new StringBuilder();
		for (String param : paramList) {
			if (result.length() == 0) {
				result.append(param);
			} else {
				result.append("&" + param);
			}
		}
		return result.toString();
	}

}