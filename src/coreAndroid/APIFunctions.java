package coreAndroid;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;



public class APIFunctions {

	private String boundary = "*****";
	private String lineEnd = "\r\n";
	private String twoHyphens = "--";
	private String separator = twoHyphens + boundary + lineEnd;

	/***********************************************************************************************
	 * Function Description : To Create Connection to API. Its A private
	 * Function and used in doGet and doPost Functions present in this class
	 * 
	 * @author Rajat jain, date: 31-Dec-2015
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 * *********************************************************************************************/

	public void SetHttpsCertificationByPass() {
		try {
			HttpsURLConnection
			.setDefaultHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname,
						SSLSession session) {
					return true;
				}

			});

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[] { new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			} }, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context
					.getSocketFactory());
		} catch (Exception e) { // should never happen
			e.printStackTrace();
		}
	}

	private HttpURLConnection ClientURLConnection(String url,
			HashMap<String, String> headers, String Action)
					throws MalformedURLException, IOException, URISyntaxException {

		URI uri = new URI(url);
		HttpURLConnection connection = (HttpURLConnection) uri.toURL()
				.openConnection();

		Set set = headers.entrySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry) it.next();

			connection.addRequestProperty(me.getKey().toString(), me.getValue()
					.toString());
		}
		connection.setDoOutput(true);// This sets request method to
		
		// Action(POSt,GET,etc).

		return connection;
	}

	/***********************************************************************************************
	 * Function Description : Return Response Code Of An API
	 * 
	 * @author Rajat jain, date: 31-Dec-2015
	 * *********************************************************************************************/

	private int GetResponseCode(HttpURLConnection connection)
			throws IOException {
		int responseCode = connection.getResponseCode();
		return responseCode;
	}

	/***********************************************************************************************
	 * Function Description : Hit Get API Using this. It will return a hashmap
	 * with response and response time. Keys for Response is "ResponseOfGet" and
	 * for Response Time is "ResponseTime"
	 * 
	 * @author Rajat jain, date: 31-Dec-2015
	 * *********************************************************************************************/

	public HashMap<String, String> doGet(String url,
			HashMap<String, String> headers) throws MalformedURLException,
			IOException, URISyntaxException {

	//	if (url.contains("login") && url.contains("test")) {
			SetHttpsCertificationByPass();
		//}


		HashMap<String, String> responseGet = new HashMap<String, String>();
		HttpURLConnection connection = ClientURLConnection(url, headers, "GET");
		String response = "";
		long startTime = System.currentTimeMillis();
		int responseCode=GetResponseCode(connection);
	
		if (responseCode == HttpURLConnection.HTTP_OK) {
			InputStream inputStream = connection.getInputStream();
			response = readInputStream(inputStream).toString();
		} else {
			InputStream inputStream = connection.getErrorStream();
			response = readInputStream(inputStream).toString();

		}
		long responseTime = System.currentTimeMillis() - startTime;
		System.out.println("Response Time of Get is " + responseTime);
		String rt = responseTime + " ms";
		responseGet.put("response", response);
		responseGet.put("time", rt);
		responseGet.put("code",responseCode+"");

		return responseGet;
	}

	/***********************************************************************************************
	 * Function Description : If Api return anything than this will read and
	 * return string
	 * 
	 * @author Rajat jain, date: 31-Dec-2015
	 * *********************************************************************************************/

	private StringBuffer readInputStream(InputStream inputStream)
			throws IOException {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream), 5000);

		StringBuffer stringBuffer = new StringBuffer();
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			stringBuffer.append(line);
		}
		inputStream.close();
		return stringBuffer;
	}

	/***********************************************************************************************
	 * Function Description : Hit POST API Using this. It will return a hashmap
	 * with response and response time. Keys for Response is "ResponseOfPost"
	 * and for Response Time is "ResponseTime"
	 * 
	 * @author Rajat jain, date: 31-Dec-2015
	 * *********************************************************************************************/

	public HashMap<String, String> doPost(String url,
			HashMap<String, String> headers, String json)
					throws URISyntaxException, MalformedURLException, IOException {

	//	if (url.contains("login") && url.contains("test")) {
			SetHttpsCertificationByPass();
		//}
		HashMap<String, String> responsePost = new HashMap<String, String>();
		HttpURLConnection connection = ClientURLConnection(url, headers, "POST");

		long startTime = System.currentTimeMillis();
		long responseTime;
		OutputStream outputStream = connection.getOutputStream();
		if (!json.equals("")) {

			byte sendData[] = json.getBytes("utf-8");
			outputStream.write(sendData);
			responseTime = System.currentTimeMillis() - startTime;
			outputStream.flush();
			outputStream.close();
		} else {
			responseTime = 0;
		}
		String response = "";
		int responseCode=GetResponseCode(connection);
	
		if ( responseCode== HttpURLConnection.HTTP_NO_CONTENT) {
			response = "";
		} else if (responseCode == HttpURLConnection.HTTP_OK) {
			InputStream inputStream = connection.getInputStream();
			response = readInputStream(inputStream).toString();
		}
		else if(responseCode==HttpURLConnection.HTTP_CREATED)
		{
			response = "";			
		}
		else {
			InputStream inputStream = connection.getErrorStream();
			response = readInputStream(inputStream).toString();
		}

		String rt = responseTime + " ms";
		responsePost.put("response", response);
		responsePost.put("time", rt);
		responsePost.put("code",responseCode+"");
		return responsePost;
	}

	/***********************************************************************************************
	 * Function Description : Merge Two Json returns string
	 * 
	 * @author Rajat jain, date: 18-Jan-2016
	 * *********************************************************************************************/

	public String mergeJsonObjects(JsonObject json1, JsonObject json2) {
		JSONObject JSON1 = null;
		JSONObject JSON2 = null;

		try {
			JSON1 = new JSONObject(json1.toString());
			JSON2 = new JSONObject(json2.toString());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		JSONObject mergedJSON = new JSONObject();

		try {

			mergedJSON = new JSONObject(JSON1, JSONObject.getNames(JSON1));
			for (String crunchifyKey : JSONObject.getNames(JSON2)) {
				mergedJSON.put(crunchifyKey, JSON2.get(crunchifyKey));
			}

		} catch (JSONException e) {
			throw new RuntimeException("JSON Exception" + e);
		}
		String finalJson = mergedJSON.toString();

		return finalJson;
	}

	/***********************************************************************************************
	 * Function Description : Make A Json Of Array Example :- If you want to
	 * make [ { "id": "10220", "endDate": "Present", "jobProfile": "" } {
	 * "organization": "Fresher8", "designation": "Fresh Graduate", "startDate":
	 * "2014-06-01" } ]
	 * 
	 * 
	 * @author Rajat jain, date: 18-Jan-2016
	 * *********************************************************************************************/
	public JsonArray MakeJsonArray(Object arr[]) {

		JsonArray list = new JsonArray();
		int i = 0;
		while (i < arr.length) {
			list.add((JsonElement) arr[i]);
			i++;
		}
		return list;
	}

	/***********************************************************************************************
	 * Function Description : Make a json of Key and Value Example :- {"Key" :
	 * "Value" }
	 * 
	 * @author Rajat jain, date: 18-Jan-2016
	 * *********************************************************************************************/

	public JsonObject MakeKeyValueJson(HashMap<String, Object> keyValue) {
		JsonObject json = new JsonObject();
		String key[] = null, value[] = null;
		// json.addProperty(keyValue.keySet().toString(),
		// keyValue.values().toString());


		key=keyValue.keySet().toString().split(",");


		int i = 0;

		while (i < key.length) {

			if(i==0)
			{
				Object val = keyValue.get(key[i].toString().substring(1).trim());
				if(val instanceof Integer)
				{
					json.addProperty(key[i].toString().substring(1).trim(), ((Integer) val).intValue());
				}
				else if(val==null)
				{
					json.add(key[i].toString().substring(1).trim(),null);
				}
				else
				{
					json.addProperty(key[i].toString().substring(1).trim(),val.toString());	
				}

			}
			else if(i==key.length-1)
			{
				Object val = keyValue.get(key[i].toString().substring(0, key[i].indexOf("]")).trim());
				if(val instanceof Integer)
				{
					json.addProperty(key[i].toString().substring(0, key[i].indexOf("]")).trim(), ((Integer) val).intValue());
				}
				else if(val==null)
				{
					json.add(key[i].toString().substring(0, key[i].indexOf("]")).trim(),null);
				}
				else
				{
					json.addProperty(key[i].toString().substring(0, key[i].indexOf("]")).trim(),val.toString());	
				}

			}
			else
			{
				Object val = keyValue.get(key[i].toString().trim());
				if(val instanceof Integer)
				{
					json.addProperty(key[i].toString().trim(), ((Integer) val).intValue());
				}
				else if(val==null)
				{
					json.add(key[i].toString().trim(),null);
				}
				else
				{
					json.addProperty(key[i].toString().trim(),val.toString());	
				}

			}

			i++;
		}
		return json;
	}

	/***********************************************************************************************
	 * Function Description : Make A json of key and array Example :-
	 * "workExperience": [ { "id": "10220", "organization": "Fresher8",
	 * "designation": "Fresh Graduate", "startDate": "2014-06-01", "endDate":
	 * "Present", "jobProfile": "" } ]
	 * 
	 * @author Rajat jain, date: 18-Jan-2016
	 * *********************************************************************************************/

	public JsonObject MakeJsonObjectOfKeyAndJsonArray(String key, JsonArray arr) {

		JsonObject obj = new JsonObject();
		obj.add(key, arr.getAsJsonArray());
		return obj;
	}

	/***********************************************************************************************
	 * Function Description : Make an Json of key and object Example :-
	 * 
	 * "photoMetadata": { "filename":
	 * "cf424aee01dfdd60dadacd234b4505d2adb37e5b74003ec3.p", "extension": "png",
	 * "uploadDate": "2015-10-07 22:25:47" }
	 * 
	 * @author Rajat jain, date: 18-Jan-2016
	 * *********************************************************************************************/

	public JsonObject MakeJsonObjectOfKeyAndJsonObject(String key,
			JsonObject object) {

		JsonObject obj = new JsonObject();
		obj.add(key, object);

		return obj;
	}

	/***********************************************************************************************
	 * Function Description : Upload File Request Example :- To upload any file
	 * whether its photo or resume
	 * 
	 * @author Rajat jain, date: 18-Jan-2016
	 * *********************************************************************************************/

	public HashMap<String, String> doUploadByPost(String url, HashMap<String, String> headers,
			String json, String fileKey, String filePath, String fileName)
					throws URISyntaxException, MalformedURLException, IOException {
		HashMap<String, String> responseUpload = new HashMap<String, String>();
		HttpURLConnection connection = ClientURLConnection(url, headers, "POST");
		connection.setUseCaches(false);

		connection.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundary);
		DataOutputStream dos = new DataOutputStream(
				connection.getOutputStream());
		InputStream inputStream = null;
		if (filePath != null) {
			File file = new File(filePath);

			inputStream = new FileInputStream(file);

		}
		dos.writeBytes(twoHyphens + boundary + lineEnd);
		dos.writeBytes("Content-Disposition: form-data; name=\"" + fileKey
				+ "\";   filename=\"" + fileName + "\"" + lineEnd);

		dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);

		dos.writeBytes(lineEnd);

		// Read file and create buffer
		int bytesAvailable = inputStream.available();
		int maxBufferSize = 64 * 1024;
		int bufferSize = Math.min(bytesAvailable, maxBufferSize);
		byte[] buffer = new byte[bufferSize];
		// Send file data
		int bytesRead = inputStream.read(buffer, 0, bufferSize);
		while (bytesRead > 0) {
			// Write buffer to socket
			dos.write(buffer, 0, bufferSize);

			bytesAvailable = inputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			bytesRead = inputStream.read(buffer, 0, bufferSize);
		}

		// send multipart form data necesssary after file data
		dos.writeBytes(lineEnd);
		dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
		dos.flush();
		dos.close();
		inputStream.close();

		OutputStream outputStream = connection.getOutputStream();

		outputStream.flush();
		outputStream.close();
		int responseCode = GetResponseCode(connection);
		String response="";
		if ( responseCode== 204) {
			response = "";
		} else if (responseCode == 200) {
			InputStream inputStream1 = connection.getInputStream();
			response = readInputStream(inputStream1).toString();
		} else {
			InputStream inputStream1 = connection.getErrorStream();
			response = readInputStream(inputStream1).toString();
		}
		responseUpload.put("response", response);
		responseUpload.put("code",responseCode+"");
		return responseUpload;

	}

	/***********************************************************************************************
	 * Function Description : Make json from response either of ResponsePost or
	 * ResponseGet Example :- {"Key" : "Value" }
	 * 
	 * @author rashi.atry, date: 29-Jan-2016
	 * @throws ParseException
	 * @throws JSONException
	 * *********************************************************************************************/

	public String getValueFromJson(String jsonResponse, String parameter) {

		String parameterValue = "";
		JsonParser parse = new JsonParser();
		
		parameterValue = parse.parse(jsonResponse).getAsJsonObject().get(parameter).toString();
		parameterValue = parameterValue.replace("\"", "");

		return parameterValue;
	}

	/***********************************************************************************************
	 * Function Description : Convert response to json 
	 * ResponseGet Example :- {"Key" : "Value" }
	 * 
	 * @author rashi.atry, date: 02-May-2016

	 * *********************************************************************************************/
	public Object getValueFromJsonObject(String jsonResponse) {

		JsonObject parameterValue;
		JsonParser parse = new JsonParser();
		parameterValue = parse.parse(jsonResponse).getAsJsonObject();
		return parameterValue;
	}
	/***********************************************************************************************
	 * Function Description : parse api error responses
	 * ResponseGet Example :- {"Key" : "Value" }
	 * 
	 * @author rashi.atry, date: 29-Jan-2016
	 * @throws ParseException
	 * @throws JSONException
	 * *********************************************************************************************/

	/*	

	{
	    "error": {
	        "status": 400,
	        "message": "Validation Error",
	        "code": 4004,
	        "developerMessage": "Please check property values",
	        "validationErrorDetails": [
	            [
	                {
	                    "code": 990035,
	                    "message": "Remove the following educationType before adding a new education : UG"
	                }
	            ]
	        ]
	    }
	}
	{
	    "error": {
	        "status": 400,
	        "message": "Validation Error",
	        "code": 4001,
	        "validationErrorDetails": [
	            {
	                "useremail": {
	                    "message": "Email already exist.",
	                    "code": "4090"
	                }
	            }
	        ],
	        "customData": ""
	    }
	}*/



	/*	public HashMap<String, String> parseAuthorizationErrorResponse(String jsonResponse) {

		HashMap<String, String> data = new HashMap<String,String>();
		Gson gson = new Gson();
		JsonElement json = new JsonParser().parse(jsonResponse);
		JsonObject obj =(JsonObject) json.getAsJsonObject().get("error");
		data.put("status", obj.get("status")+"");
		data.put("message",  obj.get("message").toString());
		data.put("code",  obj.get("code")+"");
		data.put("developerMessage",  obj.get("developerMessage").toString());
		if(jsonResponse.contains("validationErrorDetails"))
		{


		}*/


	//}

}
