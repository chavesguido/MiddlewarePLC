package com.coiron.init;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.coiron.controllers.Station;
import com.coiron.model.PLC;

public class Init{
	
	public static void main(String[] args) throws IOException{
		disableSslVerification();
		
//		StringBuffer output = new StringBuffer();
//		
//		Process p = Runtime.getRuntime().exec(
//"curl-login.bat \"" + ip + formAction + "\" -H \"Connection: keep-alive\" -H \"Origin: " + ip + "\" -H \"Referer: " + ip + PLCAdminURL + "\" --data \"Redirection=&Login=" + username + "&Password=" + password + "\" \"" + ip + "_file.txt\" " + ip + "_tempLogin.txt"
//				);
////	    p.waitFor();
//		
////		ProcessBuilder builder = new ProcessBuilder(com);
////		builder.redirectOutput(new File("curloutput.txt"));
////		builder.start();
////		System.out.println(p.getOutputStream().toString());
//		
//
//	    InputStream is = p.getInputStream();
//		InputStreamReader isr = new InputStreamReader(is);
//		BufferedReader br = new BufferedReader(isr);
//		String line;
//
//		while ((line = br.readLine()) != null) {
//			output.append(line + "\n");
//	    	System.out.println(line);
//		}
//		
//		return;
//		
//		
//		try {
////			new NetConnection().sendPost("https://www.facebook.com", "Redirection=&Login=admin&Password=admin", false);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("dsa\n\n\n\n\n\n\n\n");
//		
//		try {
////			new NetConnection().sendPost("https://192.168.0.192/FormLogin", "Redirection=&Login=admin&Password=admin", false);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//		
//		
//		System.out.println("asd");
//		StringBuffer output = new StringBuffer();
//		String com = PropertiesUtils.readProperty("curl1");
//		System.out.println(com);
//		Process p = Runtime.getRuntime().exec(com);
////	    p.waitFor();
//		
////		ProcessBuilder builder = new ProcessBuilder(com);
////		builder.redirectOutput(new File("curloutput.txt"));
////		builder.start();
////		System.out.println(p.getOutputStream().toString());
//
//	    InputStream is = p.getInputStream();
//		InputStreamReader isr = new InputStreamReader(is);
//		BufferedReader br = new BufferedReader(isr);
//		String line;
//
//		while ((line = br.readLine()) != null) {
//			output.append(line + "\n");
//	    	System.out.println(line);
//		}
//		System.out.println("dsa");
//		System.out.println("dsa");
//		System.out.println("dsa");
//		System.out.println("dsa\n\n\n\n\n\n\n\n");
//		
//		
//		System.out.println("asd");
//		 output = new StringBuffer();
//		  com = PropertiesUtils.readProperty("curl2");
//		System.out.println(com);
//		 p = Runtime.getRuntime().exec(com);
////	    p.waitFor();
//		
////		ProcessBuilder builder = new ProcessBuilder(com);
////		builder.redirectOutput(new File("curloutput.txt"));
////		builder.start();
////		System.out.println(p.getOutputStream().toString());
//
//	     is = p.getInputStream();
//		 isr = new InputStreamReader(is);
//		 br = new BufferedReader(isr);
//
//		while ((line = br.readLine()) != null) {
//			output.append(line + "\n");
//	    	System.out.println(line);
//		}
//		
//		
//		
//		
////		test();
////		String path = Init.class.getProtectionDomain().getCodeSource().getLocation().getPath();
////		try {
////			String decodedPath = URLDecoder.decode(path, "UTF-8");
////			System.out.println(decodedPath);
////			System.out.println(new File(".").getAbsolutePath());
////		} catch (UnsupportedEncodingException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
////		
//		new NetConnection("https://192.168.0.192", "/FormLogin", "/Portal/Portal.mwsl", "admin", "admin");
//		
		
//		NetConnection nc = new NetConnection("https://192.168.0.192", "/FormLogin", "/Portal/Portal.mwsl", "admin", "admin");
//		nc.editVarByCURL("https://192.168.0.192", "/awp//index.htm", "%22DiSbAltSH%22.Ti2Start=622");
		startStation();
		
		
	}
	
	private static void startStation() {
		Station.getInstance().run();
	}
	
	private static void disableSslVerification() {
	    try
	    {
	        // Create a trust manager that does not validate certificate chains
	        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	            public void checkClientTrusted(X509Certificate[] certs, String authType) {
	            }
	            public void checkServerTrusted(X509Certificate[] certs, String authType) {
	            }
	        }
	        };

	        // Install the all-trusting trust manager
	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session) {
	                return true;
	            }
	        };

	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } catch (KeyManagementException e) {
	        e.printStackTrace();
	    }
	}
	
	
	
	
	
	static void test() {
		Thread t = new Thread() {
		    public void run() {
		    	
		    	Object obj = new Object();
		    	synchronized (obj) {
					try {
						TimeUnit.SECONDS.sleep(5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
		    	
		    	try {
			    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			    	System.out.println("Simulando modificación de variable");
			    	
			    	System.out.println("Ingrese ip del plc a modificar");
					String ipPLC = br.readLine();
					
					System.out.println("Ingrese variable a modificar");
					String key = br.readLine();
					
					System.out.println("Ingrese nuevo valor");
					String value = br.readLine();
					
					for(PLC p : Station.getInstance().getPlcs()) {
						if(p.getIp().equals(ipPLC)) {
							p.getVariables().put(key, value);
							p.updateWebServer( p.getVariables());
							System.out.println("Valor modificado");
						}
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
		    }
		};
		t.start();
	}
	
	/* EJEMPLO DE HTML
	 <body>
     :="webdata".myInt:
     <form method="post">
       <input name='"webdata".myInt' type="text" />
       <button type="submit">Save</button>
     </form>
   </body>
	 * */

}
