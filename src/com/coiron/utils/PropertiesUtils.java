package com.coiron.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtils {
	
	private static final String PROPERTIES_FILEPATH = "properties.ini";
	
	private static String localServerURL = null;
	private static String cloudServerURL = null;
	private static String clientID = null;
	private static String frigName = null;
	private static String usernamePLC = null;
	private static String passwordPLC = null;
	private static String formId = null;
	private static String formAction = null;
	private static String usernameFormId = null;
	private static String passwordFormId = null;
	private static String PLCAdminURL = null;
	private static String PLCWebServerURL = null;
	private static String cantPLCS = null;
	private static Boolean onlyLocal = null;
	private static Boolean disabledLocal = null;
	private static Boolean debugLog = null;
	
	public static String getLocalServerURL() {
		if(localServerURL == null) {
			localServerURL = readProperty("localServerURL");
		}
		return localServerURL;
	}
	
	public static String getCloudServerURL() {
		if(cloudServerURL == null) {
			cloudServerURL = readProperty("cloudServerURL");
		}
		return cloudServerURL;
	}
	
	public static String getClientID() {
		if(clientID == null) {
			clientID = readProperty("clientID");
		}
		return clientID;
	}
	
	public static String getFrigName() {
		if(frigName == null) {
			frigName = readProperty("frigName");
		}
		return frigName;
	}
	
	public static String getUsernamePLC() {
		if(usernamePLC == null) {
			usernamePLC = readProperty("usernamePLC");
		}
		return usernamePLC;
	}
	
	public static String getPasswordPLC() {
		if(passwordPLC == null) {
			passwordPLC = readProperty("passwordPLC");
		}
		return passwordPLC;
	}
	
	public static String getFormId() {
		if(formId == null) {
			formId = readProperty("formId");
		}
		return formId;
	}
	
	public static String getFormAction() {
		if(formAction == null) {
			formAction = readProperty("formAction");
		}
		return formAction;
	}
	
	public static String getUsernameFormId() {
		if(usernameFormId == null) {
			usernameFormId = readProperty("usernameFormId");
		}
		return usernameFormId;
	}
	
	public static String getPasswordFormId() {
		if(passwordFormId == null) {
			passwordFormId = readProperty("passwordFormId");
		}
		return passwordFormId;
	}
	
	public static String getPLCAdminURL() {
		if(PLCAdminURL == null) {
			PLCAdminURL = readProperty("PLCAdminURL");
		}
		return PLCAdminURL;
	}
	
	public static String getPLCWebServerURL() {
		if(PLCWebServerURL == null) {
			PLCWebServerURL = readProperty("PLCWebServerURL");
		}
		return PLCWebServerURL;
	}

	public static String getCantPLCS() {
		if(cantPLCS == null) {
			cantPLCS = readProperty("cantPLCS");
		}
		return cantPLCS;
	}
	
	public static Boolean getOnlyLocal() {
		if(onlyLocal == null) {
			onlyLocal = readProperty("onlyLocal").equals("1") ? true : false;
		}
		return onlyLocal;
	}
	
	public static Boolean getDisabledLocal() {
		if(disabledLocal == null) {
			disabledLocal = readProperty("disabledLocal").equals("1") ? true : false;
		}
		return disabledLocal;
	}

	public static Boolean getDebugLog() {
		if(debugLog == null) {
			debugLog = readProperty("debugLog").equals("1") ? true : false;
		}
		return debugLog;
	}
	
	
	public static String readProperty(String name) {

		Properties prop = new Properties();
		InputStream input = null;
		String value = null;
		
		try {
			String filename = PROPERTIES_FILEPATH;
			input = PropertiesUtils.class.getClassLoader().getResourceAsStream(filename);
			if (input == null) {
				System.out.println("No se pudo encontrar el archivo de configuración: " + filename + ". Asegurese de que se encuentre en la misma ruta que el .jar.\nPresione enter para cerrar.");
				System.in.read();
				System.exit(0);
			}

			prop.load(input);

			value = prop.getProperty(name);
			
			if(value == null) {
				System.out.println("No se ha encontrado la propiedad: " + name + ". Asegurese de que el archivo " + filename + " esté bien configurado.\nPresione enter para cerrar.");
				System.in.read();
				System.exit(0);
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(0);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
		
		return value;

	}

}
