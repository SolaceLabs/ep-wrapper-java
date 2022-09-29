package community.solace.ep.wrapper;

import java.io.BufferedReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import community.solace.ep.client.ApiClient;
import community.solace.ep.client.ApiException;
import community.solace.ep.client.Configuration;
import community.solace.ep.client.api.ApplicationDomainsApi;
import community.solace.ep.client.api.ApplicationsApi;
import community.solace.ep.client.api.ConsumersApi;
import community.solace.ep.client.api.EnumsApi;
import community.solace.ep.client.api.EnvironmentsApi;
import community.solace.ep.client.api.EventApIsApi;
import community.solace.ep.client.api.EventApiProductsApi;
import community.solace.ep.client.api.EventMeshesApi;
import community.solace.ep.client.api.EventsApi;
import community.solace.ep.client.api.SchemasApi;
import community.solace.ep.client.api.StatesApi;
import community.solace.ep.client.api.TopicDomainsApi;
import community.solace.ep.client.model.Application;
import community.solace.ep.client.model.ApplicationDomain;
import community.solace.ep.client.model.ApplicationDomainsResponse;
import community.solace.ep.client.model.ApplicationVersion;
import community.solace.ep.client.model.ApplicationVersionsResponse;
import community.solace.ep.client.model.ApplicationsResponse;
import community.solace.ep.client.model.Consumer;
import community.solace.ep.client.model.ConsumersResponse;
import community.solace.ep.client.model.Environment;
import community.solace.ep.client.model.EnvironmentsResponse;
import community.solace.ep.client.model.Event;
import community.solace.ep.client.model.EventApi;
import community.solace.ep.client.model.EventApiProduct;
import community.solace.ep.client.model.EventApiProductVersion;
import community.solace.ep.client.model.EventApiProductVersionsResponse;
import community.solace.ep.client.model.EventApiProductsResponse;
import community.solace.ep.client.model.EventApiVersion;
import community.solace.ep.client.model.EventApiVersionsResponse;
import community.solace.ep.client.model.EventApisResponse;
import community.solace.ep.client.model.EventMesh;
import community.solace.ep.client.model.EventMeshesResponse;
import community.solace.ep.client.model.EventVersion;
import community.solace.ep.client.model.EventVersionsResponse;
import community.solace.ep.client.model.EventsResponse;
import community.solace.ep.client.model.SchemaObject;
import community.solace.ep.client.model.SchemaVersion;
import community.solace.ep.client.model.SchemaVersionsResponse;
import community.solace.ep.client.model.SchemasResponse;
import community.solace.ep.client.model.StateDTO;
import community.solace.ep.client.model.StatesResponse;
import community.solace.ep.client.model.TopicAddressEnum;
import community.solace.ep.client.model.TopicAddressEnumVersion;
import community.solace.ep.client.model.TopicAddressEnumVersionsResponse;
import community.solace.ep.client.model.TopicAddressEnumsResponse;
import community.solace.ep.client.model.TopicDomain;
import community.solace.ep.client.model.TopicDomainsResponse;

public enum EventPortalWrapper {
	
	INSTANCE;  // singleton pattern
	private static volatile boolean lazyLoadSchemaVers = false;  // not implemented yet!

    private static final int PAGE_SIZE = 50;
    
    private Map<String, ApplicationDomain> domains = new LinkedHashMap<>();
    
    private Map<String, Application> applicationsById = new LinkedHashMap<>();
    private Map<String, Set<Application>> applicationsByDomainId = new LinkedHashMap<>();
    private Map<String, ApplicationVersion> applicationVersionsById = new LinkedHashMap<>();
    private Map<String, Set<ApplicationVersion>> applicationVersionsByApplicationId = new LinkedHashMap<>();

    private Map<String, Event> eventsById = new LinkedHashMap<>();
    private Map<String, Set<Event>> eventsByDomainId = new LinkedHashMap<>();
    private Map<String, EventVersion> eventVersionsById = new LinkedHashMap<>();
    private Map<String, Set<EventVersion>> eventVersionsByEventId = new LinkedHashMap<>();

    private Map<String, SchemaObject> schemasById = new LinkedHashMap<>();
    private Map<String, Set<SchemaObject>> schemasByDomainId = new LinkedHashMap<>();
    private Map<String, SchemaVersion> schemaVersionsById = new LinkedHashMap<>();
    private Map<String, Set<SchemaVersion>> schemaVersionsBySchemaId = new LinkedHashMap<>();

    private Map<String, TopicAddressEnum> enumsById = new LinkedHashMap<>();
    private Map<String, Set<TopicAddressEnum>> enumsByDomainId = new LinkedHashMap<>();
    private Map<String, TopicAddressEnumVersion> enumVersionsById = new LinkedHashMap<>();
    private Map<String, Set<TopicAddressEnumVersion>> enumVersionsByEnumId = new LinkedHashMap<>();

    private Map<String, EventApi> eventApisById = new LinkedHashMap<>();
    private Map<String, Set<EventApi>> eventApisByDomainId = new LinkedHashMap<>();
    private Map<String, EventApiVersion> eventApiVersionsById = new LinkedHashMap<>();
    private Map<String, Set<EventApiVersion>> eventApiVersionsByEventApiId = new LinkedHashMap<>();

    private Map<String, EventApiProduct> eventApiProductsById = new LinkedHashMap<>();
    private Map<String, Set<EventApiProduct>> eventApiProductsByDomainId = new LinkedHashMap<>();
    private Map<String, EventApiProductVersion> eventApiProductVersionsById = new LinkedHashMap<>();
    private Map<String, Set<EventApiProductVersion>> eventApiProductVersionsByEventApiProductId = new LinkedHashMap<>();

    private Map<String, Consumer> consumersById = new LinkedHashMap<>();
    private Map<String, Set<Consumer>> consumersByApplicationVersionId = new LinkedHashMap<>();

    private Map<String, EventMesh> eventMeshesById = new LinkedHashMap<>();
    private Map<String, Set<EventMesh>> eventMeshesByEnvironmentId = new LinkedHashMap<>();
//    private Map<String, Set<EventMesh>> eventMeshesByBrokerType = new LinkedHashMap<>();
//    private Map<String, EventMeshVersion> eventMeshVersionsById = new LinkedHashMap<>();
//    private Map<String, Set<EventMeshVersion>> eventMeshVersionsByEventMeshId = new LinkedHashMap<>();

    
    private Map<String, Set<TopicDomain>> topicDomainsByDomainId = new LinkedHashMap<>();
//    private Map<String, Set<TopicDomain>> topicDomainsByBrokerType = new LinkedHashMap<>();

    
    private Map<String, Environment> environmentsById = new LinkedHashMap<>();

    private Map<String, StateDTO> statesById = new LinkedHashMap<>();
    
    private Map<String, String> userNamesById = new LinkedHashMap<>();
    
    private String token = null;

    
    public enum LoadStatus {
    	UNINITIALIZED,
    	LOADING,
    	LOADED,
    	ERROR,
    	;
    	
    	
    }
    
    private LoadStatus loadStatus = LoadStatus.UNINITIALIZED;
    private Set<String> loadErrorStrings = null;
//    private Throwable loadException = null;
	private long lastRefresh = 0L;
		
	public long getLastRefresh() {
		return lastRefresh;
	}
    
    public LoadStatus getLoadStatus() {
    	return loadStatus;
    }
    
    public Set<String> getLoadErrorString() {
    	return loadErrorStrings;
    }
    
//    public Throwable getLoadException() {
//    	return loadException;
//    }
    
    public boolean isLoaded() {
    	return loadStatus == LoadStatus.LOADED;
    }
    
    public boolean isLoading() {
    	return loadStatus == LoadStatus.LOADING;
    }
    
    /** This sets the Event Portal token used for pulling data via REST API.
     * This token is not stored on disk or anything.
     * @param token a long String of characters generated from the Solace Event Portal
     */
    public void setToken(String token) {
    	this.token = token;
    }

    /**
	 * Loads all objects in parallel using ExecutorService.  If loading is unsuccessful, query `getLoadException()`
	 * to find out issue.
     * @param pool an ExecutorService to use for loading objects
	 * @return true if loading successful; false if any issue was encountered (although perhaps partial success)
     */
	public boolean loadAll(ExecutorService pool) {
		if (loadStatus == LoadStatus.LOADING) {
			return false;
		}
		loadStatus = LoadStatus.LOADING;
	    long start = System.currentTimeMillis();
//        loadException = null;  // blank it, used at end to make sure no loading errors
        loadErrorStrings = Collections.synchronizedSet(new HashSet<>());
		AtomicInteger num = new AtomicInteger(1);  // one job, domains, for sure
		pool.submit(() -> { loadDomains();		num.decrementAndGet(); });  // don't count first one
		pool.submit(() -> { num.incrementAndGet();	loadApplications();	num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadEvents();		num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadSchemas();		num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadEnums();		num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadEventApis();	num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadEventApiProducts();	num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadStates();		num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadConsumers();	num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadEventMeshes();	num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadTopicDomains();	num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadEnvironments();	num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadUsersCustom();	num.decrementAndGet(); });
		while (num.get() > 0) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) { }
		}
		if (!loadErrorStrings.isEmpty()) {
	        System.err.println("EventPortalWrapper had issues loading: " + loadErrorStrings.toString());
			loadStatus = LoadStatus.ERROR;
			return false;
		} else {
	        System.out.println("EventPortalWrapper LOADED: " + (System.currentTimeMillis()-start) + "ms with PAGE_SIZE == " + PAGE_SIZE);
	        loadStatus = LoadStatus.LOADED;
	        lastRefresh = System.currentTimeMillis();
	        return true;
		}
	}

	/**
	 * Loads all objects serially in this thread.  If loading is unsuccessful, query `getLoadException()`
	 * to find out issue.
	 * @return true if loading successful; false if any issue was encountered (although perhaps partial success)
	 */
	public boolean loadAll() {
		if (loadStatus == LoadStatus.LOADING) {
			return false;
		}
		loadStatus = LoadStatus.LOADING;
	    long start = System.currentTimeMillis();
        loadErrorStrings = Collections.synchronizedSet(new HashSet<>());
        if (!loadDomains()) return false;
//	        if (domains.size() == 0) {
//	        	loadStatus = LoadStatus.ERROR;
//	        	IllegalStateException e = new IllegalStateException("Something wrong with loading, no domains loaded!");
//	        	loadException = e;
//	        	return false;
//	        }
        // don't fail on loading each one
        loadApplications();
        loadEvents();
        loadSchemas();
        loadEnums();
        loadEventApis();
        loadEventApiProducts();
        loadStates();
        loadConsumers();
        loadEventMeshes();
        loadTopicDomains();
        loadEnvironments();
//        if (!loadApplications()) return false;
//        if (!loadEvents()) return false;
//        if (!loadSchemas()) return false;
//        if (!loadEnums()) return false;
//        if (!loadEventApis()) return false;
//        if (!loadEventApiProducts()) return false;
//        if (!loadStates()) return false;
//        if (!loadConsumers()) return false;
//        if (!loadEventMeshes()) return false;
//        if (!loadTopicDomains()) return false;
//        if (!loadEnvironments()) return false;
        loadUsersCustom();  // either way, keep going even if this breaks
		if (!loadErrorStrings.isEmpty()) {
	        System.err.println("EventPortalWrapper had issues loading: " + loadErrorStrings.toString());
			loadStatus = LoadStatus.ERROR;
			return false;
		} else {
	        System.out.println("EventPortalWrapper LOADED: " + (System.currentTimeMillis()-start) + "ms with PAGE_SIZE == " + PAGE_SIZE);
	        loadStatus = LoadStatus.LOADED;
	        lastRefresh = System.currentTimeMillis();
	        return true;
		}
	}
	
    void afterLoadingTests() {
        System.out.println("##################################");
        System.out.println("domains.size() = " + domains.size());
        System.out.println("appsById.size() = " + applicationsById.size());
        System.out.println("eventsById.size() = " + eventsById.size());
        System.out.println("eventVersionsById.size() = " + eventVersionsById.size());
        System.out.println("eventVersionsByEventId.size() = " + eventVersionsByEventId.size());
        
        System.out.println(EventPortalWrapper.INSTANCE.getDomain("7x3q0n9go0c"));
        System.out.println(applicationsByDomainId.get("7x3q0n9go0c"));
        System.out.println(eventsByDomainId.get("7x3q0n9go0c"));
        System.out.println(applicationVersionsByApplicationId.get("g67gm0qrxk7"));
        System.out.println(eventVersionsById.get("kb0183jdx7j"));
        System.out.println(eventApisByDomainId.get("x4oo4skfh5e"));  // Aaron test 1
        System.out.println(eventApiVersionsByEventApiId.get("ofn2yb68mf1"));  // Aaron test 1
    }
    
    public ApiClient getApiClient() {
	  	ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setBasePath("http://api.solace.cloud");
        apiClient.setAccessToken(token);
//        apiClient.setBearerToken(token);
        return apiClient;
    }
    
    /**
     * Load just the ApplicationDomain info from Event Portal.
	 * @return true if loading successful; false otherwise
     */
    public boolean loadDomains() {
        ApiClient apiClient = getApiClient();
    	try {
    		long start = System.currentTimeMillis();
	        ApplicationDomainsApi apiDomains = new ApplicationDomainsApi(apiClient);
	        ApplicationDomainsResponse response;
	        int page = 1;
	        do {
	        	response = apiDomains.getApplicationDomains(PAGE_SIZE, page++, null, null, Collections.singletonList("stats"));
//	        	response = apiDomains.getApplicationDomains(PAGE_SIZE, page++, null, null, Collections.singleton("stats"));
	        	for (ApplicationDomain domain : response.getData()) {
	        		domains.put(domain.getId(), domain);
	        	}
	        } while (((Map<?, ?>)response.getMeta().get("pagination")).get("nextPage") != null);
	        System.out.printf("loadDomains() loaded %d domains, and took %dms.%n", domains.size(), System.currentTimeMillis() - start);
	        return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.toString());
    		return false;
    	}
    }
    
    public boolean loadApplications() {
        ApiClient apiClient = getApiClient();
    	try {
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
	        			applicationsByDomainId.put(app.getApplicationDomainId(), new LinkedHashSet<>());
	        		}
	        		applicationsByDomainId.get(app.getApplicationDomainId()).add(app);
	        	}
	        } while (((Map<?, ?>)response2.getMeta().get("pagination")).get("nextPage") != null);
	        
	        // app versions
	        applicationVersionsById = new LinkedHashMap<>();
	        applicationVersionsByApplicationId = new LinkedHashMap<>();
	        ApplicationVersionsResponse response3;
	        page = 1;
	        do {
	        	response3 = apiApps.getApplicationVersions(PAGE_SIZE, page++, null);
	            for (ApplicationVersion appVer : response3.getData()) {
	            	applicationVersionsById.put(appVer.getId(), appVer);
	            	if (applicationVersionsByApplicationId.get(appVer.getApplicationId()) == null) {
	            		applicationVersionsByApplicationId.put(appVer.getApplicationId(), new LinkedHashSet<>());
	            	}
	            	applicationVersionsByApplicationId.get(appVer.getApplicationId()).add(appVer);
	            }
	        } while (((Map<?, ?>)response3.getMeta().get("pagination")).get("nextPage") != null);
	        System.out.printf("loadApplications() loaded %d apps and %d versions, and took %dms.%n",
	        		applicationsById.size(), applicationVersionsById.size(), System.currentTimeMillis() - start);
    		return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.toString());
    		return false;
    	}
    }
    
    public boolean loadEvents() {
        ApiClient apiClient = getApiClient();
    	try {
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
		        		eventsByDomainId.put(event.getApplicationDomainId(), new LinkedHashSet<>());
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
	        			eventVersionsByEventId.put(eventVersion.getEventId(), new LinkedHashSet<>());
	        		}
	        		eventVersionsByEventId.get(eventVersion.getEventId()).add(eventVersion);
	        	}
	        } while (((Map<?, ?>)eventVersionsResponse.getMeta().get("pagination")).get("nextPage") != null);
	        System.out.printf("loadEvents() loaded %d events and %d versions, and took %dms.%n",
	        		eventsById.size(), eventVersionsById.size(), System.currentTimeMillis() - start);
    		return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.toString());
    		return false;
    	}
    }
    
    public boolean loadSchemas() {
        ApiClient apiClient = getApiClient();
    	try {
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
		        		schemasByDomainId.put(schema.getApplicationDomainId(), new LinkedHashSet<>());
		        	}
		        	schemasByDomainId.get(schema.getApplicationDomainId()).add(schema);
		        }
	        } while (((Map<?, ?>)schemasReponse.getMeta().get("pagination")).get("nextPage") != null);
	        
	        // schemas versions
	        schemaVersionsById = new LinkedHashMap<>();
	        schemaVersionsBySchemaId = new LinkedHashMap<>();
	        if (!lazyLoadSchemaVers) {  // else we'll try and get them on-demand
		        SchemaVersionsResponse schemaVersionsResponse;
		        page = 1;
		        do {
		        	schemaVersionsResponse = schemasApi.getSchemaVersions(PAGE_SIZE, page++, null);
		        	for (SchemaVersion schemaVersion : schemaVersionsResponse.getData()) {
		        		schemaVersionsById.put(schemaVersion.getId(), schemaVersion);
		        		if (schemaVersionsBySchemaId.get(schemaVersion.getSchemaId()) == null) {
		        			schemaVersionsBySchemaId.put(schemaVersion.getSchemaId(), new LinkedHashSet<>());
		        		}
		        		schemaVersionsBySchemaId.get(schemaVersion.getSchemaId()).add(schemaVersion);
		        	}
		        } while (((Map<?, ?>)schemaVersionsResponse.getMeta().get("pagination")).get("nextPage") != null);
	        }
	        System.out.printf("loadSchema() loaded %d schemas and %d versions, and took %dms.%n",
	        		schemasById.size(), schemaVersionsById.size(), System.currentTimeMillis() - start);
    		return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.toString());
    		return false;
    	}
    }
    
    public boolean loadEnums() {
        ApiClient apiClient = getApiClient();
    	try {
	    	enumsById = new LinkedHashMap<>();
	    	enumsByDomainId = new LinkedHashMap<>();
	    	long start = System.currentTimeMillis();
	        EnumsApi enumsApi = new EnumsApi(apiClient);
	        TopicAddressEnumsResponse enumsReponse;
	        int page = 1;
	        do {
	        	enumsReponse = enumsApi.getEnums(PAGE_SIZE, page++, null, null, null, null, null, null);
		        for (TopicAddressEnum topicAddressEnum : enumsReponse.getData()) {
		        	enumsById.put(topicAddressEnum.getId(), topicAddressEnum);
		        	if (enumsByDomainId.get(topicAddressEnum.getApplicationDomainId()) == null) {
		        		enumsByDomainId.put(topicAddressEnum.getApplicationDomainId(), new LinkedHashSet<>());
		        	}
		        	enumsByDomainId.get(topicAddressEnum.getApplicationDomainId()).add(topicAddressEnum);
		        }
	        } while (((Map<?, ?>)enumsReponse.getMeta().get("pagination")).get("nextPage") != null);
	    	
	        // enum versions
	        enumVersionsById = new LinkedHashMap<>();
	        enumVersionsByEnumId = new LinkedHashMap<>();
	        TopicAddressEnumVersionsResponse enumVersionsResponse;
	        page = 1;
	        do {
	        	enumVersionsResponse = enumsApi.getEnumVersions(PAGE_SIZE, page++, null);
	        	for (TopicAddressEnumVersion enumVersion : enumVersionsResponse.getData()) {
	        		enumVersionsById.put(enumVersion.getId(), enumVersion);
	        		if (enumVersionsByEnumId.get(enumVersion.getEnumId()) == null) {
	        			enumVersionsByEnumId.put(enumVersion.getEnumId(), new LinkedHashSet<>());
	        		}
	        		enumVersionsByEnumId.get(enumVersion.getEnumId()).add(enumVersion);
	        	}
	        } while (((Map<?, ?>)enumVersionsResponse.getMeta().get("pagination")).get("nextPage") != null);
	        System.out.printf("loadEnums() loaded %d enums and %d versions, and took %dms.%n",
	        		enumsById.size(), enumVersionsById.size(), System.currentTimeMillis() - start);
    		return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.toString());
    		return false;
    	}
    }
    
    public boolean loadEventApis() {
        ApiClient apiClient = getApiClient();
    	try {
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
		        		eventApisByDomainId.put(eventApi.getApplicationDomainId(), new LinkedHashSet<>());
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
	        			eventApiVersionsByEventApiId.put(eventApiVersion.getEventApiId(), new LinkedHashSet<>());
	        		}
	        		eventApiVersionsByEventApiId.get(eventApiVersion.getEventApiId()).add(eventApiVersion);
	        	}
	        } while (eventApisReponse.getMeta().getPagination().getNextPage() != null);
	        System.out.printf("loadEventApis() loaded %d Event APIs and %d versions, and took %dms.%n",
	        		eventApisById.size(), eventApiVersionsById.size(), System.currentTimeMillis() - start);
    		return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.toString());
    		return false;
    	}
    }

    public boolean loadEventApiProducts() {
        ApiClient apiClient = getApiClient();
    	try {
	    	long start = System.currentTimeMillis();
	    	eventApiProductsById = new LinkedHashMap<>();
	    	eventApiProductsByDomainId = new LinkedHashMap<>();
	    	EventApiProductsApi eventApiProductsApi = new EventApiProductsApi(apiClient);
	        EventApiProductsResponse eventApiProductsReponse;
	        int page = 1;
	        do {
	        	eventApiProductsReponse = eventApiProductsApi.getEventApiProducts(PAGE_SIZE, page++, null, null, null, null, null, null, null);
		        for (EventApiProduct eventApiProduct : eventApiProductsReponse.getData()) {
//		        	System.out.println(eventApiProduct);
		        	eventApiProductsById.put(eventApiProduct.getId(), eventApiProduct);
		        	if (eventApiProductsByDomainId.get(eventApiProduct.getApplicationDomainId()) == null) {
		        		eventApiProductsByDomainId.put(eventApiProduct.getApplicationDomainId(), new LinkedHashSet<>());
		        	}
		        	eventApiProductsByDomainId.get(eventApiProduct.getApplicationDomainId()).add(eventApiProduct);
		        }
	        } while (eventApiProductsReponse.getMeta().getPagination().getNextPage() != null);
	        
	        // event API versions
	        eventApiProductVersionsById = new LinkedHashMap<>();
	        eventApiProductVersionsByEventApiProductId = new LinkedHashMap<>();
	        EventApiProductVersionsResponse eventApiProductVersionsResponse;
	        page = 1;
	        do {
	        	eventApiProductVersionsResponse = eventApiProductsApi.getEventApiProductVersions(PAGE_SIZE, page++, null, null, null, null);
	        	for (EventApiProductVersion eventApiProductVersion : eventApiProductVersionsResponse.getData()) {
//	        		System.out.println(eventApiProductVersion);
	        		eventApiProductVersionsById.put(eventApiProductVersion.getId(), eventApiProductVersion);
	        		if (eventApiProductVersionsByEventApiProductId.get(eventApiProductVersion.getEventApiProductId()) == null) {
	        			eventApiProductVersionsByEventApiProductId.put(eventApiProductVersion.getEventApiProductId(), new LinkedHashSet<>());
	        		}
	        		eventApiProductVersionsByEventApiProductId.get(eventApiProductVersion.getEventApiProductId()).add(eventApiProductVersion);
	        	}
	        } while (eventApiProductsReponse.getMeta().getPagination().getNextPage() != null);
	        System.out.printf("loadEventApiProducts() loaded %d Event API Products and %d versions, and took %dms.%n",
	        		eventApiProductsById.size(), eventApiProductVersionsById.size(), System.currentTimeMillis() - start);
    		return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.toString());
    		return false;
    	}
    }
    
    public boolean loadStates() {
        ApiClient apiClient = getApiClient();
    	try {
	    	long start = System.currentTimeMillis();
	    	// states
	    	statesById = new LinkedHashMap<>();
	        StatesApi statesApi = new StatesApi(apiClient);
	        StatesResponse statesResponse = statesApi.getStates();
	        for (StateDTO state : statesResponse.getData()) {
	        	statesById.put(state.getId(), state);
	        }
	        System.out.printf("loadStates() loaded %d states, and took %dms.%n",
	        		statesById.size(), System.currentTimeMillis() - start);
    		return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.toString());
    		return false;
    	}
    }

    public boolean loadConsumers() {
        ApiClient apiClient = getApiClient();
    	try {
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
		        		consumersByApplicationVersionId.put(consumer.getApplicationVersionId(), new LinkedHashSet<>());
		        	}
		        	consumersByApplicationVersionId.get(consumer.getApplicationVersionId()).add(consumer);
		        }
	        } while (((Map<?, ?>)consumersReponse.getMeta().get("pagination")).get("nextPage") != null);
	        System.out.printf("loadConsumers() loaded %d Consumers, and took %dms.%n",
	        		consumersById.size(), System.currentTimeMillis() - start);
    		return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.toString());
    		return false;
    	}
    }
    
    public boolean loadEventMeshes() {
        ApiClient apiClient = getApiClient();
    	try {
	    	eventMeshesById = new LinkedHashMap<>();
	    	eventMeshesByEnvironmentId = new LinkedHashMap<>();
	    	long start = System.currentTimeMillis();
	        EventMeshesApi eventMeshesApi = new EventMeshesApi(apiClient);
	        EventMeshesResponse eventMeshesReponse;
	        int page = 1;
	        do {
	        	eventMeshesReponse = eventMeshesApi.getEventMeshes(PAGE_SIZE, page++, null, null);
		        for (EventMesh eventMesh : eventMeshesReponse.getData()) {
		        	eventMeshesById.put(eventMesh.getId(), eventMesh);
		        	if (eventMeshesByEnvironmentId.get(eventMesh.getEnvironmentId()) == null) {
		        		eventMeshesByEnvironmentId.put(eventMesh.getEnvironmentId(), new LinkedHashSet<>());
		        	}
		        	eventMeshesByEnvironmentId.get(eventMesh.getEnvironmentId()).add(eventMesh);
//		        	if (eventMeshesByBrokerType.get(eventMesh.get) == null) {
//		        		eventMeshesByBrokerType.put(eventMesh.getBrokerType(), new LinkedHashSet<>());
//		        	}
//		        	eventMeshesByBrokerType.get(eventMesh.getBrokerType()).add(eventMesh);
		        }
	        } while (((Map<?, ?>)eventMeshesReponse.getMeta().get("pagination")).get("nextPage") != null);
	        System.out.printf("loadEventMeshes() loaded %d Event Meshes, and took %dms.%n",
	        		eventMeshesById.size(), System.currentTimeMillis() - start);
    		return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.toString());
    		return false;
    	}
    }

    public boolean loadTopicDomains() {
        ApiClient apiClient = getApiClient();
    	try {
	    	long start = System.currentTimeMillis();
//	    	topicDomainsById = new LinkedHashMap<>();
	    	TopicDomainsApi topicDomainsApi = new TopicDomainsApi(apiClient);
	        TopicDomainsResponse topicDomainsReponse;
	        int page = 1;
	        do {
	        	topicDomainsReponse = topicDomainsApi.getTopicDomains(PAGE_SIZE, page++, null, null, null, null);
		        for (TopicDomain topicDomain : topicDomainsReponse.getData()) {
//		        	topicDomainsById.put(topicDomain.getgetId(), topicDomain);
		           	if (topicDomainsByDomainId.get(topicDomain.getApplicationDomainId()) == null) {
		           		topicDomainsByDomainId.put(topicDomain.getApplicationDomainId(), new LinkedHashSet<>());
		        	}
		           	topicDomainsByDomainId.get(topicDomain.getApplicationDomainId()).add(topicDomain);
//		           	if (topicDomainsByBrokerType.get(topicDomain.getBrokerType()) == null) {
//		           		topicDomainsByBrokerType.put(topicDomain.getBrokerType(), new LinkedHashSet<>());
//		        	}
//		           	topicDomainsByBrokerType.get(topicDomain.getBrokerType()).add(topicDomain);
		        }
	        } while (((Map<?, ?>)topicDomainsReponse.getMeta().get("pagination")).get("nextPage") != null);
	        System.out.printf("loadTopicDomains() loaded %d Topic Domains, and took %dms.%n", topicDomainsByDomainId.size(), System.currentTimeMillis() - start);
    		return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.toString());
    		return false;
    	}
    }
    	
    public boolean loadEnvironments() {
        ApiClient apiClient = getApiClient();
    	try {
	    	long start = System.currentTimeMillis();
	    	environmentsById = new LinkedHashMap<>();
	    	EnvironmentsApi environmentsApi = new EnvironmentsApi(apiClient);
	        EnvironmentsResponse environmentsReponse;
	        int page = 1;
	        do {
	        	environmentsReponse = environmentsApi.getEnvironments(PAGE_SIZE, page++, null, null);
		        for (Environment environment : environmentsReponse.getData()) {
		        	environmentsById.put(environment.getId(), environment);
		        }
	        } while (((Map<?, ?>)environmentsReponse.getMeta().get("pagination")).get("nextPage") != null);
	        System.out.printf("loadEnvironments() loaded %d Environments, and took %dms.%n", environmentsById.size(), System.currentTimeMillis() - start);
    		return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.toString());
    		return false;
    	}
    }

    
    
    private int fetchUsers(int page) {
		Request request = new Request.Builder()
				.url("https://api.solace.cloud/api/v0/users?page-size=" + PAGE_SIZE + "&page-number=" + page)
//				.url("https://api.solace.cloud/api/v2/architecture/eventApiProductVersions")
//				.header("pageSize", Integer.toString(PAGE_SIZE))
		        .header("Authorization", token)
				.get()
				.build();
		OkHttpClient client = new OkHttpClient();
		
		long start = System.currentTimeMillis();
		Call call = client.newCall(request);
		try {
			Response response = call.execute();
            System.out.printf("fetchUsers() took %dms for HTTP call.%n", System.currentTimeMillis() - start);
            start = System.currentTimeMillis();
			if (response.code() == 200) {
	            char[] buffer = new char[8 * 1024];
	            StringBuilder sb = new StringBuilder();
                int charsRead;
                while ((charsRead = response.body().charStream().read(buffer)) != -1) {
                	sb.append(buffer, 0, charsRead);
                }
                JsonObject jo = new Gson().fromJson(sb.toString(), JsonObject.class);
                System.out.printf("fetchUsers() took %dms to deserialize into JSON object.%n", System.currentTimeMillis() - start);
                if (jo.has("data")) {
                	JsonArray ar = jo.get("data").getAsJsonArray();
                	for (int i=0; i<ar.size(); i++) {
                		JsonObject user = ar.get(i).getAsJsonObject();
                		try {
	                		String id = user.get("userId").getAsString();
	                		String fn = user.has("firstName") ? user.get("firstName").getAsString() : "<BLANK>";
	                		String ln = user.has("lastName") ? user.get("lastName").getAsString() : "<BLANK>";
	                		String email = user.has("email") ? user.get("email").getAsString() : "<NO EMAIL>";
	                		String name = fn + " " + ln;
	                		if (name.equals("<BLANK> <BLANK>")) {
	                			name = email;
	                		}
	                		userNamesById.put(id, name);
                		} catch (Throwable e) {
                			System.err.println(e);
                		}
                	}
                }
                if (jo.has("meta") && jo.get("meta").getAsJsonObject().has("pages")) {
                	JsonElement nextPage = jo.get("meta").getAsJsonObject().get("pages").getAsJsonObject().get("next-page");
                	if (nextPage == null) {
//                		System.out.println("NULUUUUUUULLLL");
                		return 0;
                	} else if (nextPage.isJsonNull()) {
//                		System.out.println("JSON   NULUUUUUUULLLL");
                		return 0;
                	} else {
//                		System.out.println("Page is number " + nextPage.getAsInt());
                		return nextPage.getAsInt();
                	}
                } else {
                	return 0;
                }
			} else {  // response code != 200
				BufferedReader reader = new BufferedReader(response.body().charStream());
				String line;
				while ((line = reader.readLine()) != null) {
					System.err.println(line);
				}
				return -1;
			}
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.toString());
			return -1;
		}
    }
    
    
    /** Hacky hacky for now, using v0 undocumented API */
    public boolean loadUsersCustom() {
    	long start = System.currentTimeMillis();
    	userNamesById = new LinkedHashMap<>();
    	int page=0;
    	do {
    		page = fetchUsers(page);
    	} while (page > 0);
    	if (page == -1) {
    		return false;
    	}
        System.out.printf("loadUsersCustom() loaded %d Users, and took %dms.%n", userNamesById.size(), System.currentTimeMillis() - start);
//        System.out.println(userNamesById.keySet());
    	return true;
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
		if (applicationVersionsByApplicationId.get(applicationId) == null) return Collections.emptySet();
    	return Collections.unmodifiableSet(applicationVersionsByApplicationId.get(applicationId));
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
	

	
	// enums...
	public TopicAddressEnum getEnum(String id) {
		return enumsById.get(id);
	}

	public Collection<TopicAddressEnum> getEnums() {
		return Collections.unmodifiableCollection(enumsById.values());
	}

	public Set<TopicAddressEnum> getEnumsForDomainId(String domainId) {
		if (enumsByDomainId.get(domainId) == null) return Collections.emptySet();
		return Collections.unmodifiableSet(enumsByDomainId.get(domainId));
	}
	
	// Enum Versions...
	/**
	 * @param id
	 * @return null if not found, otherwise object
	 */
	public TopicAddressEnumVersion getEnumVersion(String id) {
		return enumVersionsById.get(id);
	}
	
	public Collection<TopicAddressEnumVersion> getEnumVersions() {
		return Collections.unmodifiableCollection(enumVersionsById.values());
	}
	
	/**
	 * 
	 * @param enumId
	 * @return empty list if not found, otherwise Set
	 */
	public Set<TopicAddressEnumVersion> getEnumVersionsForEnumId(String enumId) {
		if (enumVersionsByEnumId.get(enumId) == null) return Collections.emptySet();
		return Collections.unmodifiableSet(enumVersionsByEnumId.get(enumId));
	}

	
	
	
	// event APIs
	public EventApi getEventApi(String id) {
		return eventApisById.get(id);
	}
	
	public Collection<EventApi> getEventApis() {
		return Collections.unmodifiableCollection(eventApisById.values());
	}

	public Set<EventApi> getEventApisForDomainId(String domainId) {
		if (eventApisByDomainId.get(domainId) == null) return Collections.emptySet();
		return Collections.unmodifiableSet(eventApisByDomainId.get(domainId));
	}

	public EventApiVersion getEventApiVersion(String id) {
		return eventApiVersionsById.get(id);
	}
	
	public Collection<EventApiVersion> getEventApiVersions() {
		return Collections.unmodifiableCollection(eventApiVersionsById.values());
	}

	public Set<EventApiVersion> getEventApiVersionsForEventApiId(String eventApiId) {
		if (eventApiVersionsByEventApiId.get(eventApiId) == null) return Collections.emptySet();
		return Collections.unmodifiableSet(eventApiVersionsByEventApiId.get(eventApiId));
	}

	

	
	// event APIs
	public EventApiProduct getEventApiProduct(String id) {
		return eventApiProductsById.get(id);
	}
	
	public Collection<EventApiProduct> getEventApiProducts() {
		return Collections.unmodifiableCollection(eventApiProductsById.values());
	}

	public Set<EventApiProduct> getEventApiProductsForDomainId(String domainId) {
		if (eventApiProductsByDomainId.get(domainId) == null) return Collections.emptySet();
		return Collections.unmodifiableSet(eventApiProductsByDomainId.get(domainId));
	}

	public EventApiProductVersion getEventApiProductVersion(String id) {
		return eventApiProductVersionsById.get(id);
	}
	
	public Collection<EventApiProductVersion> getEventApiProductVersions() {
		return Collections.unmodifiableCollection(eventApiProductVersionsById.values());
	}

	public Set<EventApiProductVersion> getEventApiProductVersionsForEventApiProductId(String eventApiProductId) {
		if (eventApiProductVersionsByEventApiProductId.get(eventApiProductId) == null) return Collections.emptySet();
		return Collections.unmodifiableSet(eventApiProductVersionsByEventApiProductId.get(eventApiProductId));
	}

	
	
	
	public StateDTO getState(String id) {
		return statesById.get(id);
	}
	
	public Collection<StateDTO> getStates() {
		return Collections.unmodifiableCollection(statesById.values());
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

	

	
	public EventMesh getEventMesh(String id) {
		return eventMeshesById.get(id);
	}
	
	public Collection<EventMesh> getEventMeshes() {
		return Collections.unmodifiableCollection(eventMeshesById.values());
	}
	
	public Set<EventMesh> getEventMeshsForEnvironmentId(String environtmentId) {
		if (!eventMeshesByEnvironmentId.containsKey(environtmentId)) return Collections.emptySet();
		return Collections.unmodifiableSet(eventMeshesByEnvironmentId.get(environtmentId));
	}

	
	
	
	
	
	
	public Set<TopicDomain> getTopicDomainsForDomainId(String domainId) {
		if (topicDomainsByDomainId.get(domainId) == null) return Collections.emptySet();
		return Collections.unmodifiableSet(topicDomainsByDomainId.get(domainId));
	}

	
	
	
	
	public Environment getEnvironment(String id) {
		return environmentsById.get(id);
	}
	
	public Collection<Environment> getEnvironments() {
		return Collections.unmodifiableCollection(environmentsById.values());
	}
	
	

	/** Returns the actual user's name for a given ID if found; else it returns the ID */
	public String getUserName(String id) {
//		return String.format("%d Users loaded, *%s* (%d), *%s*, %b, %s, closest=%s", userNamesById.size(), id, id.length(), newId, userNamesById.containsKey(id), userNamesById.get(id), closest);
		if (userNamesById.containsKey(id)) return userNamesById.get(id);
		return id;
	}

	public Collection<String> getUserIds() {
		return Collections.unmodifiableCollection(userNamesById.keySet());
	}

	public Collection<String> getUserNames() {
		return Collections.unmodifiableCollection(userNamesById.values());
	}
	
	
	
	/**
	 * Possible helper function? Pass in the ID for any Event Portal object, and this
	 * will return an enum EventPortalObjectType of the object type. <b>NOTE:</b> Event
	 * Portal objects must first be loaded (loadAll()) for this to work.
	 * @param id the EventPortal ID of any type of object
	 * @return
	 */
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
	
	public String getRandomUserName() {
		int i = (int)(Math.random() * userNamesById.size());
		return userNamesById.values().stream().skip(i).findAny().orElse(null);
	}
	
	public String getAsyncApiForAppVerId(String appVerId, boolean prettyPrint) {
		ApiClient apiClient = getApiClient();
		ApplicationsApi api = new ApplicationsApi(apiClient);
//		api.getAsyncApiForApplicationVersion(appVer.getId(), "json", "2.0.0");
		try {
			Object treeMap = api.getAsyncApiForApplicationVersion(appVerId, null, null);
			if (prettyPrint) {
				String json = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(treeMap);
				return json;
			} else {
				String json = new Gson().toJson(treeMap);
				return json;
			}
		} catch (ApiException e) {
			System.err.println(e.getResponseBody());
			return null;
		}
	}
	
	public String getAsyncApiForEventApiVerId(String eventApiVerId, boolean prettyPrint) {
		ApiClient apiClient = getApiClient();
		EventApIsApi api = new EventApIsApi(apiClient);
//		api.getAsyncApiForApplicationVersion(appVer.getId(), "json", "2.0.0");
		try {
			Object treeMap = api.getAsyncApiForEventApiVersion(eventApiVerId, null, null, null);
			if (prettyPrint) {
				String json = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(treeMap);
				return json;
			} else {
				String json = new Gson().toJson(treeMap);
				return json;
			}
		} catch (ApiException e) {
			System.err.println(e.getResponseBody());
			return null;
		}
	}
	

	/**
	 * Semi-useful method to find all Applications (not versions) using any version of
	 * an Event.
	 * @param eventId the ID of the event (not EventVersion)
	 * @return a Set of all Application names and Domains that either publish or subscribe to this Event
	 */
	public Set<String> getAppsUsingEvent(String eventId) {
		
		Set<EventVersion> eventVersions = getEventVersionsForEventId(eventId);
		if (eventVersions == null) return Collections.emptySet();
		Set<Application> apps = new LinkedHashSet<>();
		Set<String> appDescs = new LinkedHashSet<>();
		for (EventVersion eventVersion : eventVersions) {
			for (String appVerId : eventVersion.getDeclaredConsumingApplicationVersionIds()) {
				Application app = getApplication(getApplicationVersion(appVerId).getApplicationId());
				apps.add(app);
				ApplicationDomain domain = getDomain(app.getApplicationDomainId());
				appDescs.add(String.format("'%s' in '%s'", app.getName(), domain.getName()));
			}
			for (String appVerId : eventVersion.getDeclaredProducingApplicationVersionIds()) {
				Application app = getApplication(getApplicationVersion(appVerId).getApplicationId());
				apps.add(app);
				ApplicationDomain domain = getDomain(app.getApplicationDomainId());
				appDescs.add(String.format("'%s' in '%s'", app.getName(), domain.getName()));
			}
		}
		return appDescs;
	}
}
