package community.solace.ep.wrapper;

import java.util.HashMap;
import java.util.Map;

import community.solace.ep.client.model.Application;
import community.solace.ep.client.model.ApplicationDomain;
import community.solace.ep.client.model.ApplicationVersion;
import community.solace.ep.client.model.Consumer;
import community.solace.ep.client.model.Environment;
import community.solace.ep.client.model.Event;
import community.solace.ep.client.model.EventApi;
import community.solace.ep.client.model.EventApiProduct;
import community.solace.ep.client.model.EventApiProductVersion;
import community.solace.ep.client.model.EventApiVersion;
import community.solace.ep.client.model.EventMesh;
import community.solace.ep.client.model.EventVersion;
import community.solace.ep.client.model.SchemaObject;
import community.solace.ep.client.model.SchemaVersion;
import community.solace.ep.client.model.TopicAddressEnum;
import community.solace.ep.client.model.TopicAddressEnumVersion;
import community.solace.ep.client.model.TopicDomain;

/**
 * Helper Enum to determine what type of Event Portal object you're dealing with.
 * Useful for switch statements for rendering views.
 */
public enum EventPortalObjectType {

	DOMAIN(ApplicationDomain.class, "Application Domain", "Domain"),
	APPLICATION(Application.class, "Application", "App"),
	APPLICATION_VERSION(ApplicationVersion.class, "Application Version", "App Ver"),
	EVENT(Event.class, "Event", "Event"),
	EVENT_VERSION(EventVersion.class, "Event Version", "Event Ver"),
	SCHEMA(SchemaObject.class, "Schema", "Schema"),
	SCHEMA_VERSION(SchemaVersion.class, "Schema Version", "Schema Ver"),
	ENUM(TopicAddressEnum.class, "Topic Address Enum", "Enum"),
	ENUM_VERSION(TopicAddressEnumVersion.class, "Topic Address Enum Version", "Enum Ver"),
	EVENT_API(EventApi.class, "Event API", "Event API"),
	EVENT_API_VERSION(EventApiVersion.class, "Event API Version", "Event API Ver"),
	EVENT_API_PRODUCT(EventApiProduct.class, "", ""),
	EVENT_API_PRODUCT_VERSION(EventApiProductVersion.class, "", ""),
	CONSUMER(Consumer.class, "", ""),
	EVENT_MESH(EventMesh.class, "", ""),
	TOPIC_DOMAIN(TopicDomain.class, "", ""),
	ENVIRONMENT(Environment.class, "", ""),
	N_A(null, "", ""),
	;
	
	private final Class<?> clazz;
	private final String fullName;
	private final String shortName;
	
	
	private EventPortalObjectType(Class<?> clazz, String fullName, String shortName) {
		this.clazz = clazz;
		this.fullName = fullName;
		this.shortName = shortName;
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
	
	public String getShortName() {
		return shortName;
	}
	
	/**
	 * Pass in a Swagger Event Portal object, and return a particular enum.
	 * @param object ideally an Event Portal object
	 * @return an enum describing the type of Event Portal object, or "N_A" if null or invalid
	 */
	public static EventPortalObjectType getType(Object object) {
		if (object == null) return N_A;
		Class<? extends Object> clazz = object.getClass();
		if (clazz.equals(DOMAIN.clazz)) return DOMAIN;
		else if (clazz.equals(APPLICATION.clazz)) return APPLICATION;
		else if (clazz.equals(APPLICATION_VERSION.clazz)) return APPLICATION_VERSION;
		else if (clazz.equals(EVENT.clazz)) return EVENT;
		else if (clazz.equals(EVENT_VERSION.clazz)) return EVENT_VERSION;
		else if (clazz.equals(SCHEMA.clazz)) return SCHEMA;
		else if (clazz.equals(SCHEMA_VERSION.clazz)) return SCHEMA_VERSION;
		else if (clazz.equals(ENUM.clazz)) return ENUM;
		else if (clazz.equals(ENUM_VERSION.clazz)) return ENUM_VERSION;
		else if (clazz.equals(EVENT_API.clazz)) return EVENT_API;
		else if (clazz.equals(EVENT_API_VERSION.clazz)) return EVENT_API_VERSION;
		else if (clazz.equals(EVENT_API_PRODUCT.clazz)) return EVENT_API_PRODUCT;
		else if (clazz.equals(EVENT_API_PRODUCT_VERSION.clazz)) return EVENT_API_PRODUCT_VERSION;
		else if (clazz.equals(CONSUMER.clazz)) return CONSUMER;
		
		return N_A;
	}
	
	
}
