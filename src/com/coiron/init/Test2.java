package com.coiron.init;

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
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Test2 {

  private List<String> cookies = new ArrayList<String>();
  private HttpsURLConnection conn;

  private final String USER_AGENT = "Mozilla/5.0";

  public static void main(String[] args) throws Exception {

	String loginURL = "https://www.facebook.com/login.php";
	String domain = "https://www.facebook.com";
	String url = "https://www.facebook.com/";

	Test2 http = new Test2();

	// make sure cookies is turn on
	CookieHandler.setDefault(new CookieManager());

	// 1. Send a "GET" request, so that you can extract the form's data.
	String page = http.GetPageContent(loginURL);
	String postParams = http.getFormParams(page, "javier.pozzi", "hackeame.esta", "login_form", "email", "pass");

	// 2. Construct above post's content and then send a POST request for
	// authentication
	http.sendPost(loginURL, postParams, domain);

	// 3. success then go to gmail.
	String result = http.GetPageContent(url);
	System.out.println(result);
  }

  private void sendPost(String url, String postParams, String domain) throws Exception {

	URL obj = new URL(url);
	conn = (HttpsURLConnection) obj.openConnection();

	// Acts like a browser
	System.out.println("cookies: " + this.cookies.toString());
	for (String cookie : this.cookies) {
		conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
	}

	conn.setDoOutput(true);
	conn.setDoInput(true);
	
	// Send post request
	DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
	wr.writeBytes(postParams);
	wr.flush();
	wr.close();
	
	setCookies(conn.getHeaderFields().get("Set-Cookie"));
	
	int responseCode = conn.getResponseCode();
	System.out.println("\nSending 'POST' request to URL : " + url);
	System.out.println("Post parameters : " + postParams);
	System.out.println("Response Code : " + responseCode);

	BufferedReader in =
             new BufferedReader(new InputStreamReader(conn.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
		response.append(inputLine);
	}
	in.close();
	 System.out.println(response.toString());

	 setCookies(conn.getHeaderFields().get("Set-Cookie"));
  }

  private String GetPageContent(String url) throws Exception {

	URL obj = new URL(url);
	conn = (HttpsURLConnection) obj.openConnection();

	// default is GET
	conn.setRequestMethod("GET");

	conn.setUseCaches(false);

	// act like a browser
	conn.setRequestProperty("User-Agent", USER_AGENT);
	conn.setRequestProperty("Accept",
		"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	if (cookies != null) {
		for (String cookie : this.cookies) {
			conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
		}
	}
	int responseCode = conn.getResponseCode();
	System.out.println("\nSending 'GET' request to URL : " + url);
	System.out.println("Response Code : " + responseCode);

	BufferedReader in =
            new BufferedReader(new InputStreamReader(conn.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
		response.append(inputLine);
	}
	in.close();

	// Get the response cookies
//	setCookies(conn.getHeaderFields().get("Set-Cookie"));
	
	if(this.cookies == null)
		setCookies( new ArrayList<String>() );

	return response.toString();

  }

  public String getFormParams(String html, String username, String password, String formID, String usernameInputName, String passwordInputName)
		throws UnsupportedEncodingException {

	System.out.println("Extracting form's data...");

	Document doc = Jsoup.parse(html);

	// Google form id
	Element loginform = doc.getElementById(formID);
//	Element loginform = doc.getElementsByAttributeValue("name", formID).get(0);
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

	// build parameters list
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

  public List<String> getCookies() {
	return cookies;
  }

  public void setCookies(List<String> cookies) {
	this.cookies = cookies;
  }

}