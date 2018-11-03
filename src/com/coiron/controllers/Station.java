package com.coiron.controllers;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.coiron.connections.CloudSocketConnection;
import com.coiron.connections.LocalSocketConnection;
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
	private String frigName = PropertiesUtils.getFrigName();
	private List<PLC> plcs = new ArrayList<PLC>();
	
	//PLCs que estaban conectados, se desconectaron, y pasan a un estado de reconexion constante. Cuando se reconectan,
	//vuelven al array de plcs normal
	private List<PLC> standBy = new ArrayList<PLC>();
	
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
		
		//Conectandose con servidor local
		new Thread(LocalSocketConnection.getInstance(), "socketLocalServer").start();
		while( !LocalSocketConnection.getInstance().isConnected() ) {
			
			synchronized (obj) {
				try {
					TimeUnit.SECONDS.sleep(3);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		new Thread(CloudSocketConnection.getInstance(), "socketCloudServer").start();
		
		//Enviando variables de los plcs a servidores
		PLCSToServers();
		
	}
	
	private void searchPLCS(){
		
		try {
			String cantMock = PropertiesUtils.getCantPLCS();
			
			System.out.println("Detectando IPs en la red...\n");
			
			Map<String, String> PLC_IPs = NetUtils.getPLCIPs();
			
			
			
			if( PLC_IPs.isEmpty() && "0".equals(cantMock) ) {
				System.out.println("\n\nNo se han encontrado PLCs en la red. Asegurese de estar conectado a la misma red y"
						+ " que los PLCs esten configurados como WebServers.");
				System.exit(0);
			}
			else
				System.out.println("\n\n" + PLC_IPs.size() + " PLC encontrados en la red.\n\n");
			
			for(String ip : PLC_IPs.keySet()){
				
				System.out.println("Sincronizando PLC con IP " + ip);
				
				PLC p = new PLC(ip, PLC_IPs.get(ip));
				plcs.add(p);
				
				p.setId( NetUtils.getMacAddress(ip) );
			}
			
			
			
			
			//MOCK PLC BASADO EN PARAMETRO DEL PROPERTIES
			
			for(int i=1; i<= Integer.parseInt(cantMock); i++) {
				System.out.println("Sincronizando PLC" + i + " con IP 127.0.0.1:4200");
				PLC p = new PLC("127.0.0.1:4200", "http://");
				plcs.add(p);
				p.setId("plc" + i + PropertiesUtils.getClientID() + PropertiesUtils.getFrigName());
			}
			//MOCK PLC
			
			
			
			System.out.println("PLC sincronizados.\n");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void PLCSToServers() {
		
		
		synchronizing = true;
		
		while(true){
			if(synchronizing) {
				try {
					
					synchronized (obj) {
						TimeUnit.SECONDS.sleep(10);
					}
					
					synchronizePLCS();
					
					if( !plcs.isEmpty() ) {
						LocalSocketConnection.getInstance().sendPLCSLikeJSON(this);
						CloudSocketConnection.getInstance().sendPLCSLikeJSON(this);
					}
					
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
			}catch(ConnectException ce) {
				LocalSocketConnection.getInstance().deletePLC(p);
				CloudSocketConnection.getInstance().deletePLC(p);
				standBy.add(p);
				
				System.out.println("Se ha perdido la conexión con el PLC con id " + p.getId() + ". Razón: " + ce.getMessage() + "\n");
			}catch (Exception e){
				
				if(e.getMessage().equalsIgnoreCase("DELETE PLC")) {
					LocalSocketConnection.getInstance().deletePLC(p);
					CloudSocketConnection.getInstance().deletePLC(p);
					standBy.add(p);
				}
				else e.printStackTrace();
			}
			
		}
		
		for (PLC p : standBy) {
			p.setWebserver(null);
			plcs.remove(p);
		}
		
		reconnectStandBy();
		
	}
	
	
	private void reconnectStandBy() {
		
		Iterator<PLC> iter = standBy.iterator();
		
		while (iter.hasNext()) {
		    PLC p = iter.next();

		    try {
				p.synchronize();
				
				//Si pasa el synchronize es porque no catcheo, entonces lo devuelve al array normal
				//y lo saco de los standBy
				
				plcs.add(p);
				iter.remove();
				
				System.out.println("Plc con id " + p.getId() + " reconectado.\n");
				
		    }catch(ConnectException ce) {
				System.out.println("Plc con id " + p.getId() + " continua offline.\n");
			} catch (Exception e){
				
				if(e.getMessage().equalsIgnoreCase("DELETE PLC")) {
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
