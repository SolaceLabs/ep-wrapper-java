package community.solace.ep.wrapper;

public class Search {

	
	
	
	public static Object getEventPortalObject(String id) {
		if (!EventPortalWrapper.INSTANCE.isLoaded()) return null;
		Object o = null;
		o = EventPortalWrapper.INSTANCE.getDomain(id);
		if (o != null) return o;
		o = EventPortalWrapper.INSTANCE.getApplication(id);
		if (o != null) return o;
		o = EventPortalWrapper.INSTANCE.getApplicationVersion(id);
		if (o != null) return o;
		o = EventPortalWrapper.INSTANCE.getEvent(id);
		if (o != null) return o;
		o = EventPortalWrapper.INSTANCE.getEventVersion(id);
		if (o != null) return o;
		o = EventPortalWrapper.INSTANCE.getSchema(id);
		if (o != null) return o;
		o = EventPortalWrapper.INSTANCE.getSchemaVersion(id);
		if (o != null) return o;
		o = EventPortalWrapper.INSTANCE.getEventApi(id);
		if (o != null) return o;
		o = EventPortalWrapper.INSTANCE.getEventVersion(id);
		if (o != null) return o;
		return null;
	}
	
	public static EventPortalObjectType getEventPortalObjectType(String id) {
		if (!EventPortalWrapper.INSTANCE.isLoaded()) return null;
		Object o = null;
		o = EventPortalWrapper.INSTANCE.getDomain(id);
		if (o != null) return EventPortalObjectType.DOMAIN;
		o = EventPortalWrapper.INSTANCE.getApplication(id);
		if (o != null) return EventPortalObjectType.APPLICATION;
		o = EventPortalWrapper.INSTANCE.getApplicationVersion(id);
		if (o != null) return EventPortalObjectType.APPLICATION_VERSION;
		o = EventPortalWrapper.INSTANCE.getEvent(id);
		if (o != null) return EventPortalObjectType.EVENT;
		o = EventPortalWrapper.INSTANCE.getEventVersion(id);
		if (o != null) return EventPortalObjectType.EVENT_VERSION;
		o = EventPortalWrapper.INSTANCE.getSchema(id);
		if (o != null) return EventPortalObjectType.SCHEMA;
		o = EventPortalWrapper.INSTANCE.getSchemaVersion(id);
		if (o != null) return EventPortalObjectType.SCHEMA_VERSION;
		o = EventPortalWrapper.INSTANCE.getEventApi(id);
		if (o != null) return EventPortalObjectType.EVENT_API;
		o = EventPortalWrapper.INSTANCE.getEventVersion(id);
		if (o != null) return EventPortalObjectType.EVENT_API_VERSION;
		return EventPortalObjectType.N_A;
	}
	
	
	
	
	public static Object getEventPortalByObject(String id, EventPortalObjectType type) {
		switch (type) {
		case DOMAIN: break;
		default: return null;
		}
		
		
		
		return null;
		
	}
	
	
	
}
