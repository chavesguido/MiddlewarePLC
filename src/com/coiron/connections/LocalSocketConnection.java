package com.coiron.connections;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.coiron.controllers.Station;
import com.coiron.model.PLC;
import com.coiron.utils.PropertiesUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class LocalSocketConnection implements Runnable {
	private static LocalSocketConnection instance = null;
	public final static Object obj = new Object();
	
    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;
    String message;
    Boolean connected = false;
    String url;
    Boolean disabled;
    
    
    private LocalSocketConnection(){
    	disabled = PropertiesUtils.getDisabledLocal();
    	if (disabled) {
			System.out.println("Sincronización con Servidor Local deshabilitada.");
		}
    }
    
    public static LocalSocketConnection getInstance() {
    	if(instance == null) {
    		instance = new LocalSocketConnection();
    	}
    	return instance;
    }
    
    @Override
    public void run() {
    	if (disabled) {
			return;
		}
    	
    	while(!connected) {
    		
    		detectLocalIPAddress();
    	
			try {
				System.out.println("Estableciendo conexión con el servidor local...");
				
				socket = IO.socket(url);
				socket.on("editarVariables", new Emitter.Listener() {
					
					public void call(Object... arg0) {
						try {
							ObjectMapper objectMapper = new ObjectMapper();
							
							System.out.println("Editando variable... " + arg0[0].toString());
							
							PLC plcServer = objectMapper.readValue(arg0[0].toString(), PLC.class);

							if(plcServer == null) return;
							
							System.out.println("mensaje del servidor: " + plcServer.toString());
							
							Station.getInstance().updatePLC(plcServer.getId(), plcServer.getVariables());
							
							//TODO
							socket.emit("resp", "editado");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
				});
				
				
				socket.on("conectado", new Emitter.Listener() {
					public void call(Object... arg0) {
						connected = true;
					}
				});
				
				socket.connect();
				
				synchronized (obj) {
					try {
						TimeUnit.SECONDS.sleep(3);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				if(connected) {
					System.out.println("Conexión establecida con el servidor Local.");
				}else {
					System.err.println("No se pudo establecer conexión con el servidor local. Asegurese de que se encuentra online. Reintentando conexión...\n");
					socket.disconnect();
					socket = null;
				}
				
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			
			
			
			
			
    	}
    }
    
    private void detectLocalIPAddress() {
		url = PropertiesUtils.getLocalServerURL();
	}

	public void sendPLCSLikeJSON(Station s) {
		if (disabled) {
			return;
		}
		
    	ObjectMapper objectMapper = new ObjectMapper();
    	
    	try {
			String json = objectMapper.writeValueAsString(s);
			
			if (PropertiesUtils.getDebugLog()) {
				System.out.println("\nEnviando al servidor plcs de " + s.getClientID() + " - "  + s.getFrigName() + ", cantidad de plcs: " + s.getPlcs().size() + "\n");
			}
			socket.emit("envioVariables", json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    }
    
    public void deletePLC(PLC p) {
    	if (disabled) {
			return;
		}
    	
		System.out.println("PLC Offline con id " + p.getId());
		socket.emit("plcOffline", p.getId());
    }
    
    public Boolean isDisabled() {
    	return disabled;
    }
    
    public void disable() {
    	disabled = true;
    }

	public Boolean isConnected() {
		return connected;
	}
    
    
    
    
}