package com.coiron.init;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import com.coiron.controllers.Station;
import com.coiron.model.PLC;

public class Init{

	public static void main(String[] args){
		test();
		Station.getInstance().run();
		
	}
	
	//TODO
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
							p.updateWebServer( new HashSet<>(    Arrays.asList(key)     ));
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
