package community.solace.ep.wrapper;

import java.io.BufferedReader;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import community.solace.ep.client.ApiClient;
import community.solace.ep.client.ApiException;
import community.solace.ep.client.Configuration;
import community.solace.ep.client.api.ApplicationDomainsApi;
import community.solace.ep.client.api.ApplicationsApi;
import community.solace.ep.client.api.ConsumersApi;
import community.solace.ep.client.api.EnumsApi;
import community.solace.ep.client.api.EnvironmentsApi;
import community.solace.ep.client.api.EventApiProductsApi;
import community.solace.ep.client.api.EventApisApi;
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
import community.solace.plat.client.api.UsersApi;
import community.solace.plat.client.model.UserRequest;
import community.solace.plat.client.model.UserResponse;
import community.solace.plat.client.model.UsersResponseEnvelope;
import dev.solace.aaron.useful.TimeUtils;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public enum EventPortalWrapper {
	
	INSTANCE;  // singleton pattern

	private static final Logger logger = LoggerFactory.getLogger(EventPortalWrapper.class);
	
	private static volatile boolean lazyLoadSchemaVers = false;  // not implemented yet!
	private static String userAgent = "AaJavaEpWrapper/20241014";

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
    private Set<String> loadErrorStrings = new HashSet<>();
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
    
    public String getUserAgent() {
    	return userAgent;
    }

    /**
     * This will append additional information onto the already configured User Agent.
     * Call this before connecting.
     * @param extra pass in the application name or something using this wrapper.
     */
    public void addUserAgentDetails(String extra) {
    	userAgent += " " + extra;
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
        if (!loadDomains()) {  // block on this one at least
        	System.err.println("Could not even load any domains! " + loadErrorStrings.toString());
        	loadStatus = LoadStatus.ERROR;
        	return false;
        }
		AtomicInteger num = new AtomicInteger(1);  // start the counter at one job, just to make sure the while loop below doesn't miss it
//		pool.submit(() -> { loadDomains();		num.decrementAndGet(); });  // don't count first one
//		pool.submit(() -> { num.incrementAndGet();	loadApplications();	num.decrementAndGet(); });
		pool.submit(() -> { loadApplications(false);	num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadApplicationVersions();	num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadEvents(false);			num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadEventVersions();		num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadSchemas(false);			num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadSchemaVersions();		num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadEnums(false);			num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadEnumVersions();			num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadEventApis(false);		num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadEventApiVersions();		num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadEventApiProducts(false);	num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadEventApiProductVersions();	num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadStates();				num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadConsumers();			num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadEventMeshes();			num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadTopicDomains();			num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadEnvironments();			num.decrementAndGet(); });
//		pool.submit(() -> { num.incrementAndGet();	loadUsersCustom();			num.decrementAndGet(); });
//		pool.submit(() -> { num.incrementAndGet();	loadUsers();			num.decrementAndGet(); });
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
	 * Loads all objects in parallel using ExecutorService.  If loading is unsuccessful, query `getLoadException()`
	 * to find out issue.
     * @param pool an ExecutorService to use for loading objects
	 * @return true if loading successful; false if any issue was encountered (although perhaps partial success)
     */
	public boolean loadAll(ExecutorService pool, EnumSet<SupportedObjectType> typesToLoad) {
		if (loadStatus == LoadStatus.LOADING) {
			return false;
		}
		loadStatus = LoadStatus.LOADING;
	    long start = System.currentTimeMillis();
//        loadException = null;  // blank it, used at end to make sure no loading errors
        loadErrorStrings = Collections.synchronizedSet(new HashSet<>());
        if (!loadDomains()) {  // block on this one at least
        	System.err.println("Could not even load any domains! " + loadErrorStrings.toString());
        	loadStatus = LoadStatus.ERROR;
        	return false;
        }
		AtomicInteger num = new AtomicInteger(1);  // start the counter at one job, just to make sure the while loop below doesn't miss it
//		pool.submit(() -> { loadDomains();		num.decrementAndGet(); });  // don't count first one
//		pool.submit(() -> { num.incrementAndGet();	loadApplications();	num.decrementAndGet(); });
		if (typesToLoad.contains(SupportedObjectType.APPLICATION) || typesToLoad.contains(SupportedObjectType.APPLICATION_VERSION))
			pool.submit(() -> { loadApplications();	num.decrementAndGet(); });
		if (typesToLoad.contains(SupportedObjectType.EVENT) || typesToLoad.contains(SupportedObjectType.EVENT_VERSION))
			pool.submit(() -> { num.incrementAndGet();	loadEvents();		num.decrementAndGet(); });
		if (typesToLoad.contains(SupportedObjectType.SCHEMA) || typesToLoad.contains(SupportedObjectType.SCHEMA_VERSION))
			pool.submit(() -> { num.incrementAndGet();	loadSchemas();		num.decrementAndGet(); });
		if (typesToLoad.contains(SupportedObjectType.ENUM) || typesToLoad.contains(SupportedObjectType.ENUM_VERSION))
			pool.submit(() -> { num.incrementAndGet();	loadEnums();		num.decrementAndGet(); });
		if (typesToLoad.contains(SupportedObjectType.EVENT_API) || typesToLoad.contains(SupportedObjectType.EVENT_API_VERSION))
			pool.submit(() -> { num.incrementAndGet();	loadEventApis();	num.decrementAndGet(); });
		if (typesToLoad.contains(SupportedObjectType.EVENT_API_PRODUCT) || typesToLoad.contains(SupportedObjectType.EVENT_API_PRODUCT_VERSION))
			pool.submit(() -> { num.incrementAndGet();	loadEventApiProducts();	num.decrementAndGet(); });
		if (typesToLoad.contains(SupportedObjectType.STATE))
			pool.submit(() -> { num.incrementAndGet();	loadStates();		num.decrementAndGet(); });
		if (typesToLoad.contains(SupportedObjectType.CONSUMER))
			pool.submit(() -> { num.incrementAndGet();	loadConsumers();	num.decrementAndGet(); });
		if (typesToLoad.contains(SupportedObjectType.EVENT_MESH))
			pool.submit(() -> { num.incrementAndGet();	loadEventMeshes();	num.decrementAndGet(); });
		if (typesToLoad.contains(SupportedObjectType.TOPIC_DOMAIN))
			pool.submit(() -> { num.incrementAndGet();	loadTopicDomains();	num.decrementAndGet(); });
		if (typesToLoad.contains(SupportedObjectType.ENVIRONMENT))
			pool.submit(() -> { num.incrementAndGet();	loadEnvironments();	num.decrementAndGet(); });
		pool.submit(() -> { num.incrementAndGet();	loadUsersCustom();	num.decrementAndGet(); });

		while (num.get() > 0) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) { }
		}
		if (!loadErrorStrings.isEmpty()) {
	        System.err.println("EventPortalWrapper had issues loading: " + loadErrorStrings.toString());
	        logger.warn("EventPortalWrapper had issues loading: " + loadErrorStrings.toString());
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
	public boolean load(EnumSet<SupportedObjectType> typesToLoad) {
		if (loadStatus == LoadStatus.LOADING) {
			return false;
		}
		loadStatus = LoadStatus.LOADING;
	    long start = System.currentTimeMillis();
        loadErrorStrings = Collections.synchronizedSet(new HashSet<>());
        if (!loadDomains()) return false;
        if (typesToLoad.contains(SupportedObjectType.APPLICATION) || typesToLoad.contains(SupportedObjectType.APPLICATION_VERSION)) loadApplications();
        if (typesToLoad.contains(SupportedObjectType.EVENT) || typesToLoad.contains(SupportedObjectType.EVENT_VERSION)) loadEvents();
        if (typesToLoad.contains(SupportedObjectType.SCHEMA) || typesToLoad.contains(SupportedObjectType.SCHEMA_VERSION)) loadSchemas();
        if (typesToLoad.contains(SupportedObjectType.ENUM) || typesToLoad.contains(SupportedObjectType.ENUM_VERSION)) loadEnums();
        if (typesToLoad.contains(SupportedObjectType.EVENT_API) || typesToLoad.contains(SupportedObjectType.EVENT_API_VERSION)) loadEventApis();
        if (typesToLoad.contains(SupportedObjectType.EVENT_API_PRODUCT) || typesToLoad.contains(SupportedObjectType.EVENT_API_PRODUCT_VERSION)) loadEventApiProducts();
        if (typesToLoad.contains(SupportedObjectType.STATE)) loadStates();
        if (typesToLoad.contains(SupportedObjectType.CONSUMER)) loadConsumers();
        if (typesToLoad.contains(SupportedObjectType.EVENT_MESH)) loadEventMeshes();
        if (typesToLoad.contains(SupportedObjectType.TOPIC_DOMAIN)) loadTopicDomains();
        if (typesToLoad.contains(SupportedObjectType.ENVIRONMENT)) loadEnvironments();
        if (typesToLoad.contains(SupportedObjectType.USER)) loadUsersCustom();  // either way, keep going even if this breaks
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

	public void loadAsync(SupportedObjectType type, ExecutorService pool) {
		pool.submit(() -> {
//			num.incrementAndGet();
			loadNew(type);
//			num.decrementAndGet();
		});

	}

	
	public boolean loadNew(SupportedObjectType type) {
		
		switch (type) {
		case APPLICATION:
			return loadApplications(false);
		case APPLICATION_VERSION:
			return loadApplicationVersions();
//		case CONFIGURATION:
//			return loadConf
//		case CONFIGURATION_TYPE:
//			break;
		case CONSUMER:
			return loadConsumers();
//		case CUSTOM_ATTRIBUTE:
//			break;
		case DOMAIN:
			return loadDomains();
		case ENUM:
			return loadEnums(false);
		case ENUM_VERSION:
			return loadEnumVersions();
		case ENVIRONMENT:
			return loadEnvironments();
		case EVENT:
			return loadEvents(false);
		case EVENT_API:
			return loadEventApis(false);
		case EVENT_API_PRODUCT:
			return loadEventApiProducts(false);
		case EVENT_API_PRODUCT_VERSION:
			return loadEventApiProductVersions();
		case EVENT_API_VERSION:
			return loadEventApiVersions();
		case EVENT_MESH:
			return loadEventMeshes();
		case EVENT_VERSION:
			return loadEventVersions();
		case SCHEMA:
			return loadSchemas(false);
		case SCHEMA_VERSION:
			return loadSchemaVersions();
		case STATE:
			return loadStates();
		case TOPIC_DOMAIN:
			return loadTopicDomains();
		case USER:
			return loadUsers();
		case N_A:
		default:
			return false;
		
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
//        apiClient.setBasePath("https://solace-sso.solace.cloud");  // https://api.solace.cloud
        apiClient.setBasePath("https://api.solace.cloud");  // https://api.solace.cloud
//        apiClient.setAccessToken(token);  // nope
//        apiClient.setApiKey(token);  // nope
        apiClient.setBearerToken(token);
        if (userAgent != null || !userAgent.isEmpty()) {
        	apiClient.setUserAgent(userAgent);
        }
        return apiClient;
    }
    
    // different one for the platform API
    public community.solace.plat.client.ApiClient getPlatApiClient() {
    	community.solace.plat.client.ApiClient apiClient = community.solace.plat.client.Configuration.getDefaultApiClient();
        apiClient.setBasePath("https://api.solace.cloud");  // https://api.solace.cloud
    	apiClient.setBearerToken(token);
        if (userAgent != null || !userAgent.isEmpty()) {
        	apiClient.setUserAgent(userAgent);
        }
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
	        	response = apiDomains.getApplicationDomains(PAGE_SIZE, page++, null, null, Collections.singleton("stats"));
//	        	response = apiDomains.getApplicationDomains(PAGE_SIZE, page++, null, null, null);
	        	for (ApplicationDomain domain : response.getData()) {
	        		domains.put(domain.getId(), domain);
	        	}
	        } while (response.getMeta().getPagination().getNextPage() != null);
	        String log = String.format("loadDomains() loaded %d domains, and took %s", domains.size(), TimeUtils.formatDeltaMs(start));
	        System.out.println(log);
	        logger.info(log);
	        return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load Domains: " + e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.toString());
    		return false;
    	}
    }
    
    public boolean loadApplications() {
    	return loadApplications(true);
    }
    
    public boolean loadApplications(boolean loadVersionsToo) {
        ApiClient apiClient = getApiClient();
    	try {
	    	long start = System.currentTimeMillis();
	    	applicationsById = new LinkedHashMap<>();
	    	applicationsByDomainId = new LinkedHashMap<>();
	        ApplicationsApi apiApps = new ApplicationsApi(apiClient);
	        int page = 1;
	        ApplicationsResponse response2;
	        do {
//	        	apiApps.getApplications(null, null, token, token, loadErrorStrings, token, token, token)
	        	response2 = apiApps.getApplications(PAGE_SIZE, page++, null, null, null, null, null, null);
	        	for (Application app : response2.getData()) {
	        		applicationsById.put(app.getId(), app);
	        		if (applicationsByDomainId.get(app.getApplicationDomainId()) == null) {
	        			applicationsByDomainId.put(app.getApplicationDomainId(), new LinkedHashSet<>());
	        		}
	        		applicationsByDomainId.get(app.getApplicationDomainId()).add(app);
	        	}
	        } while (response2.getMeta().getPagination().getNextPage() != null);
	        String log = String.format("loadApplications() loaded %d apps, and took %s",
	        		applicationsById.size(), TimeUtils.formatDeltaMs(start));
	        System.out.println(log);
	        logger.info(log);
	        if (loadVersionsToo) return loadApplicationVersions();
	        else return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load Apps: " + e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load Apps: " + e.toString());
    		return false;
    	}
    }
    
    public boolean loadApplicationVersions() {
    	return loadApplicationVersions(1, Integer.MAX_VALUE);
    }

    public boolean loadApplicationVersions(int pageStart, int pageNums) {
    	pageStart = Math.max(1, pageStart);
        ApiClient apiClient = getApiClient();
    	try {
	    	long start = System.currentTimeMillis();
	    	int page = pageStart;
	    	ApplicationsApi apiApps = new ApplicationsApi(apiClient);
	        applicationVersionsById = new LinkedHashMap<>();
	        applicationVersionsByApplicationId = new LinkedHashMap<>();
	        ApplicationVersionsResponse response;
	        do {
	        	response = apiApps.getApplicationVersions(PAGE_SIZE, page++, null, null, null, null);
	            for (ApplicationVersion appVer : response.getData()) {
	            	applicationVersionsById.put(appVer.getId(), appVer);
	            	if (applicationVersionsByApplicationId.get(appVer.getApplicationId()) == null) {
	            		applicationVersionsByApplicationId.put(appVer.getApplicationId(), new LinkedHashSet<>());
	            	}
	            	applicationVersionsByApplicationId.get(appVer.getApplicationId()).add(appVer);
	            }
	            pageNums--;
	        } while (pageNums > 0 && response.getMeta().getPagination().getNextPage() != null);
	        String log = String.format("loadApplicationVersions() loaded %d versions, and took %s",
	        		applicationVersionsById.size(), TimeUtils.formatDeltaMs(start));
//	        System.out.println(log);
	        logger.info(log);
    		return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load AppVersions: " + e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load AppVersions: " + e.toString());
    		return false;
    	}
    }
    
    public boolean loadEvents() {
    	return loadEvents(true);
    }

    public boolean loadEvents(boolean loadVersionsToo) {
        ApiClient apiClient = getApiClient();
    	try {
	    	eventsById = new LinkedHashMap<>();
	    	eventsByDomainId = new LinkedHashMap<>();
	    	long start = System.currentTimeMillis();
	        EventsApi eventsApi = new EventsApi(apiClient);
	        apiClient.setReadTimeout(20000);
	        EventsResponse eventsReponse;
	        int page = 1;
	        do {
//	        	long pageTime = System.currentTimeMillis();
//	        	System.out.print("Fetching Events page " + page + "... ");
	        	eventsReponse = eventsApi.getEvents(PAGE_SIZE, page++, null, null, null, null, null, null, null, null);
		        for (Event event : eventsReponse.getData()) {
		        	eventsById.put(event.getId(), event);
		        	if (eventsByDomainId.get(event.getApplicationDomainId()) == null) {
		        		eventsByDomainId.put(event.getApplicationDomainId(), new LinkedHashSet<>());
		        	}
		        	eventsByDomainId.get(event.getApplicationDomainId()).add(event);
		        }
//	        	System.out.printf("took %d ms, %d total events fetched%n", System.currentTimeMillis()-pageTime, eventsById.size());
	        } while (eventsReponse.getMeta().getPagination().getNextPage() != null);
	        String log = String.format("loadEvents() loaded %d events, and took %s",
	        		eventsById.size(), TimeUtils.formatDeltaMs(start));
//	        System.out.println(log);
	        logger.info(log);
	        if (loadVersionsToo) return loadEventVersions();
	        else return true;
    	} catch (ApiException e) {
//    		e.printStackTrace();
//    		System.err.println(e.getResponseBody());
    		logger.warn("Caught during loadEvents()", e);
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load Events: " + e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
    		logger.warn("Caught during loadEvents()", e);
//    		e.printStackTrace();
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load Events: " + e.toString());
    		return false;
    	}
    }

    public boolean loadEventVersions() {
    	return loadEventVersions(1, Integer.MAX_VALUE);
    }

    public boolean loadEventVersions(int pageStart, int pageNums) {
    	pageStart = Math.max(1, pageStart);
        ApiClient apiClient = getApiClient();
    	try {
    		long start = System.currentTimeMillis();
    		int page = pageStart;
    		EventsApi eventsApi = new EventsApi(apiClient);
	        eventVersionsById = new LinkedHashMap<>();
	        eventVersionsByEventId = new LinkedHashMap<>();
	        EventVersionsResponse eventVersionsResponse;
	        do {
//	        	long pageTime = System.currentTimeMillis();
//	        	System.out.print("Fetching Event Versions page " + page + "... ");
	        	eventVersionsResponse = eventsApi.getEventVersions(PAGE_SIZE, page++, null, null, null, null, null);
	        	for (EventVersion eventVersion : eventVersionsResponse.getData()) {
	        		eventVersionsById.put(eventVersion.getId(), eventVersion);
	        		if (eventVersionsByEventId.get(eventVersion.getEventId()) == null) {
	        			eventVersionsByEventId.put(eventVersion.getEventId(), new LinkedHashSet<>());
	        		}
	        		eventVersionsByEventId.get(eventVersion.getEventId()).add(eventVersion);
	        	}
	        	pageNums--;
//	        	System.out.printf("took %s, %d total event versions fetched%n", TimeUtils.formatDeltaMs(pageTime), eventVersionsById.size());
	        } while (pageNums > 0 && eventVersionsResponse.getMeta().getPagination().getNextPage() != null);
	        String log = String.format("loadEventVersions() loaded %d versions, and took %s",
	        		eventVersionsById.size(), TimeUtils.formatDeltaMs(start));
//	        System.out.println(log);
	        logger.info(log);
    		return true;
    	} catch (ApiException e) {
    		logger.warn("Caught during loadEventVersions(): " + e.getResponseBody(), e);
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load EventVersions: " + e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
    		logger.warn("Caught during loadEventVersions()", e);
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load EventVersions: " + e.toString());
    		return false;
    	}
    }
    
    public boolean loadSchemas() {
    	return loadSchemas(true);
    }
    
    public boolean loadSchemas(boolean loadVersionsToo) {
        ApiClient apiClient = getApiClient();
    	try {
	    	long start = System.currentTimeMillis();
	    	schemasById = new LinkedHashMap<>();
	    	schemasByDomainId = new LinkedHashMap<>();
	        SchemasApi schemasApi = new SchemasApi(apiClient);
	        SchemasResponse schemasReponse;
	        int page = 1;
	        do {
	        	schemasReponse = schemasApi.getSchemas(PAGE_SIZE, page++, null, null, null, null, null, null, null, null);
		        for (SchemaObject schema : schemasReponse.getData()) {
		        	schemasById.put(schema.getId(), schema);
		        	if (schemasByDomainId.get(schema.getApplicationDomainId()) == null) {
		        		schemasByDomainId.put(schema.getApplicationDomainId(), new LinkedHashSet<>());
		        	}
		        	schemasByDomainId.get(schema.getApplicationDomainId()).add(schema);
		        }
	        } while (schemasReponse.getMeta().getPagination().getNextPage() != null);
	        String log = String.format("loadSchemas() loaded %d schemas, and took %s",
	        		schemasById.size(), TimeUtils.formatDeltaMs(start));
//	        System.out.println(log);
	        logger.info(log);
	        if (loadVersionsToo) return loadSchemaVersions();
	        else return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load Schemas: " + e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load Schemas: " + e.toString());
    		return false;
    	}
    }

    public boolean loadSchemaVersions() {
        ApiClient apiClient = getApiClient();
    	try {
	    	long start = System.currentTimeMillis();
	        SchemasApi schemasApi = new SchemasApi(apiClient);
	        int page = 1;
	        schemaVersionsById = new LinkedHashMap<>();
	        schemaVersionsBySchemaId = new LinkedHashMap<>();
	        if (!lazyLoadSchemaVers) {  // else we'll try and get them on-demand
		        SchemaVersionsResponse schemaVersionsResponse;
		        do {
		        	schemaVersionsResponse = schemasApi.getSchemaVersions(PAGE_SIZE, page++, null, null, null);
		        	for (SchemaVersion schemaVersion : schemaVersionsResponse.getData()) {
		        		schemaVersionsById.put(schemaVersion.getId(), schemaVersion);
		        		if (schemaVersionsBySchemaId.get(schemaVersion.getSchemaId()) == null) {
		        			schemaVersionsBySchemaId.put(schemaVersion.getSchemaId(), new LinkedHashSet<>());
		        		}
		        		schemaVersionsBySchemaId.get(schemaVersion.getSchemaId()).add(schemaVersion);
		        	}
		        } while (schemaVersionsResponse.getMeta().getPagination().getNextPage() != null);
	        }
	        String log = String.format("loadSchemaVersions() loaded %d versions, and took %s",
	        		schemaVersionsById.size(), TimeUtils.formatDeltaMs(start));
//	        System.out.println(log);
	        logger.info(log);
    		return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load SchemaVersions: " + e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load SchemaVersions: " + e.toString());
    		return false;
    	}
    }
    
    public boolean loadEnums() {
    	return loadEnums(true);
    }
    
    public boolean loadEnums(boolean loadVersionsToo) {
        ApiClient apiClient = getApiClient();
    	try {
	    	enumsById = new LinkedHashMap<>();
	    	enumsByDomainId = new LinkedHashMap<>();
	    	long start = System.currentTimeMillis();
	        EnumsApi enumsApi = new EnumsApi(apiClient);
	        TopicAddressEnumsResponse enumsReponse;
	        int page = 1;
	        do {
	        	enumsReponse = enumsApi.getEnums(PAGE_SIZE, page++, null, null, null, null, null, null, null);
		        for (TopicAddressEnum topicAddressEnum : enumsReponse.getData()) {
		        	enumsById.put(topicAddressEnum.getId(), topicAddressEnum);
		        	if (enumsByDomainId.get(topicAddressEnum.getApplicationDomainId()) == null) {
		        		enumsByDomainId.put(topicAddressEnum.getApplicationDomainId(), new LinkedHashSet<>());
		        	}
		        	enumsByDomainId.get(topicAddressEnum.getApplicationDomainId()).add(topicAddressEnum);
		        }
	        } while (enumsReponse.getMeta().getPagination().getNextPage() != null);
	        String log = String.format("loadEnums() loaded %d enums, and took %s",
	        		enumsById.size(), TimeUtils.formatDeltaMs(start));
//	        System.out.println(log);
	        logger.info(log);
	        if (loadVersionsToo) return loadEnumVersions();
	        else return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load Enums: " + e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load Enums: " + e.toString());
    		return false;
    	}
    }
    
    public boolean loadEnumVersions() {
        ApiClient apiClient = getApiClient();
    	try {
	    	enumsById = new LinkedHashMap<>();
	    	enumsByDomainId = new LinkedHashMap<>();
	    	long start = System.currentTimeMillis();
	        EnumsApi enumsApi = new EnumsApi(apiClient);
	        int page = 1;
	        TopicAddressEnumVersionsResponse enumVersionsResponse;
	        do {
	        	enumVersionsResponse = enumsApi.getEnumVersions(PAGE_SIZE, page++, null, null);
	        	for (TopicAddressEnumVersion enumVersion : enumVersionsResponse.getData()) {
	        		enumVersionsById.put(enumVersion.getId(), enumVersion);
	        		if (enumVersionsByEnumId.get(enumVersion.getEnumId()) == null) {
	        			enumVersionsByEnumId.put(enumVersion.getEnumId(), new LinkedHashSet<>());
	        		}
	        		enumVersionsByEnumId.get(enumVersion.getEnumId()).add(enumVersion);
	        	}
	        } while (enumVersionsResponse.getMeta().getPagination().getNextPage() != null);
	        String log = String.format("loadEnumVersions() loaded %d versions, and took %s",
	        		enumVersionsById.size(), TimeUtils.formatDeltaMs(start));
//	        System.out.println(log);
	        logger.info(log);
    		return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load EnumVersions: " + e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load EnumVersions: " + e.toString());
    		return false;
    	}
    }
    public boolean loadEventApis() {
    	return loadEventApis(true);
    }
    
    public boolean loadEventApis(boolean loadVersionsToo) {
        ApiClient apiClient = getApiClient();
    	try {
	    	long start = System.currentTimeMillis();
	    	eventApisById = new LinkedHashMap<>();
	    	eventApisByDomainId = new LinkedHashMap<>();
	    	EventApisApi eventApisApi = new EventApisApi(apiClient);
	        EventApisResponse eventApisReponse;
	        int page = 1;
	        do {
	        	eventApisReponse = eventApisApi.getEventApis(PAGE_SIZE, page++, null, null, null, null, null, null, null, null, null, null);
		        for (EventApi eventApi : eventApisReponse.getData()) {
		        	eventApisById.put(eventApi.getId(), eventApi);
		        	if (eventApisByDomainId.get(eventApi.getApplicationDomainId()) == null) {
		        		eventApisByDomainId.put(eventApi.getApplicationDomainId(), new LinkedHashSet<>());
		        	}
		        	eventApisByDomainId.get(eventApi.getApplicationDomainId()).add(eventApi);
		        }
	        } while (eventApisReponse.getMeta().getPagination().getNextPage() != null);
	        String log = String.format("loadEventApis() loaded %d Event APIs, and took %s",
	        		eventApisById.size(), TimeUtils.formatDeltaMs(start));
//	        System.out.println(log);
	        logger.info(log);
	        if (loadVersionsToo) return loadEventApiVersions();
	        else return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load Event APIs: " + e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load Event APIs: " + e.toString());
    		return false;
    	}
    }

    public boolean loadEventApiVersions() {
        ApiClient apiClient = getApiClient();
    	try {
	    	long start = System.currentTimeMillis();
	    	EventApisApi eventApisApi = new EventApisApi(apiClient);
	        int page = 1;
	        eventApiVersionsById = new LinkedHashMap<>();
	        eventApiVersionsByEventApiId = new LinkedHashMap<>();
	        EventApiVersionsResponse eventApiVersionsResponse;
	        do {
	        	eventApiVersionsResponse = eventApisApi.getEventApiVersions(PAGE_SIZE, page++, null, null, null, null, null);
	        	for (EventApiVersion eventApiVersion : eventApiVersionsResponse.getData()) {
	        		eventApiVersionsById.put(eventApiVersion.getId(), eventApiVersion);
	        		if (eventApiVersionsByEventApiId.get(eventApiVersion.getEventApiId()) == null) {
	        			eventApiVersionsByEventApiId.put(eventApiVersion.getEventApiId(), new LinkedHashSet<>());
	        		}
	        		eventApiVersionsByEventApiId.get(eventApiVersion.getEventApiId()).add(eventApiVersion);
	        	}
	        } while (eventApiVersionsResponse.getMeta().getPagination().getNextPage() != null);
	        String log = String.format("loadEventApiVersions() loaded %d versions, and took %s",
	        		eventApiVersionsById.size(), TimeUtils.formatDeltaMs(start));
//	        System.out.println(log);
	        logger.info(log);
    		return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load Event API Versions: " + e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load Event API Versions: " + e.toString());
    		return false;
    	}
    }
    
    public boolean loadEventApiProducts() {
    	return loadEventApiProducts(true);
    }

    public boolean loadEventApiProducts(boolean loadVersionsToo) {
        ApiClient apiClient = getApiClient();
    	try {
	    	long start = System.currentTimeMillis();
	    	eventApiProductsById = new LinkedHashMap<>();
	    	eventApiProductsByDomainId = new LinkedHashMap<>();
	    	EventApiProductsApi eventApiProductsApi = new EventApiProductsApi(apiClient);
	        EventApiProductsResponse eventApiProductsReponse;
	        int page = 1;
	        do {
	        	eventApiProductsReponse = eventApiProductsApi.getEventApiProducts(PAGE_SIZE, page++, null, null, null, null, null, null, null, null);
		        for (EventApiProduct eventApiProduct : eventApiProductsReponse.getData()) {
		        	eventApiProductsById.put(eventApiProduct.getId(), eventApiProduct);
		        	if (eventApiProductsByDomainId.get(eventApiProduct.getApplicationDomainId()) == null) {
		        		eventApiProductsByDomainId.put(eventApiProduct.getApplicationDomainId(), new LinkedHashSet<>());
		        	}
		        	eventApiProductsByDomainId.get(eventApiProduct.getApplicationDomainId()).add(eventApiProduct);
		        }
	        } while (eventApiProductsReponse.getMeta().getPagination().getNextPage() != null);
	        String log = String.format("loadEventApiProducts() loaded %d Event API Products, and took %s",
	        		eventApiProductsById.size(), TimeUtils.formatDeltaMs(start));
//	        System.out.println(log);
	        logger.info(log);
	        if (loadVersionsToo) return loadEventApiProductVersions();
	        else return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load Event API Products: " + e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load Event API Products: " + e.toString());
    		return false;
    	}
    }
    
    public boolean loadEventApiProductVersions() {
        ApiClient apiClient = getApiClient();
    	try {
	    	long start = System.currentTimeMillis();
	    	eventApiProductsById = new LinkedHashMap<>();
	    	eventApiProductsByDomainId = new LinkedHashMap<>();
	    	EventApiProductsApi eventApiProductsApi = new EventApiProductsApi(apiClient);
	        int page = 1;
	        EventApiProductVersionsResponse eventApiProductVersionsResponse;
	        do {
	        	eventApiProductVersionsResponse = eventApiProductsApi.getEventApiProductVersions(PAGE_SIZE, page++, null, null, null, null, null, null, null, null, null);
	        	for (EventApiProductVersion eventApiProductVersion : eventApiProductVersionsResponse.getData()) {
//	        		System.out.println(eventApiProductVersion);
	        		eventApiProductVersionsById.put(eventApiProductVersion.getId(), eventApiProductVersion);
	        		if (eventApiProductVersionsByEventApiProductId.get(eventApiProductVersion.getEventApiProductId()) == null) {
	        			eventApiProductVersionsByEventApiProductId.put(eventApiProductVersion.getEventApiProductId(), new LinkedHashSet<>());
	        		}
	        		eventApiProductVersionsByEventApiProductId.get(eventApiProductVersion.getEventApiProductId()).add(eventApiProductVersion);
	        	}
	        } while (eventApiProductVersionsResponse.getMeta().getPagination().getNextPage() != null);
	        String log = String.format("loadEventApiProductVersions() loaded %d versions, and took %s",
	        		eventApiProductVersionsById.size(), TimeUtils.formatDeltaMs(start));
//	        System.out.println(log);
	        logger.info(log);
    		return true;
    	} catch (ApiException e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load Event API Product Versions: " + e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add("Couldn't load Event API Product Versions: " + e.toString());
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
	        String log = String.format("loadStates() loaded %d states, and took %s",
	        		statesById.size(), TimeUtils.formatDeltaMs(start));
//	        System.out.println(log);
	        logger.info(log);
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
	        } while (consumersReponse.getMeta().getPagination().getNextPage() != null);
	        String log = String.format("loadConsumers() loaded %d Consumers, and took %s",
	        		consumersById.size(), TimeUtils.formatDeltaMs(start));
//	        System.out.println(log);
	        logger.info(log);
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
	        	eventMeshesReponse = eventMeshesApi.getEventMeshes(PAGE_SIZE, page++, null, null, null);
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
	        } while (eventMeshesReponse.getMeta().getPagination().getNextPage() != null);
	        String log = String.format("loadEventMeshes() loaded %d Event Meshes, and took %s",
	        		eventMeshesById.size(), TimeUtils.formatDeltaMs(start));
//	        System.out.println(log);
	        logger.info(log);
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
	        } while (topicDomainsReponse.getMeta().getPagination().getNextPage() != null);
	        String log = String.format("loadTopicDomains() loaded %d Topic Domains, and took %s",
	        		topicDomainsByDomainId.size(), TimeUtils.formatDeltaMs(start));
//	        System.out.println(log);
	        logger.info(log);
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
	        	environmentsReponse = environmentsApi.getEnvironments(PAGE_SIZE, page++, null, null, null);
		        for (Environment environment : environmentsReponse.getData()) {
		        	environmentsById.put(environment.getId(), environment);
		        }
	        } while (environmentsReponse.getMeta().getPagination().getNextPage() != null);
	        String log = String.format("loadEnvironments() loaded %d Environments, and took %s",
	        		environmentsById.size(), TimeUtils.formatDeltaMs(start));
//	        System.out.println(log);
	        logger.info(log);
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
//            System.out.printf("fetchUsers() took %s for HTTP call.%n", TimeUtils.formatDeltaMs(start));
            start = System.currentTimeMillis();
			if (response.code() == 200) {
	            char[] buffer = new char[8 * 1024];
	            StringBuilder sb = new StringBuilder();
                int charsRead;
                while ((charsRead = response.body().charStream().read(buffer)) != -1) {
                	sb.append(buffer, 0, charsRead);
                }
                JsonObject jo = new Gson().fromJson(sb.toString(), JsonObject.class);
                String log = String.format("fetchUsers() took %s to deserialize into JSON object.", TimeUtils.formatDeltaMs(start));
//    	        System.out.println(log);
    	        logger.info(log);
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
        System.out.printf("loadUsersCustom() loaded %d Users, and took %s%n", userNamesById.size(), TimeUtils.formatDeltaMs(start));
//        System.out.println(userNamesById.keySet());
    	return true;
    }
    
    
    public boolean loadUsers() {
        community.solace.plat.client.ApiClient apiClient = getPlatApiClient();
    	try {
	    	long start = System.currentTimeMillis();
	    	userNamesById = new LinkedHashMap<>();
	    	UsersApi usersApi = new UsersApi(apiClient);
	        UsersResponseEnvelope response;
	        UserRequest req = new UserRequest();  // what's the point of this?
	        int page = 1;
	        do {
	        	response = usersApi.getUsers(req, page++, PAGE_SIZE);
	        	for (UserResponse user : response.getData()) {
	        		String name = user.getFirstName() + " " + user.getLastName();
	        		if (name.equals("null null")) {
	        			name = user.getEmail();
	        		}
	        		userNamesById.put(user.getId(), name);
//	        		System.out.println(name);
	        	}
	        } while (((Map<?, ?>)response.getMeta().get("pagination")).get("nextPage") != null);
	        String log = String.format("loadUsers() loaded %d users, and took %s",
	        		userNamesById.size(), TimeUtils.formatDeltaMs(start));
//	        System.out.println(log);
	        logger.info(log);
    		return true;
    	} catch (community.solace.plat.client.ApiException e) {
    		e.printStackTrace();
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.getResponseBody());
    		return false;
    	} catch (Throwable e) {
    		e.printStackTrace();
            loadStatus = LoadStatus.ERROR;
            loadErrorStrings.add(e.toString());
    		return false;
    	}
    	
    	
    	
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
	public SupportedObjectType getEventPortalObjectType(String id) {
		if (loadStatus != LoadStatus.LOADED) return null;
		if (domains.containsKey(id)) return SupportedObjectType.DOMAIN;
		if (applicationsById.containsKey(id)) return SupportedObjectType.APPLICATION;
		if (applicationVersionsById.containsKey(id)) return SupportedObjectType.APPLICATION_VERSION;
		if (eventsById.containsKey(id)) return SupportedObjectType.EVENT;
		if (eventApiVersionsById.containsKey(id)) return SupportedObjectType.EVENT_VERSION;
		if (schemasById.containsKey(id)) return SupportedObjectType.SCHEMA;
		if (schemaVersionsById.containsKey(id)) return SupportedObjectType.SCHEMA_VERSION;
		if (eventApisById.containsKey(id)) return SupportedObjectType.EVENT_API;
		if (eventVersionsById.containsKey(id)) return SupportedObjectType.EVENT_API_VERSION;
		return SupportedObjectType.N_A;
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
			String s = api.getAsyncApiForApplicationVersion(appVerId, "json", null, null, "2.5.0", null, null, null, null, null);
//			if (prettyPrint) {
//				String json = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(s);
//				return json;
//			} else {
//				String json = new Gson().toJson(s);
//				return json;
//			}
			return s;
		} catch (ApiException e) {
			System.err.println(e.getResponseBody());
			return null;
		}
	}



	public String getAsyncApiForEventApiVerId(String eventApiVerId, boolean prettyPrint) {
		ApiClient apiClient = getApiClient();
		EventApisApi api = new EventApisApi(apiClient);
//		api.getAsyncApiForApplicationVersion(appVer.getId(), "json", "2.0.0");
		try {
			String s = api.getAsyncApiForEventApiVersion(eventApiVerId, null, "json", null, "2.5.0", null, null, null);
			return s;
//			api.getAsyncApiForEv
//			Object treeMap = api.getAsyncApiForEventApiVersion(eventApiVerId, null, null, null, null, null, null, null);
//			if (prettyPrint) {
//				String json = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(treeMap);
//				return json;
//			} else {
//				String json = new Gson().toJson(treeMap);
//				return json;
//			}
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
