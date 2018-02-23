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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class NetUtils {

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


		while ((line = br.readLine()) != null) {
			List<String> list = new ArrayList<String>(Arrays.asList(line.split(" ")));
			list.removeAll(Arrays.asList("", null));
			if(!list.isEmpty() && list.get(0).contains(".")) {
				System.out.println("Detectada IP: " + list.get(0));
				ips.add(list.get(0));
			}
		}

		return ips;
	}

	public static Map<String, String> getPLCIPs() throws Exception {
		
		List<String> localIps = getLocalIPs();
		Map<String, String> PLCIps = new HashMap<String, String>();
		
		for(String ip : localIps) {
			try {
				System.out.println("scanning http://" + ip + PropertiesUtils.getPLCAdminURL());
				String loginHTML = getHTML("http://" + ip + PropertiesUtils.getPLCAdminURL());
				
				if(loginHTML.contains("Portal") || loginHTML.contains("Siemens")) {
					PLCIps.put(ip, "http://");
					System.out.println(ip + " is a plc IP");					
				}else {
					System.out.println(ip + " not a plc IP");
				}

			} catch (Exception e) {
				System.out.println(ip + " not a plc IP");
			}
			
			try {
				System.out.println("scanning https://" + ip + PropertiesUtils.getPLCAdminURL());
				String loginHTML = getHTML("https://" + ip + PropertiesUtils.getPLCAdminURL());
				
				
				
				if(loginHTML.contains("Portal") || loginHTML.contains("Siemens")) {
					PLCIps.put(ip, "https://");
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
		
		HttpURLConnection conn = null;
		
		if(urlToRead.startsWith("http:"))
			conn = (HttpURLConnection) url.openConnection();
		else if(urlToRead.startsWith("https:"))
			conn = (HttpsURLConnection) url.openConnection();
		else throw new Exception("Not a valid HTTP URL");
		
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
	
	
	
	public static String getMacAddress(String ip) throws IOException {

		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec("arp -a");
		InputStream is = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line;

		br.readLine();
		br.readLine();
		br.readLine();


		while ((line = br.readLine()) != null) {
			
			List<String> list = new ArrayList<String>(Arrays.asList(line.split(" ")));
			list.removeAll(Arrays.asList("", null));
			
			if(!list.isEmpty() && list.get(0).contains(".")) {
				if( list.get(0).equals(ip) )
					return list.get(1).replace("-", "");
			}
			
		}

		throw new IOException("MAC ADDRESS NOT FOUND FOR IP " + ip);
	}
	
}
