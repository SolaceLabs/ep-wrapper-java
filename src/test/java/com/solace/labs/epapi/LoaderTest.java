package com.solace.labs.epapi;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.junit.Test;

import com.solace.labs.epapi.client.ApiException;
import com.solace.labs.epapi.client.model.Address;
import com.solace.labs.epapi.client.model.AddressLevel;
import com.solace.labs.epapi.client.model.Application;
import com.solace.labs.epapi.client.model.ApplicationDomain;
import com.solace.labs.epapi.client.model.ApplicationVersion;
import com.solace.labs.epapi.client.model.DeliveryDescriptor;
import com.solace.labs.epapi.client.model.Event;
import com.solace.labs.epapi.client.model.EventVersion;
import com.solace.labs.epapi.client.model.SchemaObject;
import com.solace.labs.epapi.client.model.SchemaVersion;
import com.solace.labs.epapi.wrapper.EventPortalClient;

public class LoaderTest {



    


    private static final PrintStream o = System.out;

    
    public static void calcExternalEvents() {
    	
    	for (ApplicationDomain domain : EventPortalClient.INSTANCE.getDomains()) {
    		System.out.println(domain.getName().toUpperCase());
    		o.println("---------------------------------");
    		Set<String> allProducedEventsVersions = new HashSet<>();
    		Set<String> allConsumedEventsVersions = new HashSet<>();
    		
    		if (EventPortalClient.INSTANCE.getApplicationsForDomainId(domain.getId()).isEmpty()) continue;  // nothing to see here
    		for (Application app : EventPortalClient.INSTANCE.getApplicationsForDomainId(domain.getId())) {
    			if (EventPortalClient.INSTANCE.getApplicationVersionsForApplicationId(app.getId()).isEmpty()) continue;
    			for (ApplicationVersion appVer : EventPortalClient.INSTANCE.getApplicationVersionsForApplicationId(app.getId())) {
    				
    				for (String eventVerId : appVer.getDeclaredProducedEventVersionIds()) {
    					allProducedEventsVersions.add(eventVerId);
    					EventVersion ev = EventPortalClient.INSTANCE.getEventVersion(eventVerId);
    					Event event = EventPortalClient.INSTANCE.getEvent(ev.getEventId());
    					ApplicationDomain origDomain = EventPortalClient.INSTANCE.getDomain(event.getApplicationDomainId());
    					System.out.printf("%-50s%-30s%-20s%-30s%n", app.getName(), event.getName(), "pub: "+ev.getVersion(), origDomain.getName());
    				}
    				for (String eventVerId : appVer.getDeclaredConsumedEventVersionIds()) {
    					EventVersion ev = EventPortalClient.INSTANCE.getEventVersion(eventVerId);
    					Event event = EventPortalClient.INSTANCE.getEvent(ev.getEventId());
    					ApplicationDomain origDomain = EventPortalClient.INSTANCE.getDomain(event.getApplicationDomainId());
    					System.out.printf("%-50s%-30s%-20s%-30s%n", app.getName(), event.getName(), "sub: "+ev.getVersion(), origDomain.getName());
    					allConsumedEventsVersions.add(eventVerId);
    				}
    			}
    		}
    		System.out.println(allProducedEventsVersions.size());
    		System.out.println(allConsumedEventsVersions.size());
    		System.out.println(domain.getStats().getEventCount());
    		System.out.println();
    	}
    }
    
    
    
	/*
	 *     createdTime: null
updatedTime: null
createdBy: null
changedBy: null
brokerType: solace
address: class Address {
createdTime: null
updatedTime: null
createdBy: null
changedBy: null
addressLevels: [class AddressLevel {
name: catalyst-labs
addressLevelType: literal
enumVersionId: null
}, class AddressLevel {
name: acme-retail
addressLevelType: literal
enumVersionId: null
}, class AddressLevel {
name: till-system
addressLevelType: literal
enumVersionId: null
}, class AddressLevel {
name: v1
addressLevelType: literal
enumVersionId: null
}, class AddressLevel {
name: storeId
addressLevelType: variable
enumVersionId: null
}, class AddressLevel {
name: receipt
addressLevelType: literal
enumVersionId: null
}]
addressType: topic
id: null
type: null
}
keySchemaVersionId: null
keySchemaPrimitiveType: null
	 */

    private static String buildTopic(DeliveryDescriptor dd) {
    	char levelSeparator = '/';  // default
    	if ("kafka".equals(dd.getBrokerType())) levelSeparator = '.';  // could be null
    	StringBuilder sb = new StringBuilder();
    	Address a = dd.getAddress();
    	if (a == null) return "";  // no address yet defined
    	for (AddressLevel level : a.getAddressLevels()) {
    		if (level.getAddressLevelType() == AddressLevel.AddressLevelTypeEnum.LITERAL) {
        		sb.append(level.getName());
    		} else {
        		sb.append("$").append(level.getName());
    		}
    		sb.append(levelSeparator);
    	}
    	return sb.deleteCharAt(sb.length()-1).toString();
    }
    
    
//    private static void printEventInfo(String eventVerId, String pubOrSub) {
//		EventVersion ev = Loader.INSTANCE.getEventVersionById(eventVerId);
//		Event event = Loader.INSTANCE.getEvent(ev.getEventId());
//		ApplicationDomain origDomain = Loader.INSTANCE.getDomain(event.getApplicationDomainId());
//		// is there a schema for this Event?
//		if (Loader.INSTANCE.getSchemaVersion(ev.getSchemaVersionId()) != null) {
//			SchemaVersion schemaVersion = Loader.INSTANCE.getSchemaVersion(ev.getSchemaVersionId());
//			SchemaObject schema = Loader.INSTANCE.getSchema(schemaVersion.getSchemaId());
//			o.printf(" |    |    |-> %s EVENT: '%s'%s, state=%s, broker=%s, topic='%s', sch name='%s', content=%s, schVer ver=%s  [%s]  [%s]%n", event.getName(), origDomain.getId().equals(domain.getId()) ? "" : " (EXT)", Loader.INSTANCE.getState(ev.getStateId()).getName(), ev.getDeliveryDescriptor().getBrokerType(), buildTopic(ev.getDeliveryDescriptor()), schema.getName(), schema.getContentType(), schemaVersion.getVersion(), eventVerId, schemaVersion.getId());
//		} else {
//			o.printf(" |    |    |-> %s EVENT: '%s'%s, state=%s, broker=%s, topic='%s', schema=not defined  [%s]%n", event.getName(), origDomain.getId().equals(domain.getId()) ? "" : " (EXT)", Loader.INSTANCE.getState(ev.getStateId()).getName(), ev.getDeliveryDescriptor().getBrokerType(), buildTopic(ev.getDeliveryDescriptor()), eventVerId);
//		}
//    }
//    
    
    public static void drawAppsTable() {
    	for (ApplicationDomain domain : EventPortalClient.INSTANCE.getDomains()) {  // for all domains...
    		o.printf("DOMAIN '%s': # apps=%d, # events=%d%n", domain.getName(), domain.getStats().getApplicationCount(), domain.getStats().getEventCount());
    		if (EventPortalClient.INSTANCE.getApplicationsForDomainId(domain.getId()).isEmpty()) {  // no apps in this domain
				o.printf(" |-> no apps defined%n |%n");
    			continue;
    		}
    		for (Application app : EventPortalClient.INSTANCE.getApplicationsForDomainId(domain.getId())) {  // otherwise, for all apps...
    			o.printf(" |-> APP '%s': type=%s, # vers=%d  [%s]%n", app.getName(), app.getApplicationType(), app.getNumberOfVersions(), app.getId());
    			if (EventPortalClient.INSTANCE.getApplicationVersionsForApplicationId(app.getId()).isEmpty()) {  // if this app has no versions of it defined
    				o.printf(" |    |-> no versions defined%n");
    				continue;
    			}
    			for (ApplicationVersion appVer : EventPortalClient.INSTANCE.getApplicationVersionsForApplicationId(app.getId())) {
    				o.printf(" |    |-> ver=%s, state=%s, # pub=%d, # sub=%d  [%s]%n",
    						appVer.getVersion(),
    						EventPortalClient.INSTANCE.getState(appVer.getStateId()).getName(),
    						appVer.getDeclaredProducedEventVersionIds().size(),
    						appVer.getDeclaredConsumedEventVersionIds().size(),
    						appVer.getId());
    				for (String eventVerId : appVer.getDeclaredProducedEventVersionIds()) {  // for all EventVersions this AppVer produces...
    					EventVersion ev = EventPortalClient.INSTANCE.getEventVersion(eventVerId);
    					Event event = EventPortalClient.INSTANCE.getEvent(ev.getEventId());
    					ApplicationDomain origDomain = EventPortalClient.INSTANCE.getDomain(event.getApplicationDomainId());
    					// is there a schema for this Event?
    					if (EventPortalClient.INSTANCE.getSchemaVersion(ev.getSchemaVersionId()) != null) {
        					SchemaVersion schemaVersion = EventPortalClient.INSTANCE.getSchemaVersion(ev.getSchemaVersionId());
        					SchemaObject schema = EventPortalClient.INSTANCE.getSchema(schemaVersion.getSchemaId());
        					o.printf(" |    |    |-> PUB EVENT: '%s'%s, v%s, state=%s, broker=%s, topic='%s', schema='%s', type=%s, schema v%s  [%s]  [%s]%n",
        							event.getName(),
        							origDomain.getId().equals(domain.getId()) ? "" : " (EXT)",
        							ev.getVersion(),
									EventPortalClient.INSTANCE.getState(ev.getStateId()).getName(),
									ev.getDeliveryDescriptor().getBrokerType(),
									buildTopic(ev.getDeliveryDescriptor()),
									schema.getName(),
									schema.getSchemaType(),
									schemaVersion.getVersion(),
									eventVerId,
									schemaVersion.getId());
    					} else {
        					o.printf(" |    |    |-> PUB EVENT: '%s'%s, v%s, state=%s, broker=%s, topic='%s', schema=not defined  [%s]%n",
        							event.getName(),
        							origDomain.getId().equals(domain.getId()) ? "" : " (EXT)",
									ev.getVersion(),
									EventPortalClient.INSTANCE.getState(ev.getStateId()).getName(),
									ev.getDeliveryDescriptor().getBrokerType(),
									buildTopic(ev.getDeliveryDescriptor()),
									eventVerId);
    					}
    				}
    				for (String eventVerId : appVer.getDeclaredConsumedEventVersionIds()) {
    					EventVersion ev = EventPortalClient.INSTANCE.getEventVersion(eventVerId);
    					Event event = EventPortalClient.INSTANCE.getEvent(ev.getEventId());
    					ApplicationDomain origDomain = EventPortalClient.INSTANCE.getDomain(event.getApplicationDomainId());
    					// is there a schema for this Event?
    					if (EventPortalClient.INSTANCE.getSchemaVersion(ev.getSchemaVersionId()) != null) {
        					SchemaVersion schemaVersion = EventPortalClient.INSTANCE.getSchemaVersion(ev.getSchemaVersionId());
        					SchemaObject schema = EventPortalClient.INSTANCE.getSchema(schemaVersion.getSchemaId());
        					o.printf(" |    |    |-> SUB EVENT: '%s'%s, v%s, state=%s, broker=%s, topic='%s', schema='%s', type=%s, schema v%s  [%s]  [%s]%n",
        							event.getName(),
        							origDomain.getId().equals(domain.getId()) ? "" : " (EXT)",
        							ev.getVersion(),
									EventPortalClient.INSTANCE.getState(ev.getStateId()).getName(),
									ev.getDeliveryDescriptor().getBrokerType(),
									buildTopic(ev.getDeliveryDescriptor()),
									schema.getName(),
									schema.getSchemaType(),
									schemaVersion.getVersion(),
									eventVerId,
									schemaVersion.getId());
    					} else {
        					o.printf(" |    |    |-> SUB EVENT: '%s'%s, v%s, state=%s, broker=%s, topic='%s', schema=not defined  [%s]%n",
        							event.getName(),
        							origDomain.getId().equals(domain.getId()) ? "" : " (EXT)",
									ev.getVersion(),
									EventPortalClient.INSTANCE.getState(ev.getStateId()).getName(),
									ev.getDeliveryDescriptor().getBrokerType(),
									buildTopic(ev.getDeliveryDescriptor()),
									eventVerId);
    					}
    				}
    			}
    		}
    		System.out.println(" |");
    	}
    }
    
    
    public static MutableTreeNode drawAppsTree() {
    	DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    	for (ApplicationDomain domain : EventPortalClient.INSTANCE.getDomains()) {  // for all domains...
    		DefaultMutableTreeNode domainNode = new DefaultMutableTreeNode(domain);
    		root.add(domainNode);
//    		o.printf("DOMAIN '%s': # apps=%d, # events=%d%n", domain.getName(), domain.getStats().getApplicationCount(), domain.getStats().getEventCount());
    		for (Application app : EventPortalClient.INSTANCE.getApplicationsForDomainId(domain.getId())) {  // otherwise, for all apps...
        		DefaultMutableTreeNode applicationNode = new DefaultMutableTreeNode(app);
        		domainNode.add(applicationNode);
//    			o.printf(" |-> APP '%s': type=%s, # vers=%d  [%s]%n", app.getName(), app.getApplicationType(), app.getNumberOfVersions(), app.getId());
    			for (ApplicationVersion appVer : EventPortalClient.INSTANCE.getApplicationVersionsForApplicationId(app.getId())) {
    	    		DefaultMutableTreeNode appVerNode = new DefaultMutableTreeNode(appVer);
    	    		applicationNode.add(appVerNode);
//    				o.printf(" |    |-> ver=%s, state=%s, # pub=%d, # sub=%d  [%s]%n",
//    						appVer.getVersion(),
//    						EventPortalClient.INSTANCE.getState(appVer.getStateId()).getName(),
//    						appVer.getDeclaredProducedEventVersionIds().size(),
//    						appVer.getDeclaredConsumedEventVersionIds().size(),
//    						appVer.getId());
    				for (String eventVerId : appVer.getDeclaredProducedEventVersionIds()) {  // for all EventVersions this AppVer produces...
    					EventVersion ev = EventPortalClient.INSTANCE.getEventVersion(eventVerId);
    					Event event = EventPortalClient.INSTANCE.getEvent(ev.getEventId());
    		    		DefaultMutableTreeNode eventNode = new DefaultMutableTreeNode(event);
    		    		appVerNode.add(eventNode);
    		    		DefaultMutableTreeNode eventVerNode = new DefaultMutableTreeNode(ev);
    		    		eventNode.add(eventVerNode);
//    					ApplicationDomain origDomain = EventPortalClient.INSTANCE.getDomain(event.getApplicationDomainId());
    					// is there a schema for this Event?
    					if (EventPortalClient.INSTANCE.getSchemaVersion(ev.getSchemaVersionId()) != null) {
        					SchemaVersion schemaVersion = EventPortalClient.INSTANCE.getSchemaVersion(ev.getSchemaVersionId());
        					SchemaObject schema = EventPortalClient.INSTANCE.getSchema(schemaVersion.getSchemaId());
        		    		DefaultMutableTreeNode schemaNode = new DefaultMutableTreeNode(schema);
        		    		eventVerNode.add(schemaNode);
        		    		DefaultMutableTreeNode schemaVerNode = new DefaultMutableTreeNode(schemaVersion);
        		    		schemaNode.add(schemaVerNode);
//        					o.printf(" |    |    |-> PUB EVENT: '%s'%s, v%s, state=%s, broker=%s, topic='%s', schema='%s', type=%s, schema v%s  [%s]  [%s]%n",
//        							event.getName(),
//        							origDomain.getId().equals(domain.getId()) ? "" : " (EXT)",
//        							ev.getVersion(),
//									EventPortalClient.INSTANCE.getState(ev.getStateId()).getName(),
//									ev.getDeliveryDescriptor().getBrokerType(),
//									buildTopic(ev.getDeliveryDescriptor()),
//									schema.getName(),
//									schema.getSchemaType(),
//									schemaVersion.getVersion(),
//									eventVerId,
//									schemaVersion.getId());
    					}
    				}
    				for (String eventVerId : appVer.getDeclaredConsumedEventVersionIds()) {
    					EventVersion ev = EventPortalClient.INSTANCE.getEventVersion(eventVerId);
    					Event event = EventPortalClient.INSTANCE.getEvent(ev.getEventId());
    		    		DefaultMutableTreeNode eventNode = new DefaultMutableTreeNode(event);
    		    		appVerNode.add(eventNode);
    		    		DefaultMutableTreeNode eventVerNode = new DefaultMutableTreeNode(ev);
    		    		eventNode.add(eventVerNode);
//    					ApplicationDomain origDomain = EventPortalClient.INSTANCE.getDomain(event.getApplicationDomainId());
    					// is there a schema for this Event?
    					if (EventPortalClient.INSTANCE.getSchemaVersion(ev.getSchemaVersionId()) != null) {
        					SchemaVersion schemaVersion = EventPortalClient.INSTANCE.getSchemaVersion(ev.getSchemaVersionId());
        					SchemaObject schema = EventPortalClient.INSTANCE.getSchema(schemaVersion.getSchemaId());
        		    		DefaultMutableTreeNode schemaNode = new DefaultMutableTreeNode(schema);
        		    		eventVerNode.add(schemaNode);
        		    		DefaultMutableTreeNode schemaVerNode = new DefaultMutableTreeNode(schemaVersion);
        		    		schemaNode.add(schemaVerNode);
    					}
    				}
    			}
    		}
    	}
    	return root;
    }
    

    
    public static void drawEventsTable() {
    	for (ApplicationDomain domain : EventPortalClient.INSTANCE.getDomains()) {  // for all domains...
    		o.printf("DOMAIN '%s': # apps=%d, # events=%d%n",
    				domain.getName(),
    				domain.getStats().getApplicationCount(),
    				domain.getStats().getEventCount());
    		if (EventPortalClient.INSTANCE.getEventsForDomainId(domain.getId()).isEmpty()) {  // no events in this domain
				o.printf(" |-> no events defined%n |%n");
    			continue;
    		}
    		for (Event event : EventPortalClient.INSTANCE.getEventsForDomainId(domain.getId())) {  // otherwise, for all events...
    			o.printf(" |-> EVENT '%s': # vers=%d  [%s]%n", event.getName(), event.getNumberOfVersions(), event.getId());
    			if (EventPortalClient.INSTANCE.getEventVersionsForEventId(event.getId()).isEmpty()) {  // if this event has no versions of it defined
    				o.printf(" |    |-> no versions defined%n");
    				continue;
    			}
    			for (EventVersion ev : EventPortalClient.INSTANCE.getEventVersionsForEventId(event.getId())) {
					if (EventPortalClient.INSTANCE.getSchemaVersion(ev.getSchemaVersionId()) != null) {
    					SchemaVersion schemaVersion = EventPortalClient.INSTANCE.getSchemaVersion(ev.getSchemaVersionId());
    					SchemaObject schema = EventPortalClient.INSTANCE.getSchema(schemaVersion.getSchemaId());
    					o.printf(" |    |-> v%s, state=%s, broker=%s, topic='%s', sch name='%s', type=%s, schema v%s  [%s]  [%s]%n",
    							ev.getVersion(),
								EventPortalClient.INSTANCE.getState(ev.getStateId()).getName(),
								ev.getDeliveryDescriptor().getBrokerType(),
								buildTopic(ev.getDeliveryDescriptor()),
								schema.getName(),
								schema.getSchemaType(),
								schemaVersion.getVersion(),
								event.getId(),
								schemaVersion.getId());
					} else {
    					o.printf(" |    |-> v%s, state=%s, broker=%s, topic='%s', schema=not defined  [%s]%n",
								ev.getVersion(),
								EventPortalClient.INSTANCE.getState(ev.getStateId()).getName(),
								ev.getDeliveryDescriptor().getBrokerType(),
								buildTopic(ev.getDeliveryDescriptor()),
								event.getId());
					}
    			}
    		}
    		System.out.println(" |");
    	}
    }

    public static void drawSchemasTable() {
    	for (ApplicationDomain domain : EventPortalClient.INSTANCE.getDomains()) {  // for all domains...
    		o.printf("DOMAIN '%s': # schemas=%d%n",
    				domain.getName(),
    				domain.getStats().getSchemaCount());
    		if (EventPortalClient.INSTANCE.getSchemasForDomainId(domain.getId()).isEmpty()) {  // no schemas in this domain
				o.printf(" |-> no schemas defined%n |%n");
    			continue;
    		}
    		for (SchemaObject schema : EventPortalClient.INSTANCE.getSchemasForDomainId(domain.getId())) {  // otherwise, for all schemas...
    			o.printf(" |-> SCHEMA '%s':  type=%s, content=%s, # vers=%d, # events using=%d  [%s]%n",
    					schema.getName(),
    					schema.getSchemaType(),
    					schema.getContentType().toUpperCase(),
    					schema.getNumberOfVersions(),
    					schema.getEventVersionRefCount(),
    					schema.getId());
    			
    			if (EventPortalClient.INSTANCE.getSchemaVersionsForSchemaId(schema.getId()).isEmpty()) {  // if this schema has no versions of it defined
    				o.printf(" |    |-> no versions defined%n");
    				continue;
    			}
    			for (SchemaVersion schemaVersion : EventPortalClient.INSTANCE.getSchemaVersionsForSchemaId(schema.getId())) {
					o.printf(" |    |-> v%s, state=%s, length=%d  [%s]%n",
							schemaVersion.getVersion(),
							EventPortalClient.INSTANCE.getState(schemaVersion.getStateId()).getName(),
							schemaVersion.getContent().length(),
							schemaVersion.getId());
    			}
    		}
    		System.out.println(" |");
    	}
    }

    
    
    

    @Test
    public void testLoader() throws ApiException, IOException {


        

    	EventPortalClient.INSTANCE.setToken("eyJhbGciOiJSUzI1NiIsImtpZCI6Im1hYXNfcHJvZF8yMDIwMDMyNiIsInR5cCI6IkpXVCJ9.eyJvcmciOiJzb2xhY2VjdG8iLCJvcmdUeXBlIjoiRU5URVJQUklTRSIsInN1YiI6IjY3dHI4dGt0eHgiLCJwZXJtaXNzaW9ucyI6IkFBQUFBQUFBQUFBQVF3QUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFJQUFBTFA3L2c1Y2ZCUm9BQU1BQUNRPT0iLCJhcGlUb2tlbklkIjoiOTI3Y3Z1MzQ2M3IiLCJpc3MiOiJTb2xhY2UgQ29ycG9yYXRpb24iLCJpYXQiOjE2NTg0NTE0ODh9.kJ3ppLqldtYz8Y3VLSqD6D0-ZyWM5f_nmRDjKUdxQYvenzKFkHS_8JI_vSPSnHVHPdfoOR6eT6QAjurjzzqkwgWnH0E3zae4MJSi1rp2PbPkBXOjv1wOvzyc4_i-Da23tY1cS3oobI6bc4LqQsKtXPClR9SSG_CEA3DiHFvd1UYwly8TS7ZKpyo5y1eygYWhRKaHYJUa2T1Pxxv9NcCuP5Gu_uTB1O7zV19v6mtvKZUzVSo1YCk7QMwrm7OgNLulQ5h2R-AD5Rq82YhRVhphldaduVql9_6EXGzv-SjQkKb5D_xUHYDzZdDuMbYk4aM2k_IAJlrsrGXd2rpancjUsw");
    	EventPortalClient.INSTANCE.load();
    	
        o.println("#####################################");

        calcExternalEvents();
        o.println("#####################################");
        drawAppsTable();
        o.println("#####################################");
        drawEventsTable();
        o.println("#####################################");
        drawSchemasTable();

        
        o.println("#####################################");
        MutableTreeNode node = drawAppsTree();


    }
}


