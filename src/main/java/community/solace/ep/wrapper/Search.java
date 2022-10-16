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
	
	public static SupportedObjectType getEventPortalObjectType(String id) {
		if (!EventPortalWrapper.INSTANCE.isLoaded()) return null;
		Object o = null;
		o = EventPortalWrapper.INSTANCE.getDomain(id);
		if (o != null) return SupportedObjectType.DOMAIN;
		o = EventPortalWrapper.INSTANCE.getApplication(id);
		if (o != null) return SupportedObjectType.APPLICATION;
		o = EventPortalWrapper.INSTANCE.getApplicationVersion(id);
		if (o != null) return SupportedObjectType.APPLICATION_VERSION;
		o = EventPortalWrapper.INSTANCE.getEvent(id);
		if (o != null) return SupportedObjectType.EVENT;
		o = EventPortalWrapper.INSTANCE.getEventVersion(id);
		if (o != null) return SupportedObjectType.EVENT_VERSION;
		o = EventPortalWrapper.INSTANCE.getSchema(id);
		if (o != null) return SupportedObjectType.SCHEMA;
		o = EventPortalWrapper.INSTANCE.getSchemaVersion(id);
		if (o != null) return SupportedObjectType.SCHEMA_VERSION;
		o = EventPortalWrapper.INSTANCE.getEventApi(id);
		if (o != null) return SupportedObjectType.EVENT_API;
		o = EventPortalWrapper.INSTANCE.getEventVersion(id);
		if (o != null) return SupportedObjectType.EVENT_API_VERSION;
		return SupportedObjectType.N_A;
	}
	
	
	
	
	public static Object getEventPortalByObject(String id, SupportedObjectType type) {
		switch (type) {
		case DOMAIN: break;
		default: return null;
		}
		
		
		
		return null;
		
	}
	
	
	
}
