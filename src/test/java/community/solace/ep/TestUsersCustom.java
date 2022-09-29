package community.solace.ep;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.BeforeClass;
import org.junit.Test;

import community.solace.ep.client.ApiException;
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
				System.err.println("COUldn't load!!!   " + EventPortalWrapper.INSTANCE.getLoadErrorString());
				throw new RuntimeException(EventPortalWrapper.INSTANCE.getLoadErrorString().toString());
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
        o.println(EventPortalWrapper.INSTANCE.getUserNames());
        o.println();
        o.println();
        o.println("#####################################");
        for (String userId : EventPortalWrapper.INSTANCE.getUserIds()) {
        	o.println(userId + ": "+EventPortalWrapper.INSTANCE.getUserName(userId));
        }
        
    }
}


