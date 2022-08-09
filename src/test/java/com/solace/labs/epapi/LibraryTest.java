/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.solace.labs.epapi;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.solace.labs.epapi.wrapper.EventPortalClient;
import com.solace.labs.epapi.wrapper.SchemaToPojo;
import com.solace.labs.epapi.wrapper.SchemaToPojo.Builder;

public class LibraryTest {
    @Test public void testSomeLibraryMethod() {
        
    	
    	
    	EventPortalClient.INSTANCE.load();
    	
        
        SchemaToPojo.Builder builder = new Builder().setSchemaVersionId("fkfgxsm5h8x")
        		.setBasePackageName("com.solace.aaron.model")
        		.setFilePath("src/main/java");
        System.out.println(builder);
        assertTrue("builder.build() should return 'true'", builder.build());
    }
}
