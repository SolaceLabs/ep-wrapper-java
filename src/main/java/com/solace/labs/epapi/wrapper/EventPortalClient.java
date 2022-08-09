package com.solace.labs.epapi.wrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.solace.labs.epapi.client.ApiClient;
import com.solace.labs.epapi.client.ApiException;
import com.solace.labs.epapi.client.Configuration;
import com.solace.labs.epapi.client.api.ApplicationDomainsApi;
import com.solace.labs.epapi.client.api.ApplicationsApi;
import com.solace.labs.epapi.client.api.EventApIsApi;
import com.solace.labs.epapi.client.api.EventsApi;
import com.solace.labs.epapi.client.api.SchemasApi;
import com.solace.labs.epapi.client.api.StatesApi;
import com.solace.labs.epapi.client.model.Application;
import com.solace.labs.epapi.client.model.ApplicationDomain;
import com.solace.labs.epapi.client.model.ApplicationDomainsResponse;
import com.solace.labs.epapi.client.model.ApplicationVersion;
import com.solace.labs.epapi.client.model.ApplicationVersionsResponse;
import com.solace.labs.epapi.client.model.ApplicationsResponse;
import com.solace.labs.epapi.client.model.Event;
import com.solace.labs.epapi.client.model.EventApi;
import com.solace.labs.epapi.client.model.EventApiVersion;
import com.solace.labs.epapi.client.model.EventApiVersionsResponse;
import com.solace.labs.epapi.client.model.EventApisResponse;
import com.solace.labs.epapi.client.model.EventVersion;
import com.solace.labs.epapi.client.model.EventVersionsResponse;
import com.solace.labs.epapi.client.model.EventsResponse;
import com.solace.labs.epapi.client.model.SchemaObject;
import com.solace.labs.epapi.client.model.SchemaVersion;
import com.solace.labs.epapi.client.model.SchemasResponse;
import com.solace.labs.epapi.client.model.StateDTO;
import com.solace.labs.epapi.client.model.StatesResponse;

public enum EventPortalClient {

	INSTANCE;

    private final int PAGE_SIZE = 50;
    
    private Map<String, ApplicationDomain> domains = new LinkedHashMap<>();
    
    private Map<String, Application> applicationsById = new LinkedHashMap<>();
    private Map<String, List<Application>> applicationsByDomainId = new LinkedHashMap<>();
    private Map<String, ApplicationVersion> applicationVersionsById = new LinkedHashMap<>();
    private Map<String, List<ApplicationVersion>> applicationVersionsByApplicatoinId = new LinkedHashMap<>();

    private Map<String, Event> eventsById = new LinkedHashMap<>();
    private Map<String, List<Event>> eventsByDomainId = new LinkedHashMap<>();
    private Map<String, EventVersion> eventVersionsById = new LinkedHashMap<>();
    private Map<String, List<EventVersion>> eventVersionsByEventId = new LinkedHashMap<>();

    private Map<String, SchemaObject> schemasById = new LinkedHashMap<>();
    private Map<String, List<SchemaObject>> schemasByDomainId = new LinkedHashMap<>();
    private Map<String, SchemaVersion> schemaVersionsById = new LinkedHashMap<>();
    private Map<String, List<SchemaVersion>> schemaVersionsBySchemaId = new LinkedHashMap<>();

    private Map<String, EventApi> eventApisById = new LinkedHashMap<>();
    private Map<String, List<EventApi>> eventApisByDomainId = new LinkedHashMap<>();
    private Map<String, EventApiVersion> eventApiVersionsById = new LinkedHashMap<>();
    private Map<String, List<EventApiVersion>> eventApiVersionsByEventApiId = new LinkedHashMap<>();
    
    private Map<String, StateDTO> statesById = new LinkedHashMap<>();
    
    private String token = null;

    
    private AtomicBoolean loaded = new AtomicBoolean(false);
    
    public boolean isLoaded() {
    	return loaded.get();
    }
    
//    public boolean testConnectivity() {
//	  	ApiClient apiClient = Configuration.getDefaultApiClient();
//        apiClient.setBasePath("http://api.solace.cloud");
//        apiClient.setAccessToken(token);
////        apiClient.getHttpClient().getDns().
//        return true;
//    }

    public void setToken(String token) {
    	this.token = token;
    }
	
	public boolean load() {
		if (loaded.compareAndSet(false, true)) {
			// good to go!
		} else {  // already loaded, don't load again
			return false;
		}
	    long start = System.currentTimeMillis();
        
	  	ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setBasePath("http://api.solace.cloud");
        apiClient.setAccessToken(token);
    	try {
	        getDomainsInfo(apiClient);
	        getApplicationsInfo(apiClient);
	        getEventsInfo(apiClient);
	        getSchemaInfo(apiClient);
	        getEventApisInfo(apiClient);
	        getOtherInfo(apiClient);
	        return true;
    	} catch (ApiException e) {
    		e.printStackTrace();
    		return false;
    	} finally {
            System.out.println("EventPortalClient LOADED: " + (System.currentTimeMillis()-start) + "ms with PAGE_SIZE == " + PAGE_SIZE);
    	}
	}
	
    public void afterLoadingTests() {
        System.out.println("##################################");
        System.out.println("domains.size() = " + domains.size());
        System.out.println("appsById.size() = " + applicationsById.size());
        System.out.println("eventsById.size() = " + eventsById.size());
        System.out.println("eventVersionsById.size() = " + eventVersionsById.size());
        System.out.println("eventVersionsByEventId.size() = " + eventVersionsByEventId.size());
        
        System.out.println(EventPortalClient.INSTANCE.getDomain("7x3q0n9go0c"));
        System.out.println(applicationsByDomainId.get("7x3q0n9go0c"));
        System.out.println(eventsByDomainId.get("7x3q0n9go0c"));
        System.out.println(applicationVersionsByApplicatoinId.get("g67gm0qrxk7"));
        System.out.println(eventVersionsById.get("kb0183jdx7j"));
        System.out.println(eventApisByDomainId.get("x4oo4skfh5e"));  // Aaron test 1
        System.out.println(eventApiVersionsByEventApiId.get("ofn2yb68mf1"));  // Aaron test 1


    }
    
    
    private void getDomainsInfo(ApiClient apiClient) throws ApiException {
    	long start = System.currentTimeMillis();
        ApplicationDomainsApi apiDomains = new ApplicationDomainsApi(apiClient);
        ApplicationDomainsResponse response;
        int page = 1;
        do {
        	response = apiDomains.getApplicationDomains(PAGE_SIZE, page++, null, null, Collections.singletonList("stats"));
        	for (ApplicationDomain domain : response.getData()) {
        		domains.put(domain.getId(), domain);
        	}
        } while (((Map<?, ?>)response.getMeta().get("pagination")).get("nextPage") != null);
        System.out.printf("getDomainsInfo() took %dms.%n", System.currentTimeMillis() - start);
    }
    
    private void getApplicationsInfo(ApiClient apiClient) throws ApiException {
    	long start = System.currentTimeMillis();
        ApplicationsApi apiApps = new ApplicationsApi(apiClient);
        int page = 1;
        ApplicationsResponse response2;
        do {
        	response2 = apiApps.getApplications(PAGE_SIZE, page++, null, null, null, null, null);
        	for (Application app : response2.getData()) {
        		applicationsById.put(app.getId(), app);
        		if (applicationsByDomainId.get(app.getApplicationDomainId()) == null) {
        			applicationsByDomainId.put(app.getApplicationDomainId(), new ArrayList<>());
        		}
        		applicationsByDomainId.get(app.getApplicationDomainId()).add(app);
        	}
        } while (((Map<?, ?>)response2.getMeta().get("pagination")).get("nextPage") != null);
        
        // app versions
        ApplicationVersionsResponse response3;
        page = 1;
        do {
        	response3 = apiApps.getApplicationVersions(PAGE_SIZE, page++, null, null);
            for (ApplicationVersion appVer : response3.getData()) {
            	applicationVersionsById.put(appVer.getId(), appVer);
            	if (applicationVersionsByApplicatoinId.get(appVer.getApplicationId()) == null) {
            		applicationVersionsByApplicatoinId.put(appVer.getApplicationId(), new ArrayList<>());
            	}
            	applicationVersionsByApplicatoinId.get(appVer.getApplicationId()).add(appVer);
            }
        } while (((Map<?, ?>)response3.getMeta().get("pagination")).get("nextPage") != null);
        System.out.printf("getApplicationsInfo() took %dms.%n", System.currentTimeMillis() - start);
    }
    
    private void getEventsInfo(ApiClient apiClient) throws ApiException {
    	long start = System.currentTimeMillis();
        EventsApi eventsApi = new EventsApi(apiClient);
        EventsResponse eventsReponse;
        int page = 1;
        do {
        	eventsReponse = eventsApi.getEvents(PAGE_SIZE, page++, null, null, null, null, null, null);
	        for (Event event : eventsReponse.getData()) {
	        	eventsById.put(event.getId(), event);
	        	if (eventsByDomainId.get(event.getApplicationDomainId()) == null) {
	        		eventsByDomainId.put(event.getApplicationDomainId(), new ArrayList<>());
	        	}
	        	eventsByDomainId.get(event.getApplicationDomainId()).add(event);
	        }
        } while (((Map<?, ?>)eventsReponse.getMeta().get("pagination")).get("nextPage") != null);
    	
        // event versions
        EventVersionsResponse eventVersionsResponse;
        page = 1;
        do {
        	eventVersionsResponse = eventsApi.getEventVersions(PAGE_SIZE, page++, null);
        	for (EventVersion eventVersion : eventVersionsResponse.getData()) {
        		eventVersionsById.put(eventVersion.getId(), eventVersion);
        		if (eventVersionsByEventId.get(eventVersion.getEventId()) == null) {
        			eventVersionsByEventId.put(eventVersion.getEventId(), new ArrayList<>());
        		}
        		eventVersionsByEventId.get(eventVersion.getEventId()).add(eventVersion);
        	}
        } while (((Map<?, ?>)eventVersionsResponse.getMeta().get("pagination")).get("nextPage") != null);
        System.out.printf("getEventsInfo() took %dms.%n", System.currentTimeMillis() - start);
    }
    
    private void getSchemaInfo(ApiClient apiClient) throws ApiException {
    	long start = System.currentTimeMillis();
        SchemasApi schemasApi = new SchemasApi(apiClient);
        SchemasResponse schemasReponse;
        int page = 1;
        do {
        	schemasReponse = schemasApi.getSchemas(PAGE_SIZE, page++, null, null, null, null, null, null);
	        for (SchemaObject schema : schemasReponse.getData()) {
	        	schemasById.put(schema.getId(), schema);
	        	if (schemasByDomainId.get(schema.getApplicationDomainId()) == null) {
	        		schemasByDomainId.put(schema.getApplicationDomainId(), new ArrayList<>());
	        	}
	        	schemasByDomainId.get(schema.getApplicationDomainId()).add(schema);
	        }
        } while (((Map<?, ?>)schemasReponse.getMeta().get("pagination")).get("nextPage") != null);
        
        // schemas versions
        /*
        SchemaVersionResponse schemaVersionsResponse;
        page = 1;
        do {
        	
        	schemaVersionsResponse = schemasApi.getSchemaVersions(PAGE_SIZE, page++, null);
        	schemaVersionsResponse.getData().
        	for (SchemaVersion schemaVersion : schemaVersionsResponse.getData()) {
        		schemaVersionsById.put(schemaVersion.getId(), schemaVersion);
        		if (schemaVersionsBySchemaId.get(schemaVersion.getSchemaId()) == null) {
        			schemaVersionsBySchemaId.put(schemaVersion.getSchemaId(), new ArrayList<>());
        		}
        		schemaVersionsBySchemaId.get(schemaVersion.getSchemaId()).add(schemaVersion);
        	}
        } while (((Map<?, ?>)schemaVersionsResponse.getMeta().get("pagination")).get("nextPage") != null);
*/
        // hacky way of doing this, but not correct b/c this only finds schemas that are being used by events
        for (String eventVersionId : eventVersionsById.keySet()) {
        	if (eventVersionsById.get(eventVersionId).getSchemaVersionId() == null) continue;
        	SchemaVersion schemaVersion = schemasApi.getSchemaVersion(eventVersionsById.get(eventVersionId).getSchemaVersionId()).getData();
        	schemaVersionsById.put(schemaVersion.getId(), schemaVersion);
    		if (schemaVersionsBySchemaId.get(schemaVersion.getSchemaId()) == null) {
    			schemaVersionsBySchemaId.put(schemaVersion.getSchemaId(), new ArrayList<>());
    		}
    		schemaVersionsBySchemaId.get(schemaVersion.getSchemaId()).add(schemaVersion);
        }
        System.out.printf("getSchemaInfo() took %dms.%n", System.currentTimeMillis() - start);
    }
    
    private void getEventApisInfo(ApiClient apiClient) throws ApiException {
    	long start = System.currentTimeMillis();
        EventApIsApi eventApisApi = new EventApIsApi(apiClient);
        EventApisResponse eventApisReponse;
        int page = 1;
        do {
        	eventApisReponse = eventApisApi.getEventApis(PAGE_SIZE, page++, null, null, null, null, null, null, null);
	        for (EventApi eventApi : eventApisReponse.getData()) {
	        	eventApisById.put(eventApi.getId(), eventApi);
	        	if (eventApisByDomainId.get(eventApi.getApplicationDomainId()) == null) {
	        		eventApisByDomainId.put(eventApi.getApplicationDomainId(), new ArrayList<>());
	        	}
	        	eventApisByDomainId.get(eventApi.getApplicationDomainId()).add(eventApi);
	        }
        } while (((Map<?, ?>)eventApisReponse.getMeta().get("pagination")).get("nextPage") != null);
        
        // event API versions
        EventApiVersionsResponse eventApiVersionsResponse;// = eventsApi.getEventVersions(3, null, null);
        page = 1;
        do {
        	eventApiVersionsResponse = eventApisApi.getEventApiVersions(PAGE_SIZE, page++, null, null, null);
        	for (EventApiVersion eventApiVersion : eventApiVersionsResponse.getData()) {
        		eventApiVersionsById.put(eventApiVersion.getId(), eventApiVersion);
        		if (eventApiVersionsByEventApiId.get(eventApiVersion.getEventApiId()) == null) {
        			eventApiVersionsByEventApiId.put(eventApiVersion.getEventApiId(), new ArrayList<>());
        		}
        		eventApiVersionsByEventApiId.get(eventApiVersion.getEventApiId()).add(eventApiVersion);
        	}
        } while (((Map<?, ?>)eventApiVersionsResponse.getMeta().get("pagination")).get("nextPage") != null);
        System.out.printf("loadEventApisInfo() took %dms.%n", System.currentTimeMillis() - start);
    }

    private void getOtherInfo(ApiClient apiClient) throws ApiException {
    	long start = System.currentTimeMillis();
    	// states
        StatesApi statesApi = new StatesApi(apiClient);
        StatesResponse statesResponse = statesApi.getStates();
        for (StateDTO state : statesResponse.getData()) {
        	statesById.put(state.getId(), state);
        }
        
        // level separator
        
        System.out.printf("getOtherInfo() took %dms.%n", System.currentTimeMillis() - start);
    }
	
	
    
    
    
    
    
    
    
    
    
    // PUBLIC ACCESSOR METHOD /////////////////////////////////////////
    
    public ApplicationDomain getDomain(String id) {
    	return domains.get(id);
    }
	
    public Collection<ApplicationDomain> getDomains() {
    	return Collections.unmodifiableCollection(domains.values());
    }
    
    public Application getApplication(String id) {
    	return applicationsById.get(id);
    }
    
    public List<Application> getApplicationsForDomainId(String domainId) {
		if (applicationsByDomainId.get(domainId) == null) return Collections.emptyList();
    	return Collections.unmodifiableList(applicationsByDomainId.get(domainId));
    }
    
    public ApplicationVersion getApplicationVersion(String id) {
    	return applicationVersionsById.get(id);
    }
    
    public List<ApplicationVersion> getApplicationVersionsForApplicationId(String applicationId) {
		if (applicationVersionsByApplicatoinId.get(applicationId) == null) return Collections.emptyList();
    	return Collections.unmodifiableList(applicationVersionsByApplicatoinId.get(applicationId));
    }
    
	public Event getEvent(String id) {
		return eventsById.get(id);
	}
	
	public List<Event> getEventsForDomainId(String domainId) {
		if (eventsByDomainId.get(domainId) == null) return Collections.emptyList();
		return Collections.unmodifiableList(eventsByDomainId.get(domainId));
	}
	
	public EventVersion getEventVersion(String id) {
		return eventVersionsById.get(id);
	}
	
	public List<EventVersion> getEventVersionsForEventId(String eventId) {
		if (eventVersionsByEventId.get(eventId) == null) return Collections.emptyList();
		return Collections.unmodifiableList(eventVersionsByEventId.get(eventId));
	}
	
	public SchemaObject getSchema(String id) {
		return schemasById.get(id);
	}
	
	public List<SchemaObject> getSchemasForDomainId(String domainId) {
		if (schemasByDomainId.get(domainId) == null) return Collections.emptyList();
		return Collections.unmodifiableList(schemasByDomainId.get(domainId));
	}
	
	/**
	 * 
	 * @param id
	 * @return null if not found, otherwise object
	 */
	public SchemaVersion getSchemaVersion(String id) {
		return schemaVersionsById.get(id);
	}
	
	/**
	 * 
	 * @param schemaId
	 * @return empty list if not found, otherwise list
	 */
	public List<SchemaVersion> getSchemaVersionsForSchemaId(String schemaId) {
		if (schemaVersionsBySchemaId.get(schemaId) == null) return Collections.emptyList();
		return Collections.unmodifiableList(schemaVersionsBySchemaId.get(schemaId));
	}
	
	
	
	
	
	public StateDTO getState(String id) {
		return statesById.get(id);
	}
	
	
	
	
}
