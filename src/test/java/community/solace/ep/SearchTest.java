package community.solace.ep;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import community.solace.ep.client.model.ApplicationDomain;
import community.solace.ep.client.model.ApplicationVersion;
import community.solace.ep.client.model.Consumer;
import community.solace.ep.client.model.Event;
import community.solace.ep.wrapper.EventPortalWrapper;

public class SearchTest {
	
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

    @Test public void testRnadomId() {
//    	System.out.println(Search.getEventPortalObject("0tjf6z4j4eu"));
    }
    
    
    @Test public void randomThings() {
    	System.out.println(EventPortalWrapper.INSTANCE.getRandomApplication());
    	System.out.println(EventPortalWrapper.INSTANCE.getRandomApplicationVersion());
    	System.out.println(EventPortalWrapper.INSTANCE.getRandomApplicationVersion());
    	System.out.println(EventPortalWrapper.INSTANCE.getRandomApplicationVersion());
    	System.out.println(EventPortalWrapper.INSTANCE.getRandomApplicationVersion());
    	System.out.println(EventPortalWrapper.INSTANCE.getRandomApplicationVersion());
    	System.out.println(EventPortalWrapper.INSTANCE.getRandomApplicationVersion());
    	System.out.println(EventPortalWrapper.INSTANCE.getRandomApplicationVersion());
    	System.out.println(EventPortalWrapper.INSTANCE.getRandomApplicationVersion());
//    	System.out.println(EP.INSTANCE.getRandomEvent());
//    	System.out.println(EP.INSTANCE.getRandomEventVersion());
//    	System.out.println(EP.INSTANCE.getConsumers());
    }
    
    public void printAppsForEvent() {
    	for (int i=0;i<5;i++) {
	    	Event event = EventPortalWrapper.INSTANCE.getRandomEvent();
	    	ApplicationDomain domain = EventPortalWrapper.INSTANCE.getDomain(event.getApplicationDomainId());
	    	System.out.printf("Apps using Event '%s' (%s) in '%s': %s%n", event.getName(), event.getId(), domain.getName(), EventPortalWrapper.INSTANCE.getAppsUsingEvent(event.getId()));
    	}
    }
    
    @Test public void findConsumers() {
    	for (ApplicationVersion appVer : EventPortalWrapper.INSTANCE.getApplicationVersions()) {
    		if (appVer.getConsumers().size() >= 2) {
    			System.out.printf("%s v%s: ", EventPortalWrapper.INSTANCE.getApplication(appVer.getApplicationId()).getName(), appVer.getVersion());
    			for (Consumer consumer : appVer.getConsumers()) {
    				System.out.printf("%s (%s %s), ", consumer.getName(), consumer.getBrokerType(), consumer.getConsumerType());
    			}
    			System.out.println();
    		}
    	}
    }
    
    
}
