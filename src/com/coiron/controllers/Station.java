package com.coiron.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.coiron.connections.NetConnection;
import com.coiron.connections.SocketConnection;
import com.coiron.model.PLC;
import com.coiron.utils.NetUtils;
import com.coiron.utils.PropertiesUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Station {
	@JsonIgnore
	private static Station instance = null;
	@JsonIgnore
	public final static Object obj = new Object();
	@JsonIgnore
	private boolean synchronizing = false;

	
	private String clientID = PropertiesUtils.getClientID();
	private List<PLC> plcs = new ArrayList<PLC>();
	
	private Station() {}
	
	public static Station getInstance() {
		if(instance == null) {
			instance = new Station();
		}
		return instance;
	}
	
	public void run() {
		
		searchPLCS();
		
		new Thread(SocketConnection.getInstance(), "socket").start();
		
		PLCSToServer();
		
	}
	
	private void searchPLCS(){
		
		try {
			System.out.println("Detectando PLC en la red...");
			
			List<String> PLC_IPs = NetUtils.getPLCIPs();
			
			System.out.println(PLC_IPs.size() + " PLC encontrados en la red.");
			
			for(String ip : PLC_IPs){
				
				System.out.println("Sincronizando PLC con IP " + ip);
				
				PLC p = new PLC();
				
				p.setIp(ip);
				p.setWebserver(
						new NetConnection(ip + PropertiesUtils.getPLCAdminURL(),
											"", "", "")
						);
				plcs.add(p);
				
				//ACA VA METODO PARA CONSEGUIR EL ID DEL PLC CONECTANDOSE AL HTML
//				getPLCId();
			}
			
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
		try {
			
			for (PLC p : plcs)
				p.synchronize();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void updatePLC( String idPLC, Entry<String, String> variable ) {
		synchronizing = false;
		
		PLC plc = null;
		
		for(PLC p : plcs)
			if(p.getIdPlc().equals(idPLC))
				plc = p;
		
		if(plc == null) return;
		
		plc.getVariables().put( variable.getKey(), variable.getValue() );
		
		try {
			
			plc.updateWebServer(variable.getKey());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		synchronizing = true;
	}

	
	public String getClientID() {
		return clientID;
	}
	public List<PLC> getPlcs() {
		return plcs;
	}
	
}
