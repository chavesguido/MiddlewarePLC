package com.coiron.connections;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.Map.Entry;

import com.coiron.controllers.Station;
import com.coiron.model.PLC;
import com.coiron.utils.PropertiesUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class SocketConnection implements Runnable{
	private static SocketConnection instance = null;
	
    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;
    String message;
    
    private SocketConnection(){}
    
    public static SocketConnection getInstance() {
    	if(instance == null) {
    		instance = new SocketConnection();
    	}
    	return instance;
    }
    
    @Override
    public void run() {
    	
			try {
				System.out.println("Estableciendo conexión con el servidor web...");
				
				socket = IO.socket( PropertiesUtils.getServerURL() );
				socket.on("editarVariables", new Emitter.Listener() {
					
					public void call(Object... arg0) {
						try {
							ObjectMapper objectMapper = new ObjectMapper();
							
							PLC plcServer = objectMapper.readValue(arg0[0].toString(), PLC.class);
							
							if(plcServer == null) return;
							
//							System.out.println("mensaje del servidor: " + plcServer.toString());
							
							//TODO
//							Station.getInstance().updatePLC(plcServer.getId(), plcServer.getVariables());
							
							
							//TODO
							socket.emit("resp", "editado");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
				});
				
				socket.connect();
				
				System.out.println("Conexión establecida.");
				
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
    }
    
    public void sendPLCSLikeJSON(Station s) {
    	ObjectMapper objectMapper = new ObjectMapper();
    	
    	try {
			String json = objectMapper.writeValueAsString(s);
			
			socket.emit("envioVariables", json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    	
    }
    
}