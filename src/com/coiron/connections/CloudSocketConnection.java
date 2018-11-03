package com.coiron.connections;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import com.coiron.controllers.Station;
import com.coiron.model.PLC;
import com.coiron.utils.PropertiesUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class CloudSocketConnection implements Runnable {
	private static CloudSocketConnection instance = null;
	public final static Object obj = new Object();
	
    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;
    String message;
    Boolean connected = false;
    String url = PropertiesUtils.getCloudServerURL();
    Boolean disabled;
    
    
    private CloudSocketConnection(){
    	disabled = PropertiesUtils.getOnlyLocal();
    }
    
    public static CloudSocketConnection getInstance() {
    	if(instance == null) {
    		instance = new CloudSocketConnection();
    	}
    	return instance;
    }
    
    @Override
    public void run() {
    	if (disabled) {
			return;
		}
    	
    	while(!connected) {
    	
			try {
				System.out.println("Estableciendo conexión con el servidor Cloud...");
				
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
						System.out.println("Socket conectado con el servidor Clod.");
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
					System.out.println("Conexión establecida con el servidor Cloud.");
				}else {
					System.out.println("\nNo se pudo establecer conexión con el servidor Cloud. Reintentando.");
					socket.disconnect();
					socket = null;
					
					synchronized (obj) {
						try {
							TimeUnit.SECONDS.sleep(15);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
				}
				
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			
			
			
			
			
    	}
    }
    
    public void sendPLCSLikeJSON(Station s) {
    	if (disabled) {
			return;
		}
    	
    	ObjectMapper objectMapper = new ObjectMapper();
    	
    	try {
			String json = objectMapper.writeValueAsString(s);
			
			
			System.out.println("\nEnviando al servidor plcs de " + s.getClientID() + " - "  + s.getFrigName() + ", cantidad de plcs: " + s.getPlcs().size() + "\n");
			socket.emit("envioVariables", json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    }
    
    public void deletePLC(PLC p) {
    	if (disabled) {
			return;
		}
    	
		System.out.println("PLC OFFLINE CON ID " + p.getId());
		socket.emit("plcOffline", p.getId());
    }

	public Boolean isConnected() {
		return connected;
	}
    
    
    
    
}