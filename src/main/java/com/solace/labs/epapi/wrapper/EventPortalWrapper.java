package com.solace.labs.epapi.wrapper;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.solace.labs.epapi.client.ApiClient;
import com.solace.labs.epapi.client.ApiException;
import com.solace.labs.epapi.client.Configuration;
import com.solace.labs.epapi.client.api.ApplicationDomainsApi;
import com.solace.labs.epapi.client.api.ApplicationsApi;
import com.solace.labs.epapi.client.api.ConsumersApi;
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
import com.solace.labs.epapi.client.model.Consumer;
import com.solace.labs.epapi.client.model.ConsumersResponse;
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
import com.solace.labs.epapi.client.model.SchemaVersionsResponse;
import com.solace.labs.epapi.client.model.SchemasResponse;
import com.solace.labs.epapi.client.model.StateDTO;
import com.solace.labs.epapi.client.model.StatesResponse;

public enum EventPortalWrapper {

	INSTANCE;  // singleton pattern

    private static final int PAGE_SIZE = 50;
    
    private Map<String, ApplicationDomain> domains = new LinkedHashMap<>();
    
    private Map<String, Application> applicationsById = new LinkedHashMap<>();
    private Map<String, Set<Application>> applicationsByDomainId = new LinkedHashMap<>();
    private Map<String, ApplicationVersion> applicationVersionsById = new LinkedHashMap<>();
    private Map<String, Set<ApplicationVersion>> applicationVersionsByApplicatoinId = new LinkedHashMap<>();

    private Map<String, Event> eventsById = new LinkedHashMap<>();
    private Map<String, Set<Event>> eventsByDomainId = new LinkedHashMap<>();
    private Map<String, EventVersion> eventVersionsById = new LinkedHashMap<>();
    private Map<String, Set<EventVersion>> eventVersionsByEventId = new LinkedHashMap<>();

    private Map<String, SchemaObject> schemasById = new LinkedHashMap<>();
    private Map<String, Set<SchemaObject>> schemasByDomainId = new LinkedHashMap<>();
    private Map<String, SchemaVersion> schemaVersionsById = new LinkedHashMap<>();
    private Map<String, Set<SchemaVersion>> schemaVersionsBySchemaId = new LinkedHashMap<>();

    private Map<String, EventApi> eventApisById = new LinkedHashMap<>();
    private Map<String, Set<EventApi>> eventApisByDomainId = new LinkedHashMap<>();
    private Map<String, EventApiVersion> eventApiVersionsById = new LinkedHashMap<>();
    private Map<String, Set<EventApiVersion>> eventApiVersionsByEventApiId = new LinkedHashMap<>();

    private Map<String, Consumer> consumersById = new LinkedHashMap<>();
    private Map<String, Set<Consumer>> consumersByApplicationVersionId = new LinkedHashMap<>();

    private Map<String, StateDTO> statesById = new LinkedHashMap<>();
    
    private String token = null;

    
    public enum LoadStatus {
    	UNINITIALIZED,
    	LOADING,
    	LOADED,
    	ERROR,
    	;
    	
    	
    }
    
    private LoadStatus loadStatus = LoadStatus.UNINITIALIZED;
    private Exception loadException = null;
	private long lastRefresh = 0L;
		
	public long getLastRefresh() {
		return lastRefresh;
	}
    
    public LoadStatus getLoadStatus() {
    	return loadStatus;
    }
    
    public boolean isLoaded() {
    	return loadStatus == LoadStatus.LOADED;
    }
    
    public boolean isLoading() {
    	return loadStatus == LoadStatus.LOADING;
    }
    
    public Exception getLoadException() {
    	return loadException;
    }
    
//    public boolean testConnectivity() {
//	  	ApiClient apiClient = Configuration.getDefaultApiClient();
//        apiClient.setBasePath("http://api.solace.cloud");
//        apiClient.setAccessToken(token);
////        apiClient.getHttpClient().getDns().
//        return true;
//    }

    /** This sets the Event Portal token used for pulling data via REST API.
     * This token is not stored on disk or anything.
     */
    public void setToken(String token) {
    	this.token = token;
    }
    
    private static class PortalThreadFactory implements ThreadFactory {

    	private static AtomicInteger count = new AtomicInteger(1);
    	
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "portal_"+count.getAndIncrement());
			t.setDaemon(true);
			t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					e.printStackTrace();
				}
			});
			return t;
		}
    	
    }

	boolean loadRefresh(ExecutorService pool) {
		if (loadStatus == LoadStatus.LOADING) {
			return false;
		}
		loadStatus = LoadStatus.LOADING;
	    long start = System.currentTimeMillis();
        
	  	ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setBasePath("http://api.solace.cloud");
        apiClient.setAccessToken(token);
		Runnable domains = () -> {
			try {
				loadDomainsInfo(apiClient);
			} catch (ApiException e) {
	    		e.printStackTrace();
	            loadStatus = LoadStatus.ERROR;
	            loadException = e;
			}
		};
		pool.submit(domains);
		
		
		return false;
	}
    


	public boolean loadRefresh() {
		if (loadStatus == LoadStatus.LOADING) {
			return false;
		}
		loadStatus = LoadStatus.LOADING;
	    long start = System.currentTimeMillis();
        
	  	ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setBasePath("http://api.solace.cloud");
        apiClient.setAccessToken(token);
        
    	try {
	        loadDomainsInfo(apiClient);
//	        if (domains.size() == 0) {
//	        	loadStatus = LoadStatus.ERROR;
//	        	IllegalStateException e = new IllegalStateException("Something wrong with loading, no domains loaded!");
//	        	loadException = e;
//	        	return false;
//	        }
	        loadApplicationsInfo(apiClient);
	        loadEventsInfo(apiClient);
	        loadSchemaInfo(apiClient);
	        loadEventApisInfo(apiClient);
	        loadConsumersInfo(apiClient);
	        loadOtherInfo(apiClient);
            System.out.println("EventPortalClient LOADED: " + (System.currentTimeMillis()-start) + "ms with PAGE_SIZE == " + PAGE_SIZE);
            loadStatus = LoadStatus.LOADED;
            lastRefresh = System.currentTimeMillis();
	        return true;
    	} catch (ApiException e) {
    		e.printStackTrace();
            loadStatus = LoadStatus.ERROR;
            loadException = e;
    		return false;
    	}
	}
	
	public void refresh() {
		
	}
	
    private void afterLoadingTests() {
        System.out.println("##################################");
        System.out.println("domains.size() = " + domains.size());
        System.out.println("appsById.size() = " + applicationsById.size());
        System.out.println("eventsById.size() = " + eventsById.size());
        System.out.println("eventVersionsById.size() = " + eventVersionsById.size());
        System.out.println("eventVersionsByEventId.size() = " + eventVersionsByEventId.size());
        
        System.out.println(EventPortalWrapper.INSTANCE.getDomain("7x3q0n9go0c"));
        System.out.println(applicationsByDomainId.get("7x3q0n9go0c"));
        System.out.println(eventsByDomainId.get("7x3q0n9go0c"));
        System.out.println(applicationVersionsByApplicatoinId.get("g67gm0qrxk7"));
        System.out.println(eventVersionsById.get("kb0183jdx7j"));
        System.out.println(eventApisByDomainId.get("x4oo4skfh5e"));  // Aaron test 1
        System.out.println(eventApiVersionsByEventApiId.get("ofn2yb68mf1"));  // Aaron test 1


    }
    
    
    
    private void loadDomainsInfo(ApiClient apiClient) throws ApiException {
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
    
    private void loadApplicationsInfo(ApiClient apiClient) throws ApiException {
    	long start = System.currentTimeMillis();
    	applicationsById = new LinkedHashMap<>();
    	applicationsByDomainId = new LinkedHashMap<>();
        ApplicationsApi apiApps = new ApplicationsApi(apiClient);
        int page = 1;
        ApplicationsResponse response2;
        do {
        	response2 = apiApps.getApplications(PAGE_SIZE, page++, null, null, null, null, null);
        	for (Application app : response2.getData()) {
        		applicationsById.put(app.getId(), app);
        		if (applicationsByDomainId.get(app.getApplicationDomainId()) == null) {
        			applicationsByDomainId.put(app.getApplicationDomainId(), new HashSet<>());
        		}
        		applicationsByDomainId.get(app.getApplicationDomainId()).add(app);
        	}
        } while (((Map<?, ?>)response2.getMeta().get("pagination")).get("nextPage") != null);
        
        // app versions
        applicationVersionsById = new LinkedHashMap<>();
        applicationVersionsByApplicatoinId = new LinkedHashMap<>();
        ApplicationVersionsResponse response3;
        page = 1;
        do {
        	response3 = apiApps.getApplicationVersions(PAGE_SIZE, page++, null);
            for (ApplicationVersion appVer : response3.getData()) {
            	applicationVersionsById.put(appVer.getId(), appVer);
            	if (applicationVersionsByApplicatoinId.get(appVer.getApplicationId()) == null) {
            		applicationVersionsByApplicatoinId.put(appVer.getApplicationId(), new HashSet<>());
            	}
            	applicationVersionsByApplicatoinId.get(appVer.getApplicationId()).add(appVer);
            }
        } while (((Map<?, ?>)response3.getMeta().get("pagination")).get("nextPage") != null);
        System.out.printf("getApplicationsInfo() took %dms.%n", System.currentTimeMillis() - start);
    }
    
    private void loadEventsInfo(ApiClient apiClient) throws ApiException {
    	eventsById = new LinkedHashMap<>();
    	eventsByDomainId = new LinkedHashMap<>();
    	long start = System.currentTimeMillis();
        EventsApi eventsApi = new EventsApi(apiClient);
        EventsResponse eventsReponse;
        int page = 1;
        do {
        	eventsReponse = eventsApi.getEvents(PAGE_SIZE, page++, null, null, null, null, null, null);
	        for (Event event : eventsReponse.getData()) {
	        	eventsById.put(event.getId(), event);
	        	if (eventsByDomainId.get(event.getApplicationDomainId()) == null) {
	        		eventsByDomainId.put(event.getApplicationDomainId(), new HashSet<>());
	        	}
	        	eventsByDomainId.get(event.getApplicationDomainId()).add(event);
	        }
        } while (((Map<?, ?>)eventsReponse.getMeta().get("pagination")).get("nextPage") != null);
    	
        // event versions
        eventVersionsById = new LinkedHashMap<>();
        eventVersionsByEventId = new LinkedHashMap<>();
        EventVersionsResponse eventVersionsResponse;
        page = 1;
        do {
        	eventVersionsResponse = eventsApi.getEventVersions(PAGE_SIZE, page++, null);
        	for (EventVersion eventVersion : eventVersionsResponse.getData()) {
        		eventVersionsById.put(eventVersion.getId(), eventVersion);
        		if (eventVersionsByEventId.get(eventVersion.getEventId()) == null) {
        			eventVersionsByEventId.put(eventVersion.getEventId(), new HashSet<>());
        		}
        		eventVersionsByEventId.get(eventVersion.getEventId()).add(eventVersion);
        	}
        } while (((Map<?, ?>)eventVersionsResponse.getMeta().get("pagination")).get("nextPage") != null);
        System.out.printf("getEventsInfo() took %dms.%n", System.currentTimeMillis() - start);
    }
    
    private void loadSchemaInfo(ApiClient apiClient) throws ApiException {
    	long start = System.currentTimeMillis();
    	schemasById = new LinkedHashMap<>();
    	schemasByDomainId = new LinkedHashMap<>();
        SchemasApi schemasApi = new SchemasApi(apiClient);
        SchemasResponse schemasReponse;
        int page = 1;
        do {
        	schemasReponse = schemasApi.getSchemas(PAGE_SIZE, page++, null, null, null, null, null, null);
	        for (SchemaObject schema : schemasReponse.getData()) {
	        	schemasById.put(schema.getId(), schema);
	        	if (schemasByDomainId.get(schema.getApplicationDomainId()) == null) {
	        		schemasByDomainId.put(schema.getApplicationDomainId(), new HashSet<>());
	        	}
	        	schemasByDomainId.get(schema.getApplicationDomainId()).add(schema);
	        }
        } while (((Map<?, ?>)schemasReponse.getMeta().get("pagination")).get("nextPage") != null);
        
        // schemas versions
        schemaVersionsById = new LinkedHashMap<>();
        schemaVersionsBySchemaId = new LinkedHashMap<>();

        SchemaVersionsResponse schemaVersionsResponse;
        page = 1;
        do {
        	schemaVersionsResponse = schemasApi.getSchemaVersions(PAGE_SIZE, page++, null);
        	for (SchemaVersion schemaVersion : schemaVersionsResponse.getData()) {
        		schemaVersionsById.put(schemaVersion.getId(), schemaVersion);
        		if (schemaVersionsBySchemaId.get(schemaVersion.getSchemaId()) == null) {
        			schemaVersionsBySchemaId.put(schemaVersion.getSchemaId(), new HashSet<>());
        		}
        		schemaVersionsBySchemaId.get(schemaVersion.getSchemaId()).add(schemaVersion);
        	}
        } while (((Map<?, ?>)schemaVersionsResponse.getMeta().get("pagination")).get("nextPage") != null);

        // hacky way of doing this, but not correct b/c this only finds schemas that are being used by events
//        for (String eventVersionId : eventVersionsById.keySet()) {
//        	if (eventVersionsById.get(eventVersionId).getSchemaVersionId() == null) continue;
//        	SchemaVersion schemaVersion = schemasApi.getSchemaVersion(eventVersionsById.get(eventVersionId).getSchemaVersionId()).getData();
//        	schemaVersionsById.put(schemaVersion.getId(), schemaVersion);
//    		if (schemaVersionsBySchemaId.get(schemaVersion.getSchemaId()) == null) {
//    			schemaVersionsBySchemaId.put(schemaVersion.getSchemaId(), new HashSet<>());
//    		}
//    		schemaVersionsBySchemaId.get(schemaVersion.getSchemaId()).add(schemaVersion);
//        }
        System.out.printf("getSchemaInfo() took %dms.%n", System.currentTimeMillis() - start);
    }
    
    private void loadEventApisInfo(ApiClient apiClient) throws ApiException {
    	long start = System.currentTimeMillis();
    	eventApisById = new LinkedHashMap<>();
    	eventApisByDomainId = new LinkedHashMap<>();
        EventApIsApi eventApisApi = new EventApIsApi(apiClient);
        EventApisResponse eventApisReponse;
        int page = 1;
        do {
        	eventApisReponse = eventApisApi.getEventApis(PAGE_SIZE, page++, null, null, null, null, null, null, null);
	        for (EventApi eventApi : eventApisReponse.getData()) {
	        	eventApisById.put(eventApi.getId(), eventApi);
	        	if (eventApisByDomainId.get(eventApi.getApplicationDomainId()) == null) {
	        		eventApisByDomainId.put(eventApi.getApplicationDomainId(), new HashSet<>());
	        	}
	        	eventApisByDomainId.get(eventApi.getApplicationDomainId()).add(eventApi);
	        }
        } while (eventApisReponse.getMeta().getPagination().getNextPage() != null);
        
        // event API versions
        eventApiVersionsById = new LinkedHashMap<>();
        eventApiVersionsByEventApiId = new LinkedHashMap<>();
        EventApiVersionsResponse eventApiVersionsResponse;
        page = 1;
        do {
        	eventApiVersionsResponse = eventApisApi.getEventApiVersions(PAGE_SIZE, page++, null, null, null);
        	for (EventApiVersion eventApiVersion : eventApiVersionsResponse.getData()) {
        		eventApiVersionsById.put(eventApiVersion.getId(), eventApiVersion);
        		if (eventApiVersionsByEventApiId.get(eventApiVersion.getEventApiId()) == null) {
        			eventApiVersionsByEventApiId.put(eventApiVersion.getEventApiId(), new HashSet<>());
        		}
        		eventApiVersionsByEventApiId.get(eventApiVersion.getEventApiId()).add(eventApiVersion);
        	}
        } while (eventApisReponse.getMeta().getPagination().getNextPage() != null);
        System.out.printf("loadEventApisInfo() took %dms.%n", System.currentTimeMillis() - start);
    }

    
    private void loadConsumersInfo(ApiClient apiClient) throws ApiException {
    	long start = System.currentTimeMillis();
    	consumersById = new LinkedHashMap<>();
    	consumersByApplicationVersionId = new LinkedHashMap<>();
    	ConsumersApi consumersApi = new ConsumersApi(apiClient);
        ConsumersResponse consumersReponse;
        int page = 1;
        do {
        	consumersReponse = consumersApi.getConsumers(PAGE_SIZE, page++, null, null);
	        for (Consumer consumer : consumersReponse.getData()) {
	        	consumersById.put(consumer.getId(), consumer);
	        	if (consumersByApplicationVersionId.get(consumer.getApplicationVersionId()) == null) {
	        		consumersByApplicationVersionId.put(consumer.getApplicationVersionId(), new HashSet<>());
	        	}
	        	consumersByApplicationVersionId.get(consumer.getApplicationVersionId()).add(consumer);
	        }
        } while (((Map<?, ?>)consumersReponse.getMeta().get("pagination")).get("nextPage") != null);
        System.out.printf("loadConsumersInfo() took %dms.%n", System.currentTimeMillis() - start);
    }

    
    private void loadOtherInfo(ApiClient apiClient) throws ApiException {
    	long start = System.currentTimeMillis();
    	// states
    	statesById = new LinkedHashMap<>();
        StatesApi statesApi = new StatesApi(apiClient);
        StatesResponse statesResponse = statesApi.getStates();
        for (StateDTO state : statesResponse.getData()) {
        	statesById.put(state.getId(), state);
        }
        
        // level separator
        
        System.out.printf("getOtherInfo() took %dms.%n", System.currentTimeMillis() - start);
    }
	
	
    
    
    
    
    
    
    
    
    
    // PUBLIC ACCESSOR METHOD /////////////////////////////////////////
    
    // Domains...
    public ApplicationDomain getDomain(String id) {
    	return domains.get(id);
    }
	
    public Collection<ApplicationDomain> getDomains() {
    	return Collections.unmodifiableCollection(domains.values());
    }
    
    
    // Applications...
    public Application getApplication(String id) {
    	return applicationsById.get(id);
    }

    public Set<Application> getApplicationsForDomainId(String domainId) {
		if (applicationsByDomainId.get(domainId) == null) return Collections.emptySet();
    	return Collections.unmodifiableSet(applicationsByDomainId.get(domainId));
    }
    
    public Collection<Application> getApplications() {
    	return Collections.unmodifiableCollection(applicationsById.values());
    }
    
    
    // Application Versions...
    public ApplicationVersion getApplicationVersion(String id) {
    	return applicationVersionsById.get(id);
    }

    /** Returns a copy */
    public Collection<ApplicationVersion> getApplicationVersions() {
    	return Collections.unmodifiableCollection(applicationVersionsById.values());
    }

    public Set<ApplicationVersion> getApplicationVersionsForApplicationId(String applicationId) {
		if (applicationVersionsByApplicatoinId.get(applicationId) == null) return Collections.emptySet();
    	return Collections.unmodifiableSet(applicationVersionsByApplicatoinId.get(applicationId));
    }
    
    
    // Events...
	public Event getEvent(String id) {
		return eventsById.get(id);
	}

	public Collection<Event> getEvents() {
		return Collections.unmodifiableCollection(eventsById.values());
	}
	
	public Set<Event> getEventsForDomainId(String domainId) {
		if (eventsByDomainId.get(domainId) == null) return Collections.emptySet();
		return Collections.unmodifiableSet(eventsByDomainId.get(domainId));
	}

	public boolean isExternalEvent(String eventVerId, String appVerId) {
		Event event = eventsById.get(eventVersionsById.get(eventVerId).getEventId());
		Application app = applicationsById.get(applicationVersionsById.get(appVerId).getApplicationId());
		return (event.getApplicationDomainId().equals(app.getApplicationDomainId()));
	}

	
	// Event Versions...
	public EventVersion getEventVersion(String id) {
		return eventVersionsById.get(id);
	}
	
	public Collection<EventVersion> getEventVersions() {
		return Collections.unmodifiableCollection(eventVersionsById.values());
	}

	public Set<EventVersion> getEventVersionsForEventId(String eventId) {
		if (eventVersionsByEventId.get(eventId) == null) return Collections.emptySet();
		return Collections.unmodifiableSet(eventVersionsByEventId.get(eventId));
	}
	
	
	// Schemas...
	public SchemaObject getSchema(String id) {
		return schemasById.get(id);
	}

	public Collection<SchemaObject> getSchemas() {
		return Collections.unmodifiableCollection(schemasById.values());
	}

	public Set<SchemaObject> getSchemasForDomainId(String domainId) {
		if (schemasByDomainId.get(domainId) == null) return Collections.emptySet();
		return Collections.unmodifiableSet(schemasByDomainId.get(domainId));
	}
	
	
	// Schema Versions...
	/**
	 * @param id
	 * @return null if not found, otherwise object
	 */
	public SchemaVersion getSchemaVersion(String id) {
		return schemaVersionsById.get(id);
	}
	
	public Collection<SchemaVersion> getSchemaVersions() {
		return Collections.unmodifiableCollection(schemaVersionsById.values());
	}
	
	/**
	 * 
	 * @param schemaId
	 * @return empty list if not found, otherwise list
	 */
	public Set<SchemaVersion> getSchemaVersionsForSchemaId(String schemaId) {
		if (schemaVersionsBySchemaId.get(schemaId) == null) return Collections.emptySet();
		return Collections.unmodifiableSet(schemaVersionsBySchemaId.get(schemaId));
	}
	
	
	
	public EventApi getEventApi(String id) {
		return eventApisById.get(id);
	}

	public Set<EventApi> getEventApisForDomainId(String domainId) {
		if (eventApisByDomainId.get(domainId) == null) return Collections.emptySet();
		return Collections.unmodifiableSet(eventApisByDomainId.get(domainId));
	}
	
	
	

	public EventApiVersion getEventApiVersion(String id) {
		return eventApiVersionsById.get(id);
	}
	
	public Set<EventApiVersion> getEventApiVersionsForEventApiId(String eventApiId) {
		if (eventApiVersionsByEventApiId.get(eventApiId) == null) return Collections.emptySet();
		return Collections.unmodifiableSet(eventApiVersionsByEventApiId.get(eventApiId));
	}

	
	
	
	public Consumer getConsumer(String id) {
		return consumersById.get(id);
	}
	
	public Collection<Consumer> getConsumers() {
		return Collections.unmodifiableCollection(consumersById.values());
	}
	
	public Set<Consumer> getConsumersForApplicationVersionId(String applicationVersionId) {
		if (!consumersByApplicationVersionId.containsKey(applicationVersionId)) return Collections.emptySet();
		return Collections.unmodifiableSet(consumersByApplicationVersionId.get(applicationVersionId));
	}

	
	
	public StateDTO getState(String id) {
		return statesById.get(id);
	}
	
	public Collection<StateDTO> getStates() {
		return Collections.unmodifiableCollection(statesById.values());
	}
	
	
	public EventPortalObjectType getEventPortalObjectType(String id) {
		if (loadStatus != LoadStatus.LOADED) return null;
		if (domains.containsKey(id)) return EventPortalObjectType.DOMAIN;
		if (applicationsById.containsKey(id)) return EventPortalObjectType.APPLICATION;
		if (applicationVersionsById.containsKey(id)) return EventPortalObjectType.APPLICATION_VERSION;
		if (eventsById.containsKey(id)) return EventPortalObjectType.EVENT;
		if (eventApiVersionsById.containsKey(id)) return EventPortalObjectType.EVENT_VERSION;
		if (schemasById.containsKey(id)) return EventPortalObjectType.SCHEMA;
		if (schemaVersionsById.containsKey(id)) return EventPortalObjectType.SCHEMA_VERSION;
		if (eventApisById.containsKey(id)) return EventPortalObjectType.EVENT_API;
		if (eventVersionsById.containsKey(id)) return EventPortalObjectType.EVENT_API_VERSION;
		return EventPortalObjectType.N_A;
	}

	
	public Application getRandomApplication() {
		int i = (int)(Math.random() * applicationsById.size());
		return applicationsById.values().stream().skip(i).findAny().orElse(null);
	}

	public ApplicationVersion getRandomApplicationVersion() {
		int i = (int)(Math.random() * applicationVersionsById.size());
		return applicationVersionsById.values().stream().skip(i).findAny().orElse(null);
	}

	public Event getRandomEvent() {
		int i = (int)(Math.random() * eventsById.size());
		return eventsById.values().stream().skip(i).findAny().orElse(null);
	}

	public EventVersion getRandomEventVersion() {
		int i = (int)(Math.random() * eventVersionsById.size());
		return eventVersionsById.values().stream().skip(i).findAny().orElse(null);
	}

	public Set<String> getAppsUsingEvent(String eventId) {
		
		Set<EventVersion> eventVersions = getEventVersionsForEventId(eventId);
		Set<Application> apps = new HashSet<>();
		Set<String> appDescs = new HashSet<>();
		if (eventVersions == null) return Collections.emptySet();
		
		for (EventVersion eventVersion : eventVersions) {
			for (String appVerId : eventVersion.getDeclaredConsumingApplicationVersionIds()) {
				Application app = getApplication(getApplicationVersion(appVerId).getApplicationId());
				apps.add(app);
				ApplicationDomain domain = getDomain(app.getApplicationDomainId());
				appDescs.add(String.format("'%s' in '%s'", app.getName(), domain.getName()));
			}
		}
		return appDescs;
	}
	
}
