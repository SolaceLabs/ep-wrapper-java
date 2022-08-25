package com.solace.labs.epapi.wrapper;

import com.solace.labs.epapi.client.model.*;

public enum EventPortalObjectType {

	DOMAIN(ApplicationDomain.class),
	APPLICATION(Application.class),
	APPLICATION_VERSION(ApplicationVersion.class),
	EVENT(Event.class),
	EVENT_VERSION(EventVersion.class),
	SCHEMA(SchemaObject.class),
	SCHEMA_VERSION(SchemaVersion.class),
	EVENT_API(EventApi.class),
	EVENT_API_VERSION(EventApiVersion.class),
	N_A(null)
	;
	
	
	private final Class<?> clazz;
	
	private EventPortalObjectType(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	/*
	 * app -> domain
	 * appV -> app
	 * event -> [ app ]
	 * eventV -> [ appV ], event
	 * schema -> 
	 */
	
	public Class<?> getClazz() {
		return clazz;
	}
	
	public EventPortalObjectType getType(Object object) {
		if (object == null) return N_A;
		Class<? extends Object> clazz = object.getClass();
		if (clazz.equals(ApplicationDomain.class)) return DOMAIN;
		else if (clazz.equals(Application.class)) return APPLICATION;
		else if (clazz.equals(ApplicationVersion.class)) return APPLICATION_VERSION;
		else if (clazz.equals(Event.class)) return EVENT;
		else if (clazz.equals(EventVersion.class)) return EVENT_VERSION;
		else if (clazz.equals(SchemaObject.class)) return SCHEMA;
		else if (clazz.equals(SchemaVersion.class)) return SCHEMA_VERSION;
		else if (clazz.equals(EventApi.class)) return EVENT_API;
		else if (clazz.equals(EventApiVersion.class)) return EVENT_API_VERSION;
		return N_A;
	}
	
	
}
