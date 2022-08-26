package community.solace.ep;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.junit.BeforeClass;
import org.junit.Test;

import community.solace.ep.client.ApiException;
import community.solace.ep.client.model.Address;
import community.solace.ep.client.model.AddressLevel;
import community.solace.ep.client.model.Application;
import community.solace.ep.client.model.ApplicationDomain;
import community.solace.ep.client.model.ApplicationVersion;
import community.solace.ep.client.model.DeliveryDescriptor;
import community.solace.ep.client.model.Event;
import community.solace.ep.client.model.EventVersion;
import community.solace.ep.client.model.SchemaObject;
import community.solace.ep.client.model.SchemaVersion;
import community.solace.ep.wrapper.EventPortalWrapper;

public class LoaderTest {



    


    private static final PrintStream o = System.out;

    
    public static void calcExternalEvents() {
    	
    	for (ApplicationDomain domain : EventPortalWrapper.INSTANCE.getDomains()) {
    		System.out.println(domain.getName().toUpperCase());
    		o.println("---------------------------------");
    		Set<String> allProducedEventsVersions = new HashSet<>();
    		Set<String> allConsumedEventsVersions = new HashSet<>();
    		
    		if (EventPortalWrapper.INSTANCE.getApplicationsForDomainId(domain.getId()).isEmpty()) continue;  // nothing to see here
    		for (Application app : EventPortalWrapper.INSTANCE.getApplicationsForDomainId(domain.getId())) {
    			if (EventPortalWrapper.INSTANCE.getApplicationVersionsForApplicationId(app.getId()).isEmpty()) continue;
    			for (ApplicationVersion appVer : EventPortalWrapper.INSTANCE.getApplicationVersionsForApplicationId(app.getId())) {
    				
    				for (String eventVerId : appVer.getDeclaredProducedEventVersionIds()) {
    					allProducedEventsVersions.add(eventVerId);
    					EventVersion ev = EventPortalWrapper.INSTANCE.getEventVersion(eventVerId);
    					Event event = EventPortalWrapper.INSTANCE.getEvent(ev.getEventId());
    					ApplicationDomain origDomain = EventPortalWrapper.INSTANCE.getDomain(event.getApplicationDomainId());
    					System.out.printf("%-50s%-30s%-20s%-30s%n", app.getName(), event.getName(), "pub: "+ev.getVersion(), origDomain.getName());
    				}
    				for (String eventVerId : appVer.getDeclaredConsumedEventVersionIds()) {
    					EventVersion ev = EventPortalWrapper.INSTANCE.getEventVersion(eventVerId);
    					Event event = EventPortalWrapper.INSTANCE.getEvent(ev.getEventId());
    					ApplicationDomain origDomain = EventPortalWrapper.INSTANCE.getDomain(event.getApplicationDomainId());
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
    	for (ApplicationDomain domain : EventPortalWrapper.INSTANCE.getDomains()) {  // for all domains...
    		o.printf("DOMAIN '%s': # apps=%d, # events=%d%n", domain.getName(), domain.getStats().getApplicationCount(), domain.getStats().getEventCount());
    		if (EventPortalWrapper.INSTANCE.getApplicationsForDomainId(domain.getId()).isEmpty()) {  // no apps in this domain
				o.printf(" |-> no apps defined%n |%n");
    			continue;
    		}
    		for (Application app : EventPortalWrapper.INSTANCE.getApplicationsForDomainId(domain.getId())) {  // otherwise, for all apps...
    			o.printf(" |-> APP '%s': type=%s, # vers=%d  [%s]%n", app.getName(), app.getApplicationType(), app.getNumberOfVersions(), app.getId());
    			if (EventPortalWrapper.INSTANCE.getApplicationVersionsForApplicationId(app.getId()).isEmpty()) {  // if this app has no versions of it defined
    				o.printf(" |    |-> no versions defined%n");
    				continue;
    			}
    			for (ApplicationVersion appVer : EventPortalWrapper.INSTANCE.getApplicationVersionsForApplicationId(app.getId())) {
    				o.printf(" |    |-> ver=%s (%s), state=%s, # pub=%d, # sub=%d  [%s]%n",
    						appVer.getVersion(),
    						appVer.getDisplayName(),
    						EventPortalWrapper.INSTANCE.getState(appVer.getStateId()).getName(),
    						appVer.getDeclaredProducedEventVersionIds().size(),
    						appVer.getDeclaredConsumedEventVersionIds().size(),
    						appVer.getId());
    				for (String eventVerId : appVer.getDeclaredProducedEventVersionIds()) {  // for all EventVersions this AppVer produces...
    					EventVersion ev = EventPortalWrapper.INSTANCE.getEventVersion(eventVerId);
    					Event event = EventPortalWrapper.INSTANCE.getEvent(ev.getEventId());
    					ApplicationDomain origDomain = EventPortalWrapper.INSTANCE.getDomain(event.getApplicationDomainId());
    					// is there a schema for this Event?
    					if (EventPortalWrapper.INSTANCE.getSchemaVersion(ev.getSchemaVersionId()) != null) {
        					SchemaVersion schemaVersion = EventPortalWrapper.INSTANCE.getSchemaVersion(ev.getSchemaVersionId());
        					SchemaObject schema = EventPortalWrapper.INSTANCE.getSchema(schemaVersion.getSchemaId());
        					o.printf(" |    |    |-> PUB EVENT: '%s'%s, v%s, state=%s, broker=%s, topic='%s', schema='%s', type=%s, schema v%s  [%s]  [%s]%n",
        							event.getName(),
        							origDomain.getId().equals(domain.getId()) ? "" : " (EXT)",
        							ev.getVersion(),
									EventPortalWrapper.INSTANCE.getState(ev.getStateId()).getName(),
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
									EventPortalWrapper.INSTANCE.getState(ev.getStateId()).getName(),
									ev.getDeliveryDescriptor().getBrokerType(),
									buildTopic(ev.getDeliveryDescriptor()),
									eventVerId);
    					}
    				}
    				for (String eventVerId : appVer.getDeclaredConsumedEventVersionIds()) {
    					EventVersion ev = EventPortalWrapper.INSTANCE.getEventVersion(eventVerId);
    					Event event = EventPortalWrapper.INSTANCE.getEvent(ev.getEventId());
    					ApplicationDomain origDomain = EventPortalWrapper.INSTANCE.getDomain(event.getApplicationDomainId());
    					// is there a schema for this Event?
    					if (EventPortalWrapper.INSTANCE.getSchemaVersion(ev.getSchemaVersionId()) != null) {
        					SchemaVersion schemaVersion = EventPortalWrapper.INSTANCE.getSchemaVersion(ev.getSchemaVersionId());
        					SchemaObject schema = EventPortalWrapper.INSTANCE.getSchema(schemaVersion.getSchemaId());
        					o.printf(" |    |    |-> SUB EVENT: '%s'%s, v%s, state=%s, broker=%s, topic='%s', schema='%s', type=%s, schema v%s  [%s]  [%s]%n",
        							event.getName(),
        							origDomain.getId().equals(domain.getId()) ? "" : " (EXT)",
        							ev.getVersion(),
									EventPortalWrapper.INSTANCE.getState(ev.getStateId()).getName(),
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
									EventPortalWrapper.INSTANCE.getState(ev.getStateId()).getName(),
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
    	for (ApplicationDomain domain : EventPortalWrapper.INSTANCE.getDomains()) {  // for all domains...
    		DefaultMutableTreeNode domainNode = new DefaultMutableTreeNode(domain);
    		root.add(domainNode);
//    		o.printf("DOMAIN '%s': # apps=%d, # events=%d%n", domain.getName(), domain.getStats().getApplicationCount(), domain.getStats().getEventCount());
    		for (Application app : EventPortalWrapper.INSTANCE.getApplicationsForDomainId(domain.getId())) {  // otherwise, for all apps...
        		DefaultMutableTreeNode applicationNode = new DefaultMutableTreeNode(app);
        		domainNode.add(applicationNode);
//    			o.printf(" |-> APP '%s': type=%s, # vers=%d  [%s]%n", app.getName(), app.getApplicationType(), app.getNumberOfVersions(), app.getId());
    			for (ApplicationVersion appVer : EventPortalWrapper.INSTANCE.getApplicationVersionsForApplicationId(app.getId())) {
    	    		DefaultMutableTreeNode appVerNode = new DefaultMutableTreeNode(appVer);
    	    		applicationNode.add(appVerNode);
//    				o.printf(" |    |-> ver=%s, state=%s, # pub=%d, # sub=%d  [%s]%n",
//    						appVer.getVersion(),
//    						EventPortalClient.INSTANCE.getState(appVer.getStateId()).getName(),
//    						appVer.getDeclaredProducedEventVersionIds().size(),
//    						appVer.getDeclaredConsumedEventVersionIds().size(),
//    						appVer.getId());
    				for (String eventVerId : appVer.getDeclaredProducedEventVersionIds()) {  // for all EventVersions this AppVer produces...
    					EventVersion ev = EventPortalWrapper.INSTANCE.getEventVersion(eventVerId);
    					Event event = EventPortalWrapper.INSTANCE.getEvent(ev.getEventId());
    		    		DefaultMutableTreeNode eventNode = new DefaultMutableTreeNode(event);
    		    		appVerNode.add(eventNode);
    		    		DefaultMutableTreeNode eventVerNode = new DefaultMutableTreeNode(ev);
    		    		eventNode.add(eventVerNode);
//    					ApplicationDomain origDomain = EventPortalClient.INSTANCE.getDomain(event.getApplicationDomainId());
    					// is there a schema for this Event?
    					if (EventPortalWrapper.INSTANCE.getSchemaVersion(ev.getSchemaVersionId()) != null) {
        					SchemaVersion schemaVersion = EventPortalWrapper.INSTANCE.getSchemaVersion(ev.getSchemaVersionId());
        					SchemaObject schema = EventPortalWrapper.INSTANCE.getSchema(schemaVersion.getSchemaId());
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
    					EventVersion ev = EventPortalWrapper.INSTANCE.getEventVersion(eventVerId);
    					Event event = EventPortalWrapper.INSTANCE.getEvent(ev.getEventId());
    		    		DefaultMutableTreeNode eventNode = new DefaultMutableTreeNode(event);
    		    		appVerNode.add(eventNode);
    		    		DefaultMutableTreeNode eventVerNode = new DefaultMutableTreeNode(ev);
    		    		eventNode.add(eventVerNode);
//    					ApplicationDomain origDomain = EventPortalClient.INSTANCE.getDomain(event.getApplicationDomainId());
    					// is there a schema for this Event?
    					if (EventPortalWrapper.INSTANCE.getSchemaVersion(ev.getSchemaVersionId()) != null) {
        					SchemaVersion schemaVersion = EventPortalWrapper.INSTANCE.getSchemaVersion(ev.getSchemaVersionId());
        					SchemaObject schema = EventPortalWrapper.INSTANCE.getSchema(schemaVersion.getSchemaId());
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
    	for (ApplicationDomain domain : EventPortalWrapper.INSTANCE.getDomains()) {  // for all domains...
    		o.printf("DOMAIN '%s': # apps=%d, # events=%d%n",
    				domain.getName(),
    				domain.getStats().getApplicationCount(),
    				domain.getStats().getEventCount());
    		if (EventPortalWrapper.INSTANCE.getEventsForDomainId(domain.getId()).isEmpty()) {  // no events in this domain
				o.printf(" |-> no events defined%n |%n");
    			continue;
    		}
    		for (Event event : EventPortalWrapper.INSTANCE.getEventsForDomainId(domain.getId())) {  // otherwise, for all events...
    			o.printf(" |-> EVENT '%s': # vers=%d  [%s]%n", event.getName(), event.getNumberOfVersions(), event.getId());
    			if (EventPortalWrapper.INSTANCE.getEventVersionsForEventId(event.getId()).isEmpty()) {  // if this event has no versions of it defined
    				o.printf(" |    |-> no versions defined%n");
    				continue;
    			}
    			for (EventVersion ev : EventPortalWrapper.INSTANCE.getEventVersionsForEventId(event.getId())) {
					if (EventPortalWrapper.INSTANCE.getSchemaVersion(ev.getSchemaVersionId()) != null) {
    					SchemaVersion schemaVersion = EventPortalWrapper.INSTANCE.getSchemaVersion(ev.getSchemaVersionId());
    					SchemaObject schema = EventPortalWrapper.INSTANCE.getSchema(schemaVersion.getSchemaId());
    					o.printf(" |    |-> v%s, state=%s, broker=%s, topic='%s', sch name='%s', type=%s, schema v%s  [%s]  [%s]%n",
    							ev.getVersion(),
								EventPortalWrapper.INSTANCE.getState(ev.getStateId()).getName(),
								ev.getDeliveryDescriptor().getBrokerType(),
								buildTopic(ev.getDeliveryDescriptor()),
								schema.getName(),
								schema.getSchemaType(),
								schemaVersion.getVersion(),
								ev.getId(),
								schemaVersion.getId());
					} else if (ev.getSchemaPrimitiveType() != null) {
						ev.getDeliveryDescriptor().getKeySchemaPrimitiveType();
						ev.getDeliveryDescriptor().getKeySchemaVersionId();
    					o.printf(" |    |-> v%s, state=%s, broker=%s, topic='%s', schema=%s  [%s]%n",
								ev.getVersion(),
								EventPortalWrapper.INSTANCE.getState(ev.getStateId()).getName(),
								ev.getDeliveryDescriptor().getBrokerType(),
								buildTopic(ev.getDeliveryDescriptor()),
								ev.getSchemaPrimitiveType(),
								ev.getId());
    					
					} else {
    					o.printf(" |    |-> v%s, state=%s, broker=%s, topic='%s', schema=not defined  [%s]%n",
								ev.getVersion(),
								EventPortalWrapper.INSTANCE.getState(ev.getStateId()).getName(),
								ev.getDeliveryDescriptor().getBrokerType(),
								buildTopic(ev.getDeliveryDescriptor()),
								ev.getId());
					}
    			}
    		}
    		System.out.println(" |");
    	}
    }

    public static void drawSchemasTable() {
    	for (ApplicationDomain domain : EventPortalWrapper.INSTANCE.getDomains()) {  // for all domains...
    		o.printf("DOMAIN '%s': # schemas=%d%n",
    				domain.getName(),
    				domain.getStats().getSchemaCount());
    		if (EventPortalWrapper.INSTANCE.getSchemasForDomainId(domain.getId()).isEmpty()) {  // no schemas in this domain
				o.printf(" |-> no schemas defined%n |%n");
    			continue;
    		}
    		for (SchemaObject schema : EventPortalWrapper.INSTANCE.getSchemasForDomainId(domain.getId())) {  // otherwise, for all schemas...
    			o.printf(" |-> SCHEMA '%s':  type=%s, content=%s, # vers=%d, # events using=%d  [%s]%n",
    					schema.getName(),
    					schema.getSchemaType(),
    					schema.getContentType().toUpperCase(),
    					schema.getNumberOfVersions(),
    					schema.getEventVersionRefCount(),
    					schema.getId());
    			
    			if (EventPortalWrapper.INSTANCE.getSchemaVersionsForSchemaId(schema.getId()).isEmpty()) {  // if this schema has no versions of it defined
    				o.printf(" |    |-> no versions defined%n");
    				continue;
    			}
    			for (SchemaVersion schemaVersion : EventPortalWrapper.INSTANCE.getSchemaVersionsForSchemaId(schema.getId())) {
					o.printf(" |    |-> v%s, state=%s, length=%d, referredBy=%d  [%s]%n",
							schemaVersion.getVersion(),
							EventPortalWrapper.INSTANCE.getState(schemaVersion.getStateId()).getName(),
							(schemaVersion.getContent() == null ? 0 : schemaVersion.getContent().length()),
							(schemaVersion.getReferencedByEventVersionIds() == null ? 0 : schemaVersion.getReferencedByEventVersionIds().size()),
							schemaVersion.getId());
					if (schemaVersion.getReferencedByEventVersionIds() == null) continue;
					for (String eventVerId : schemaVersion.getReferencedByEventVersionIds()) {
						EventVersion eventVersion = EventPortalWrapper.INSTANCE.getEventVersion(eventVerId);
						if (eventVersion == null) continue;  // shouldn't happen!
    					Event event = EventPortalWrapper.INSTANCE.getEvent(eventVersion.getEventId());
    					ApplicationDomain origDomain = EventPortalWrapper.INSTANCE.getDomain(event.getApplicationDomainId());

    					o.printf(" |    |    |-> USED BY EVENT: '%s'%s, v%s, state=%s, broker=%s, topic='%s'  [%s]%n",
    							event.getName(),
    							origDomain.getId().equals(domain.getId()) ? "" : " (EXT)",
    									eventVersion.getVersion(),
								EventPortalWrapper.INSTANCE.getState(eventVersion.getStateId()).getName(),
								eventVersion.getDeliveryDescriptor().getBrokerType(),
								buildTopic(eventVersion.getDeliveryDescriptor()),
								eventVerId);
					}
					
					
					
    			}
    		}
    		System.out.println(" |");
    	}
    }
    
    
    public static void test1(String eventVerId) {
    	
    	EventVersion ev = EventPortalWrapper.INSTANCE.getEventVersion(eventVerId);
    	Event event = EventPortalWrapper.INSTANCE.getEvent(ev.getEventId());
    	o.println(event.getName() + " v" + ev.getVersion());
    	System.out.println(ev.getSchemaVersionId());
    	o.println(ev.getSchemaPrimitiveType());
    	o.println(ev.getDeliveryDescriptor());
    	o.println(ev.getDeliveryDescriptor().getKeySchemaVersionId());
    	o.println(ev.getDeliveryDescriptor().getKeySchemaPrimitiveType());
    	o.println();
    }

    
    
    
	@BeforeClass public static void load() {
    	Properties p = new Properties();
    	try {
			p.load(new FileInputStream("token.properties"));
			EventPortalWrapper.INSTANCE.setToken(p.getProperty("token"));
			EventPortalWrapper.INSTANCE.loadAll();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
    
    

    @Test
    public void testLoader() throws ApiException, IOException {
        o.println("#####################################");
        calcExternalEvents();
        o.println();
        o.println();
        o.println("#####################################");
        drawAppsTable();
        o.println();
        o.println();
        o.println("#####################################");
        drawEventsTable();
        o.println();
        o.println();
        o.println("#####################################");
        drawSchemasTable();
        o.println();
        o.println();
        o.println("#####################################");
        MutableTreeNode node = drawAppsTree();
        node.getChildCount();
        
        
        test1("kdcce3vxdup");
        test1("9imb4qc38rh");
        test1("a7gf2ygh07z");
        
        
        
    }
}


