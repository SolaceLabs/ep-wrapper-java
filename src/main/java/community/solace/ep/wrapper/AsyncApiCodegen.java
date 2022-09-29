package community.solace.ep.wrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import community.solace.ep.client.model.ApplicationVersion;
import community.solace.ep.client.model.EventVersion;

public class AsyncApiCodegen {
	
	public enum Template {
		SPRING_CLOUD_STREAM("@asyncapi/java-spring-cloud-stream-template"),
		JMS("@asyncapi/java-template"),
		;
		
		private final String templateName;
		
		private Template(String templateName) {
			this.templateName = templateName;
		}
	}

	
	// https://github.com/asyncapi/generator
	// https://api.asyncapi.com/v1/docs#tag/generate
	// 
	
	public static class SolaceSpringCloudStreamBuilder extends Builder {
/*
 * 		Builder b = new Builder()
				.withParam("binder", "solace")
				.withParam("dynamicType", "header")
				.withParam("javaPackage", "com.solace.test.aaron")
				.withParam("host", "localhost")
				.withParam("msgVpn", "default")
				.withParam("username", "aaron");
		
 */
		public SolaceSpringCloudStreamBuilder() {
			//https://github.com/asyncapi/java-spring-cloud-stream-template
			super();
			this.template = Template.SPRING_CLOUD_STREAM;
			withParam("binder", "solace").withParam("dynamicType", "header");
			withParam("javaPackage", "com.example");
			withGroupId("com.example");
			withParam("host", "tcp://localhost:55555");
			withParam("msgVpn", "default");
			withParam("username", "aaron");

		}
		
		public SolaceSpringCloudStreamBuilder withJavaPackage(String javaPackage) {
			return (SolaceSpringCloudStreamBuilder)withParam("javaPackage", javaPackage);
		}
		
		public SolaceSpringCloudStreamBuilder withArtifactId(String artifactId) {
			return (SolaceSpringCloudStreamBuilder)withParam("artifactId", artifactId);
		}
		
		public SolaceSpringCloudStreamBuilder withGroupId(String groupId) {
			return (SolaceSpringCloudStreamBuilder)withParam("groupId", groupId);
		}

		
		/** e.g. "tcps://192.168.23.34:55443"; default == "tcp://localhost:55555" */
		public SolaceSpringCloudStreamBuilder withHost(String host) {
			return (SolaceSpringCloudStreamBuilder)withParam("host", host);
		}

		/** default == "default" */
		public SolaceSpringCloudStreamBuilder withMsgVpn(String msgVpn) {
			return (SolaceSpringCloudStreamBuilder)withParam("msgVpn", msgVpn);
		}

		public SolaceSpringCloudStreamBuilder withUsername(String username) {
			return (SolaceSpringCloudStreamBuilder)withParam("username", username);
		}

		public SolaceSpringCloudStreamBuilder withPassword(String password) {
			return (SolaceSpringCloudStreamBuilder)withParam("password", password);
		}
		
		public Map<String,Set<String>> getChannels() {
			Map<String, Set<String>> functions = new HashMap<>();
			functions.put("publish", new HashSet<>());
			functions.put("subscribe", new HashSet<>());

			JsonObject o = new Gson().fromJson(this.asyncApiText, JsonObject.class);
			if (o.get("channels") != null) {
				JsonObject channels = o.get("channels").getAsJsonObject();
				for (String key : channels.keySet()) {
					JsonObject channel = channels.getAsJsonObject(key);
					if (channel.get("publish") != null) {
						functions.get("publish").add(key);
					}  // could be both
					if (channel.get("subscribe") != null) {
						functions.get("subscribe").add(key);
					}
				}
			}
			System.out.println(functions);
			
//			Map<?, ?>
			
			
			
			
			return functions;
		}
		
		public SolaceSpringCloudStreamBuilder bindPubSubFunction(String functionName, String subChannel, String pubChannel) {
			if (!getChannels().get("publish").contains(pubChannel)) {
				throw new IllegalArgumentException("list of channels does not contain publish "+pubChannel);
			}
			if (!getChannels().get("subscribe").contains(subChannel)) {
				throw new IllegalArgumentException("list of channels does not contain subscribe "+subChannel);
			}
			JsonObject o = new Gson().fromJson(this.asyncApiText, JsonObject.class);
			o.get("channels").getAsJsonObject().get(pubChannel).getAsJsonObject().get("publish").getAsJsonObject().addProperty("x-scs-destination", functionName);
			o.get("channels").getAsJsonObject().get(subChannel).getAsJsonObject().get("subscribe").getAsJsonObject().addProperty("x-scs-destination", functionName);
			this.withAsyncApi(new Gson().toJson(o));
			return this;
		}
		
		
	}
	
	
	public static void bindEvents(ApplicationVersion appVer, String functionName, String subChannel, String pubChannel) {
		List<EventVersion> publishedEvents = new ArrayList<>();
		List<EventVersion> subscribedEvents = new ArrayList<>();
		for (String id : appVer.getDeclaredProducedEventVersionIds()) {
			publishedEvents.add(EventPortalWrapper.INSTANCE.getEventVersion(id));
		}
		for (String id : appVer.getDeclaredConsumedEventVersionIds()) {
			publishedEvents.add(EventPortalWrapper.INSTANCE.getEventVersion(id));
		}
		
		
		
	}
	
	
	
	public static class Builder {
		
		protected File destDir = new File(System.getProperty("user.home") + "/Downloads");  // default
		protected String projectFolderName = "NewAsyncApiApp";
		protected Template template = Template.SPRING_CLOUD_STREAM;
		protected String asyncApiText;
		protected final Map<String, String> parameters = new HashMap<>();

		public Builder() {
		}
		
		public Builder withDestDir(File dir) {
			if (!dir.isDirectory()) throw new IllegalArgumentException(dir.getName() +  " must be a directory");
			this.destDir = dir;
			return this;
		}
		
		public Builder withProjectFolder(String folderName) {
			this.projectFolderName = folderName;
			return this;
		}
		
		public Builder withAsyncApi(String text) {
			this.asyncApiText = text;
			return this;
		}

		public Builder withParam(String key, String value) {
			parameters.put(key, value);
			return this;
		}
		
		public void validate() {
			if (destDir.exists()) {
				if (!destDir.isDirectory()) throw new IllegalArgumentException("destDir is not dir");
				// so it is a directory
				if (!destDir.canWrite()) throw new IllegalArgumentException("Cannot write to destDir");
				
			} else {
				if (!destDir.mkdirs()) {
					throw new IllegalArgumentException("Could not create destDir");
				}
			}
		}
		
		public String getBody() {
			Map<String, Object> body = new HashMap<>();
			body.put("asyncapi", asyncApiText);
			body.put("parameters", parameters);
			body.put("template", template.templateName);
			return new Gson().toJson(body);
		}
		
	}
	
	
	
	private static final String REST_CALL = "https://api.asyncapi.com/v1/generate";
	

	
	public static String loadAsyncApiFile(File f) throws FileNotFoundException {
		StringBuilder sb = new StringBuilder();
		Scanner scanner = new Scanner(f);
		while (scanner.hasNextLine()) {
			sb.append(scanner.nextLine());
		}
		scanner.close();
		return sb.toString();
	}
	
	
	
	/** Uses the OkHttp library used by Swagger */
	public static File getCode2(Builder b) throws Exception {
		
		RequestBody body = RequestBody.create(MediaType.parse("application/json"), b.getBody());
		Request request = new Request.Builder()
				.url(REST_CALL)
				.post(body)
				.build();
		OkHttpClient client = new OkHttpClient();
		Call call = client.newCall(request);
		try {
			Response response = call.execute();
			System.out.println(response.code());
			if (response.code() == 200) {
				
				if ("save zip".equals("no thanks")) {
					File zipFileOutput = new File(b.destDir, b.projectFolderName + ".zip");
					FileOutputStream fos = new FileOutputStream(zipFileOutput);
		            byte[] buffer = new byte[8 * 1024];
	                int bytesRead;
	                while ((bytesRead = response.body().byteStream().read(buffer)) != -1) {
	                	fos.write(buffer, 0, bytesRead);
	                }
	                fos.close();
	                return zipFileOutput;
				} else {
					File unzippedFolderName = new File(b.destDir, b.projectFolderName);
					if (!unzippedFolderName.exists()) {
						if (!unzippedFolderName.mkdirs()) {
							throw new Exception("Couldn't save generate code!  Could not make directory " + unzippedFolderName.getName());
						}
					}
					FileUtil.decompress(response.body().byteStream(), unzippedFolderName);
					return unzippedFolderName;
				}
			} else {
//				FileOutputStream fos = new FileOutputStream(new File(b.destDir, "error.txt"));
				BufferedReader reader = new BufferedReader(response.body().charStream());
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
//					fos.write(line.getBytes());
					sb.append(line);
//					System.out.println(line);
				}
//                fos.close();
				throw new Exception("Couldn't generate code!  " + response.code() + ", " + response.message() + ", " + sb.toString());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception("Couldn't generate code! " + e.toString(),e);
		}
		
	}

	/** Vanilla Java 
	 * @throws Exception */
/*	public static void getCode(Builder b) throws IOException {
        HttpURLConnection httpConnection = null;
        try {
            URL urlObj = new URL(REST_CALL);
            httpConnection = (HttpURLConnection)urlObj.openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);
            httpConnection.setConnectTimeout(10000);
            httpConnection.setReadTimeout(10000);
            httpConnection.addRequestProperty("content-type", "application/json");
            String body = b.generateBody();
            OutputStreamWriter postWriter = null;
            FileOutputStream fos = new FileOutputStream(new File(b.destDir, b.filename));
            try {
                if (body != null && !body.isEmpty()) {
                    postWriter = new OutputStreamWriter(httpConnection.getOutputStream());
                    postWriter.write(body);
                    postWriter.flush();
                    postWriter.close();
                }
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = httpConnection.getInputStream().read(buffer)) != -1) {
                	fos.write(buffer, 0, bytesRead);
                }
                fos.close();
             } finally {
                try {
                    if (postWriter != null) postWriter.close();
                } catch (IOException e) {}
            }
        } catch (IOException e) {
            if (httpConnection != null && httpConnection.getErrorStream() != null) {
                    // let's see if there's any error response on the stream
                    StringBuilder sb = new StringBuilder();
                    String line;
                    // java 1.7: try with resource... will be automatically closed.
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()))) {
                                    while ((line = errorReader.readLine()) != null) {
                                            sb.append(line).append('\n');
                                    }
                            throw new RuntimeException("Caught while trying to fetch SEMP. Additional error: "+sb.toString(),e);
                            } catch (IOException e1) {
                                    // oh well
                            throw new RuntimeException("Caught while trying to fetch SEMP",e);
                            }
            } else {
                    throw new RuntimeException("Caught while trying to fetch SEMP",e);
            }
         } finally {
        	
        }
//		BufferedReader input = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
//		InputStream input = (connection.getInputStream());
//		byte[] buffer = new byte[4096];
//		int n;
//		File f = new File(b.destDir, "hello.zip");
//
//		OutputStream output = new FileOutputStream(f);
//		while ((n = input.read(buffer)) != -1) {
//		    output.write(buffer, 0, n);
//		}
//		output.close();
//		input.close();
//		
		
		
	}
*/	
	

	public static void main(String... args) throws Exception {
		
		
		System.out.println(System.getProperty("user.home"));

		

		
//		System.out.println("Exists? " + b.destDir.exists());
//		System.out.println("Is Directory? " + b.destDir.isDirectory());
//		System.out.println("Can Write? " + b.destDir.canWrite());
//		File f2 = new File(b.destDir, "hello.txt");
//		System.out.println("File Exists? " + f2.exists());
//		System.out.println("Can Write to file? " + f2.canWrite());
//		
//		FileWriter w = new FileWriter(f2);
//		w.append("Hello world");
//		w.close();

		SolaceSpringCloudStreamBuilder b = new SolaceSpringCloudStreamBuilder()
				.withArtifactId("PersonArrivalListener")
				.withJavaPackage("com.solace.aaron.test");
		
		
		
		File f3 = new File("PersonArrivalNotifier-0.1.0.json");
		System.out.println("File Exists? " + f3.exists());
		System.out.println("Can read? " + f3.canRead());

		System.out.println(loadAsyncApiFile(f3));
		b.withAsyncApi(loadAsyncApiFile(f3));
		System.out.println(b.getBody());
//		System.out.println(b.generateBody());

		b.getChannels();
//		b.bindPubSubFunction("NotifyUpdate", "person/{lastname}/arrived", "person/{lastname}/notified");
		
		System.out.println(b.asyncApiText);
		getCode2(b);
	}
	
	
	
}
