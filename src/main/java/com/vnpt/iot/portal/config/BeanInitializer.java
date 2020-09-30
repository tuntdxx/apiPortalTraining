package com.vnpt.iot.portal.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.onem2m.protocol.protocol.RestClient;
import com.onem2m.protocol.protocol.binding.http.HTTPRestClient;
import com.onem2m.protocol.protocol.binding.mqtt.RestClientService;

import lombok.extern.slf4j.Slf4j;

/**
 * Initializer bean after start app
 * @author AnhQH
 * */
@Slf4j
public class BeanInitializer {

	public void initBean() {
		log.info("Bean initializing ...");
		//init the ConcurrentHashMap RestClientService
		Map<String, RestClientService> sclClients = new ConcurrentHashMap<String, RestClientService>();
		sclClients.put("http", new HTTPRestClient());
		RestClient.setRestClients(sclClients);
		//init the ConcurrentHashMap RestClientService done
		
		log.info("Bean Initialized ...");
	}
	
}
