package com.coiron.init;

import com.coiron.connections.SocketConnection;
import com.coiron.controllers.Station;

public class Init{

	public static void main(String[] args){
		
//		new Thread(Station.getInstance(), "station").start();
		
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		new Thread(SocketConnection.getInstance(), "socket").start();
		
		
	}

}
