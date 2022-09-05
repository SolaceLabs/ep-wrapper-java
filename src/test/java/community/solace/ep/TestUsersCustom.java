package community.solace.ep;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

public class TestUsersCustom {



    private static final PrintStream o = System.out;
    
    
	@BeforeClass public static void load() {
    	Properties p = new Properties();
    	try {
			p.load(new FileInputStream("token.properties"));
			EventPortalWrapper.INSTANCE.setToken(p.getProperty("token"));
			ExecutorService pool = Executors.newFixedThreadPool(8);
			
			if (!EventPortalWrapper.INSTANCE.loadAll(pool)) {
				System.err.println("COUldn't load!!!   " + EventPortalWrapper.INSTANCE.getLoadException());
				throw new RuntimeException(EventPortalWrapper.INSTANCE.getLoadException());
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
    
    

    @Test
    public void testLoader() throws ApiException, IOException {
        o.println("#####################################");
        EventPortalWrapper.INSTANCE.loadUsersCustom();
        o.println();
        o.println();
        o.println("#####################################");
        
        
    }
}


