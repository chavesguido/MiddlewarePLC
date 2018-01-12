package com.coiron.connections;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;

import com.coiron.controllers.Station;
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
				socket = IO.socket( PropertiesUtils.getServerURL() );
				socket.on("testResp", new Emitter.Listener() {
					
					public void call(Object... arg0) {
						//TODO
						//TODO
						//TODO
						for(Object o : arg0)
							System.out.println((String)o.toString());
					}
					
				});
				
				socket.connect();
				
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
    
    public void sendPLCSLikeJSON(Station s) {
    	ObjectMapper objectMapper = new ObjectMapper();
    	
    	try {
			String json = objectMapper.writeValueAsString(s);
			
			socket.emit("test", json);
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    	
    }
}