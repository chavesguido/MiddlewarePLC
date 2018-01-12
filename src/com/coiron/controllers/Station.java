package com.coiron.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.coiron.connections.SocketConnection;
import com.coiron.model.PLC;
import com.coiron.utils.NetUtils;
import com.coiron.utils.PropertiesUtils;

public class Station implements Runnable{
	private static Station instance = null;
	
	private String clientID = PropertiesUtils.getClientID();
	private List<PLC> plcs = new ArrayList<PLC>();
	
	private Station() {}
	
	public static Station getInstance() {
		if(instance == null) {
			instance = new Station();
		}
		return instance;
	}
	
	@Override
	public void run() {
		
		searchPLCS();
		
		PLCSToServer();
		
	}
	
	private void searchPLCS(){
		
		try {
			
			for(String ip : NetUtils.getPLCIPs()){
				PLC p = new PLC();
				
				p.setIp(ip);
				plcs.add(p);
				
				//ACA VAN METODOS PARA CONSEGUIR EL ID Y LAS VARIABLES DEL PLC CONECTANDOSE AL HTML
				getPLCSId();
				synchronizePLCS();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void PLCSToServer() {
		
		while(true){
			
			try {
				
				synchronizePLCS();
				
				SocketConnection.getInstance().sendPLCSLikeJSON(this);
				
				this.wait(5000);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	public void updatePLC( String idPLC, Entry<String, String> variable ) {
		
		for(PLC p : plcs)
			if(p.getId().equals(idPLC))
				p.getVariables().put( variable.getKey(), variable.getValue() );
		
		//NETUTILS UPDATE ¿INDIVIDUAL O DE A MUCHAS VARIABLES? UN FORM GENERAL O UN FORM POR VARIABLE?
	}

	
	public String getClientID() {
		return clientID;
	}
	public List<PLC> getPlcs() {
		return plcs;
	}
	
}
