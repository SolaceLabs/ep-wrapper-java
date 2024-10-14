package community.solace.ep.wrapper;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import community.solace.ep.client.model.Application;
import community.solace.ep.client.model.ApplicationDomain;
import community.solace.ep.client.model.ApplicationVersion;
import community.solace.ep.client.model.Event;
import community.solace.ep.client.model.EventApi;
import community.solace.ep.client.model.EventApiProduct;
import community.solace.ep.client.model.EventApiProductVersion;
import community.solace.ep.client.model.EventApiVersion;
import community.solace.ep.client.model.EventVersion;
import community.solace.ep.client.model.SchemaObject;
import community.solace.ep.client.model.SchemaVersion;

public enum PortalLinksUtils {

	DOMAIN_URL("https://%s/ep/designer/domains/%s/applications",
			SupportedObjectType.DOMAIN),
	DOMAIN_EVENT_API_URL("https://%s/ep/designer/domains/%s/eventApis",
			SupportedObjectType.DOMAIN),
	APP_URL("https://%s/ep/designer/domains/%s/applications?selectedId=%s",
			SupportedObjectType.DOMAIN, SupportedObjectType.APPLICATION),
	APP_VER_URL("https://%s/ep/designer/domains/%s/applications/%s?selectedVersionId=%s",
			SupportedObjectType.DOMAIN, SupportedObjectType.APPLICATION, SupportedObjectType.APPLICATION_VERSION),
	EVENT_URL("https://%s/ep/designer/domains/%s/events?selectedId=%s",
			SupportedObjectType.DOMAIN, SupportedObjectType.EVENT),
	EVENT_VER_URL("https://%s/ep/designer/domains/%s/events/%s?selectedVersionId=%s",
			SupportedObjectType.DOMAIN, SupportedObjectType.EVENT, SupportedObjectType.EVENT_VERSION),
	SCHEMA_URL("https://%s/ep/designer/domains/%s/schemas?selectedId=%s",
			SupportedObjectType.DOMAIN, SupportedObjectType.SCHEMA),
	SCHEMA_VER_URL("https://%s/ep/designer/domains/%s/schemas/%s?selectedVersionId=%s",
			SupportedObjectType.DOMAIN, SupportedObjectType.SCHEMA, SupportedObjectType.SCHEMA_VERSION),
	EVENT_API_URL("https://%s/ep/designer/domains/%s/eventApis?selectedId=%s",
			SupportedObjectType.DOMAIN, SupportedObjectType.EVENT_API),
	EVENT_API_VER_URL("https://%s/ep/designer/domains/%s/eventApis/%s?selectedVersionId=%s",
			SupportedObjectType.DOMAIN, SupportedObjectType.EVENT_API, SupportedObjectType.EVENT_API_VERSION),
	EVENT_API_PRODUCT_URL("https://%s/ep/designer/domains/%s/eventApiProducts?selectedId=%s",
			SupportedObjectType.DOMAIN, SupportedObjectType.EVENT_API_PRODUCT),
	EVENT_API_PRODUCT_VER_URL("https://%s/ep/designer/domains/%s/eventApiProducts/%s?selectedVersionId=%s",
			SupportedObjectType.DOMAIN, SupportedObjectType.EVENT_API_PRODUCT, SupportedObjectType.EVENT_API_PRODUCT_VERSION),
	;

	private static final Logger logger = LoggerFactory.getLogger(PortalLinksUtils.class);

	final String url;
	final SupportedObjectType[] params;

	PortalLinksUtils(String url, SupportedObjectType... params) {
		this.url = url;
		this.params = params;
	}

	public String getUrl() {
		return url;
	}
	
	public SupportedObjectType[] getParams() {
		return Arrays.copyOf(params, params.length);
	}
	
//	public static PortalLinksUtils getUrlType(SupportedObjectType type) {
//		
//	}
	
	
	public static String generateUrl(Object o, String urlBase) {
		
		SupportedObjectType type = SupportedObjectType.getType(o);
		logger.info("generateUrl() called for object class " + o.getClass().getSimpleName() + ", SupportedType: " + type);
		switch (type) {
		case APPLICATION: {
			Application app = (Application)o;
			return String.format(APP_URL.url, urlBase, app.getApplicationDomainId(), app.getId());
		}
		case APPLICATION_VERSION: {
			ApplicationVersion appVer = (ApplicationVersion)o;
			Application app = EventPortalWrapper.INSTANCE.getApplication(appVer.getApplicationId());
			return String.format(APP_VER_URL.url, urlBase, app.getApplicationDomainId(), app.getId(), appVer.getId());
		}
		case CONFIGURATION:
			break;
		case CONFIGURATION_TYPE:
			break;
		case CONSUMER:
			break;
		case CUSTOM_ATTRIBUTE:
			break;
		case DOMAIN: {
			ApplicationDomain domain = (ApplicationDomain)o;
			return String.format(DOMAIN_URL.url, urlBase, domain.getId());
		}
		case ENUM:
			break;
		case ENUM_VERSION:
			break;
		case ENVIRONMENT:
			break;
		case EVENT: {
			Event event = (Event)o;
			return String.format(EVENT_URL.url, urlBase, event.getApplicationDomainId(), event.getId());
		}
		case EVENT_API: {
			EventApi eventApi = (EventApi)o;
			return String.format(EVENT_API_URL.url, urlBase, eventApi.getApplicationDomainId(), eventApi.getId());
		}
		case EVENT_API_PRODUCT: {
			EventApiProduct eventApiProduct = (EventApiProduct)o;
			return String.format(EVENT_API_PRODUCT_URL.url, urlBase, eventApiProduct.getApplicationDomainId(), eventApiProduct.getId());
		}
		case EVENT_API_PRODUCT_VERSION: {
			EventApiProductVersion eventApiProductVersion = (EventApiProductVersion)o;
			EventApiProduct eventApiProduct = EventPortalWrapper.INSTANCE.getEventApiProduct(eventApiProductVersion.getEventApiProductId());
			return String.format(EVENT_API_PRODUCT_VER_URL.url, urlBase, eventApiProduct.getApplicationDomainId(), eventApiProduct.getId(), eventApiProductVersion.getId());
		}
		case EVENT_API_VERSION: {
			EventApiVersion eventApiVersion = (EventApiVersion)o;
			EventApi eventApi = EventPortalWrapper.INSTANCE.getEventApi(eventApiVersion.getEventApiId());
			return String.format(EVENT_API_VER_URL.url, urlBase, eventApi.getApplicationDomainId(), eventApi.getId(), eventApiVersion.getId());
		}
		case EVENT_MESH:
			break;
		case EVENT_VERSION: {
			EventVersion eventVer = (EventVersion)o;
			Event event = EventPortalWrapper.INSTANCE.getEvent(eventVer.getEventId());
			return String.format(EVENT_VER_URL.url, urlBase, event.getApplicationDomainId(), event.getId(), eventVer.getId());
		}
		case N_A:
			break;
		case SCHEMA: {
			SchemaObject schema = (SchemaObject)o;
			return String.format(SCHEMA_URL.url, urlBase, schema.getApplicationDomainId(), schema.getId());
		}
		case SCHEMA_VERSION: {
			SchemaVersion schemaVer = (SchemaVersion)o;
			SchemaObject schema = EventPortalWrapper.INSTANCE.getSchema(schemaVer.getSchemaId());
			return String.format(SCHEMA_VER_URL.url, urlBase, schema.getApplicationDomainId(), schema.getId(), schemaVer.getId());
		}
		case STATE:
			break;
		case TOPIC_DOMAIN:
			break;
		case USER:
			break;
		default:
			break;
		
		}
		
		
		
		
		
		
		
		return "https://solace.dev";
	}
	

	//	public int getNumReplacements() {
	//		int count = 0;
	//		int index = 0;
	//		while (index < url.length()) {
	//			int index
	//		}
	//	}


}
