package com.coiron.utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetUtils {

	private static final String IPV4_REGEX = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
	
	private static List<String> getLocalIPs() throws Exception {
		List<String> ips = new ArrayList<String>();

		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec("arp -a");
		InputStream is = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line;

		br.readLine();
		br.readLine();
		br.readLine();

		Pattern pattern = Pattern.compile(IPV4_REGEX);
		Matcher matcher;

		while ((line = br.readLine()) != null) {
			matcher = pattern.matcher(line);
			if (matcher.find())
				ips.add(matcher.group());
		}

		return ips;
	}

	public static List<String> getPLCIPs() throws Exception {
		
		
//		List<String> a = new ArrayList<String>();
//		a.add("192.168.0.123");
//		a.add("192.168.0.125");
//		a.add("192.168.0.127");
//		return a;
		
		List<String> localIps = getLocalIPs();
		List<String> PLCIps = new ArrayList<String>();
		
		for(String ip : localIps) {
			System.out.println("\n scanning " + ip);
			try {
				String loginHTML = getHTML("http://" + ip + PropertiesUtils.getPLCAdminURL());
				if(loginHTML.contains("Portal") || loginHTML.contains("Siemens")) {
					PLCIps.add(ip);
					System.out.println(ip + " is a plc IP");					
				}else {
					System.out.println(ip + " not a plc IP");
				}

			} catch (Exception e) {
				System.out.println(ip + " not a plc IP");
			}
		}
		
		return PLCIps;
	}

	private static String getHTML(String urlToRead) throws Exception {
		int timeout = 2000;
		
		StringBuilder result = new StringBuilder();
		URL url = new URL(urlToRead);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		rd.close();
		return result.toString();
	}
}
