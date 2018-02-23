package com.coiron.init;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import com.coiron.controllers.Station;
import com.coiron.model.PLC;

public class Init{

	public static void main(String[] args){
//		test();
		
		disableSslVerification();
		
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
