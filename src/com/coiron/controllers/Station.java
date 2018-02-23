package com.coiron.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.coiron.connections.NetConnection;
import com.coiron.connections.SocketConnection;
import com.coiron.model.PLC;
import com.coiron.utils.NetUtils;
import com.coiron.utils.PropertiesUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Station {
	@JsonIgnore
	private static Station instance = null;
	@JsonIgnore
	public final static Object obj = new Object();
	@JsonIgnore
	private boolean synchronizing = false;

	
	private String clientID = PropertiesUtils.getClientID();
	private String frigName = PropertiesUtils.getFrigName();
	private List<PLC> plcs = new ArrayList<PLC>();
	
	private Station() {}
	
	public static Station getInstance() {
		if(instance == null) {
			instance = new Station();
		}
		return instance;
	}
	
	public void run() {
		
		//Buscando PLCs con WebServer habilitado en red Local
		searchPLCS();
		
		//Conectandose con el servidor
		new Thread(SocketConnection.getInstance(), "socket").start();
		
		while( !SocketConnection.getInstance().isConnected() ) {
			
			synchronized (obj) {
				try {
					TimeUnit.SECONDS.sleep(3);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		//Enviando variables de los plcs al servidor
		PLCSToServer();
		
	}
	
	private void searchPLCS(){
		
		try {
			System.out.println("Detectando PLC en la red...");
			
			Map<String, String> PLC_IPs = NetUtils.getPLCIPs();
			
			System.out.println("\n\n" + PLC_IPs.size() + " PLC encontrados en la red.\n\n");
			
			for(String ip : PLC_IPs.keySet()){
				
				System.out.println("Sincronizando PLC con IP " + ip);
				
				PLC p = new PLC(ip, PLC_IPs.get(ip));
				plcs.add(p);
				
				p.setId( NetUtils.getMacAddress(ip) );
			}
			
			
			
			
			//MOCK PLC BASADO EN PARAMETRO DEL PROPERTIES
			String cant = PropertiesUtils.getCantPLCS();
			
			for(int i=1; i<= Integer.parseInt(cant); i++) {
				System.out.println("Sincronizando PLC" + i + " con IP 127.0.0.1:4200");
				PLC p = new PLC("127.0.0.1:4200", "http://");
				plcs.add(p);
				p.setId("plc" + i + PropertiesUtils.getClientID() + PropertiesUtils.getFrigName());
			}
			//MOCK PLC
			
			
			
			System.out.println("PLC sincronizados.");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void PLCSToServer() {
		
		synchronizing = true;
		
		while(true){
			if(synchronizing) {
				try {
					
					synchronized (obj) {
						TimeUnit.SECONDS.sleep(5);
					}
					
					synchronizePLCS();
					
					SocketConnection.getInstance().sendPLCSLikeJSON(this);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void synchronizePLCS() {
		for (PLC p : plcs) {
			
			try {
				p.synchronize();
			} catch (Exception e){
				
				if(e.getMessage().equalsIgnoreCase("DELETE PLC")) {
					SocketConnection.getInstance().deletePLC(p);
				}
				else e.printStackTrace();
				
			}
			
		}
	}
	
	
	public void updatePLC( String idPLC, Map<String, String> variables ) {
		synchronizing = false;
		
		if(idPLC == null || variables == null) return;
		
		PLC plcLocal = getPlcById(idPLC);
		
		if(plcLocal == null) return;
		
//		plcLocal.setVariables(variables);
		
//		for(Entry<String, String> v : variables.entrySet()) 
//			plcLocal.getVariables().put( v.getKey(), v.getValue() );
		
		try {
			
			plcLocal.updateWebServer( variables );
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		synchronizing = true;
	}
	
	public PLC getPlcById(String id) {
		for(PLC p : plcs)
			if(p.getId().equals(id))
				return p;
		return null;
	}

	
	public String getClientID() {
		return clientID;
	}
	public List<PLC> getPlcs() {
		return plcs;
	}
	public String getFrigName() {
		return frigName;
	}
	
	
}
