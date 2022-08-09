package com.solace.labs.epapi.wrapper;

import java.io.File;
import java.io.IOException;

import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.rules.RuleFactory;

import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import com.solace.labs.epapi.client.model.EventVersion;
import com.solace.labs.epapi.client.model.SchemaObject;
import com.solace.labs.epapi.client.model.SchemaVersion;
import com.sun.codemodel.JCodeModel;

public class SchemaToPojo {

	
	public static class Builder {
		
		public enum PayloadType {
			JSON,
		}
		public enum AnnotationType {
			JACKSON2,
			GSON,
		}
		
		private PayloadType payloadType = PayloadType.JSON;
		private String filePath = "";
		private String basePackageName = "";
		private String schemaVersionId;
		private String packageNameSuffix = "";  // auto-update
		private String className = "";  // auto-update
		private String version = "";  // auto-update


		public Builder setFilePath(String filePath) {
			this.filePath = filePath;
			return this;
		}
		
		public Builder setBasePackageName(String basePackageName) {
			this.basePackageName = basePackageName;
			return this;
		}
		
		public Builder setEventVersionId(String eventVersionId) {
			EventVersion eventVersion = EventPortalClient.INSTANCE.getEventVersion(eventVersionId);
			return this.setSchemaVersionId(eventVersion.getSchemaVersionId());
		}
		
		public Builder setSchemaVersionId(String schemaVersionId) {
			this.schemaVersionId = schemaVersionId;
//			if (!smartUpdate) return this;
			// else...
	    	SchemaVersion schemaVersion = EventPortalClient.INSTANCE.getSchemaVersion(schemaVersionId);
	    	if (schemaVersion == null) throw new IllegalArgumentException("Cannot find schema version "+schemaVersionId);
	    	SchemaObject schema = EventPortalClient.INSTANCE.getSchema(schemaVersion.getSchemaId());
	    	className = schema.getName();
	    	if (className == null || className.isEmpty()) {
	    		className = "Test";
	    	}
	    	className = className.replaceAll("[^a-zA-Z0-9]", "");
	    	className = className.replaceAll("(?i)schema", "");  // drop the word "schema" from the object (if it contains it)
	    	if (className.isEmpty()) {
	    		className = "Test";
	    	}
	    	className = className.substring(0,1).toUpperCase() + (className.length() > 1 ? className.substring(1) : "");
	    	version = schemaVersion.getVersion();
	    	version = "v" + version.replaceAll("[^0-9]", "");
	    	packageNameSuffix = className.toLowerCase() + "." + version;
			return this;
		}

		public PayloadType getPayloadType() {
			return payloadType;
		}

		public String getFilePath() {
			return filePath;
		}

		public String getBasePackageName() {
			return basePackageName;
		}

		public String getSchemaVersionId() {
			return schemaVersionId;
		}

		public String getClassName() {
			return className;
		}

		public String getVersion() {
			return version;
		}
		
		public String getPackageNameSuffix() {
			return packageNameSuffix;
		}
		
//		@Override
//		public String toString() {
//			StringBuilder sb = new StringBuilder();
//			sb.append(false)
//			
//			
//			return sb.toString();
//		}

		public boolean build() {
			try {
				buildPojoJsonJackson2(this);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			
			
		}

		@Override
		public String toString() {
			return "Builder [payloadType=" + payloadType + ", filePath=" + filePath + ", basePackageName="
					+ basePackageName + ", packageNameSuffix=" + packageNameSuffix + ", schemaVersionId="
					+ schemaVersionId + ", className=" + className + ", version=" + version + "]";
		}
		
	}
	
	
    private static void buildPojoJsonJackson2(SchemaToPojo.Builder builder) throws IOException {
    	
    	
    	JCodeModel codeModel = new JCodeModel();

    	GenerationConfig config = new DefaultGenerationConfig() {
    		@Override
    		public boolean isGenerateBuilders() { // set config option by overriding method
    			return true;
    		}
//    		@Override
//    		public AnnotationStyle getAnnotationStyle() {
//    			return AnnotationStyle.GSON;
//    		}
    	};
    	String packageName = builder.getBasePackageName() + "." + builder.getPackageNameSuffix();
    	// "3n47qbnu95q"
//    	URL source = Example.class.getResource("/schema/required.json");
    	SchemaMapper mapper = new SchemaMapper(new RuleFactory(config, new Jackson2Annotator(config), new SchemaStore()), new SchemaGenerator());
//    	SchemaMapper mapper = new SchemaMapper(new RuleFactory(config, new GsonAnnotator(config), new SchemaStore()), new SchemaGenerator());
    	mapper.generate(codeModel, builder.getClassName(), packageName, EventPortalClient.INSTANCE.getSchemaVersion(builder.getSchemaVersionId()).getContent());
    	
    	File location = new File(builder.getFilePath());
    	location.mkdirs();
    	codeModel.build(location);
    }

	
	
	
}
