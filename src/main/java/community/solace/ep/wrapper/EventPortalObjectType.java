package community.solace.ep.wrapper;

import community.solace.ep.client.model.Application;
import community.solace.ep.client.model.ApplicationDomain;
import community.solace.ep.client.model.ApplicationVersion;
import community.solace.ep.client.model.Event;
import community.solace.ep.client.model.EventApi;
import community.solace.ep.client.model.EventApiVersion;
import community.solace.ep.client.model.EventVersion;
import community.solace.ep.client.model.SchemaObject;
import community.solace.ep.client.model.SchemaVersion;

/**
 * Helper Enum to determine what type of Event Portal object you're dealing with.
 * Useful for switch statements for rendering views.
 */
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
	
	/**
	 * Pass in a Swagger Event Portal object, and return a particular enum.
	 * @param object ideally an Event Portal object
	 * @return an enum describing the type of Event Portal object, or "N_A" if null or invalid
	 */
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
