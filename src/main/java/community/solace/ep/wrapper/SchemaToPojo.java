package community.solace.ep.wrapper;

import java.io.File;
import java.io.IOException;

import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.rules.RuleFactory;

import com.sun.codemodel.JCodeModel;

import community.solace.ep.client.model.EventVersion;
import community.solace.ep.client.model.SchemaObject;
import community.solace.ep.client.model.SchemaVersion;

public class SchemaToPojo {

	public static abstract class Builder {

		public enum PayloadType {
			JSON,
		}
		public enum AnnotationType {
			JACKSON2,
			GSON,
		}
		
		protected String schemaContents = "";
		protected PayloadType payloadType = PayloadType.JSON;
		protected String filePath = "tmp";
		protected String basePackageName = "org.example";
		
		protected String packageNameSuffix = "suffix";
		protected String className = "DefaultClass";
		protected String version = "000000";

		public Builder setSchemaContentText(String schemaContents) {
			this.schemaContents = schemaContents;
			return this;
		}
		
		public Builder setFilePath(String filePath) {
			this.filePath = filePath;
			return this;
		}
		
		public Builder setBasePackageName(String basePackageName) {
			this.basePackageName = basePackageName;
			return this;
		}
		
		public String getSchema() {
			return schemaContents;
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
		
		public String getPackageNameSuffix() {
			return packageNameSuffix;
		}

		public String getClassName() {
			return className;
		}
		
		public String getVersion() {
			return version;
		}

	}
	
	
	public static class BuilderFromSchema extends Builder {

		private final SchemaObject schema;
		private final SchemaVersion schemaVersion;
		
		public BuilderFromSchema(SchemaObject schema, SchemaVersion schemaVersion) {
			if (schemaVersion == null) throw new IllegalArgumentException("Cannot use null schema version!");
			this.schema = schema;
			this.schemaVersion = schemaVersion;
			setSchemaVersion();
		}

		private BuilderFromSchema setEventVersionId(String eventVersionId) {
			EventVersion eventVersion = EventPortalWrapper.INSTANCE.getEventVersion(eventVersionId);
			return this.setSchemaVersion();
		}
		
		private BuilderFromSchema setSchemaVersion() {
	    	this.setSchemaContentText(schemaVersion.getContent());
//	    	SchemaObject schema = EventPortalWrapper.INSTANCE.getSchema(schemaVersion.getSchemaId());
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
		
		

		public SchemaVersion getSchemaVersion() {
			return schemaVersion;
		}

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
					+ schemaVersion.getId() + ", className=" + className + ", version=" + version + "]";
		}
		
	}

	
	
	public static class BuilderFromText extends Builder {

		
		
		private BuilderFromText notUsedSetSchemaVersionId(String schemaVersionId) {
			// else...
	    	SchemaVersion schemaVersion = EventPortalWrapper.INSTANCE.getSchemaVersion(schemaVersionId);
	    	if (schemaVersion == null) throw new IllegalArgumentException("Cannot find schema version "+schemaVersionId);
	    	SchemaObject schema = EventPortalWrapper.INSTANCE.getSchema(schemaVersion.getSchemaId());
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
					+ basePackageName + ", packageNameSuffix=" + packageNameSuffix + ", className="
					+ className + ", version=" + version + "]";
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
    	mapper.generate(codeModel, builder.getClassName(), packageName, builder.getSchema());
    	
    	File location = new File(builder.getFilePath());
    	location.mkdirs();
    	codeModel.build(location);
    }

	
	
	
}
