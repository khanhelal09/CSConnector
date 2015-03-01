package com.helal.csconnector;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public class RestClient {
	private static final String TAG = RestClient.class.getSimpleName();

	private String jsonData;
	private String requestUrl = "";
	private int responseCode;
	private String message;
	private String response;

	public static enum RequestMethod {
		GET, POST, PUT, DELETE
	}

	private int readTimeout = 20000;// in milliseconds, 20 seconds
	private int socketTimeout = 30000; // in milliseconds, 60 seconds

	private boolean isJSONRequest = false;

	private boolean isSecured = false;
	private boolean isMultipartFormData = false;
	private String fileName;
	private byte[] fileAsByteArray;
	private Map<String, List<String>> headerFieldsMap;
	private String headerFieldKey;
	private String headerFieldValue;

	private ArrayList<NameValuePair> params;
	private ArrayList<NameValuePair> headers;

	/**
	 * Initialization of RestClient constructor with specific URL.
	 * 
	 * @param String
	 */

	public RestClient(String url) {
		this.requestUrl = url;
		headers = new ArrayList<NameValuePair>();
		params = new ArrayList<NameValuePair>();
	}

	public RestClient(String url, boolean isJSONRequest) {
		this.requestUrl = url;
		this.setJSONRequest(isJSONRequest);
		headers = new ArrayList<NameValuePair>();
		params = new ArrayList<NameValuePair>();
	}

	public RestClient(String url, boolean isJSONRequest, boolean isSecured) {
		this(url, isJSONRequest);
		this.isSecured = isSecured;
	}

	public void execute(RequestMethod method) {
		HttpURLConnection conn = initializeAndOpenConnection(method);

		switch (method) {
		case GET:
			doGetOrDelete(conn, "GET");
			break;
		case POST:
			doPostOrPut(conn, "POST");
			break;
		case PUT:
			doPostOrPut(conn, "PUT");
			break;
		case DELETE:
			doGetOrDelete(conn, "DELETE");
			break;
		}
	}

	private HttpURLConnection initializeAndOpenConnection(RequestMethod method) {
		URL url = null;
		HttpURLConnection conn = null;
		try {
			requestUrl += addParamsWithUrl(method);

			Log.i(TAG, "Request Url: " + requestUrl);
			url = new URL(requestUrl);
			conn = (HttpURLConnection) url.openConnection();

			// add headers
			for (NameValuePair h : headers) {
				conn.addRequestProperty(h.getName(), h.getValue());
			}

			conn.setReadTimeout(readTimeout);
			conn.setConnectTimeout(socketTimeout);
			conn.setRequestProperty("Accept-Encoding", "gzip");

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return conn;
	}

	// GET,DELETE:
	public void doGetOrDelete(HttpURLConnection conn, String requestMethod) {
		try {
			conn.setDoInput(true);
			conn.setRequestMethod(requestMethod);
			conn.connect();

			//Process response
			processResponse(conn);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeAllConnection(null, null, conn);
		}
	}

	// POST,PUT:
	public void doPostOrPut(HttpURLConnection conn, String requestMethod) {
		DataOutputStream wr = null;
		try {
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod(requestMethod);
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			//For Json formated body as post data
			if (Utilities.isValidString(jsonData)) {
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setRequestProperty("Content-Length",
						"" + Integer.toString(jsonData.getBytes().length));
				wr = new DataOutputStream(conn.getOutputStream());
				wr.writeBytes(jsonData);
			}
			
			//For key value data as post data
			if (!params.isEmpty()) {
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				wr = new DataOutputStream(conn.getOutputStream());
				wr.writeBytes(getParamsAsString(params));
			}

			//Process response
			processResponse(conn);
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeAllConnection(null, wr, conn);
		}
	}

	private void processResponse(HttpURLConnection conn) {
		InputStream	is = null;
		try {
			// Response Status Code
			responseCode = conn.getResponseCode();
			Log.i(TAG, "Response Code: " + responseCode);
			
			// get header fields from connection
			getHeaderFieldsFromResponse(conn);
	
			// Response
			is = conn.getInputStream();
			String contentEncoding = conn.getContentEncoding();
			// && contentEncoding.equalsIgnoreCase("gzip")
			if (contentEncoding != null
					&& contentEncoding.indexOf("gzip") != -1) {
				is = new GZIPInputStream(is);
			}
	
			response = convertStreamToString(is).trim();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			closeAllConnection(is, null, null);
		}
	}

	private void closeAllConnection(InputStream is, DataOutputStream wr,
			HttpURLConnection conn) {
		if (is != null) {
			try {
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (wr != null) {
			try {
				wr.flush();
				wr.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (conn != null) {
			try {
				conn.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
	}


	private String addParamsWithUrl(RequestMethod method) {
		String combinedParams = "";
		try {
			switch (method) {
			case GET:
				// add parameters
				if (!params.isEmpty()) {
					combinedParams += "?";
					for (NameValuePair p : params) {
						String paramString = p.getName() + "="
								+ URLEncoder.encode(p.getValue(), "UTF-8");
						if (combinedParams.length() > 1) {
							combinedParams += "&" + paramString;
						} else {
							combinedParams += paramString;
						}
					}
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return combinedParams;
	}


	private void getHeaderFieldsFromResponse(HttpURLConnection conn) {
		try {
			// get all headers
			headerFieldsMap = conn.getHeaderFields();
			// get header value by key
			if (getHeaderFieldKey() != null && getHeaderFieldKey() != "") {
				headerFieldValue = conn.getHeaderField(getHeaderFieldKey());
				Log.i(TAG, "Header Value: " + this.getHeaderFieldValue());
			}
	
			//for (Map.Entry<String, List<String>> entry : headerFieldsMap.entrySet()) {
				// System.out.println("Key : " + entry.getKey() + " ,Value : " +
				// entry.getValue());
			//}
	
			// get header by 'key'
			// String server = conn.getHeaderField("Server");
			// Log.e(TAG, "header Value: " + server);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private String getParamsAsString(List<NameValuePair> params){
		StringBuilder result = new StringBuilder();
		try {
			boolean first = true;
	
			for (NameValuePair pair : params) {
				if (first)
					first = false;
				else
					result.append("&");
	
				result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
				result.append("=");
				result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
			}
		}catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	private static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
	
	private void writeMultipartData(OutputStreamWriter outputStream) {
		String name = fileName;
		if (name == null || name == "") {
			name = "image.jpg";
		}

		// http://stackoverflow.com/questions/11766878/sending-files-using-post-with-httpurlconnection
		String attachmentName = "bitmap";
		String attachmentFileName = "bitmap.bmp";
		String crlf = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		// outputStream.write(twoHyphens + boundary + crlf);
		// outputStream.writeBytes("Content-Disposition: form-data; name=\"" +
		// attachmentName + "\";filename=\"" + attachmentFileName + "\"" +
		// crlf);
		// outputStream.write(crlf.getBytes());

		if (fileAsByteArray != null) {
			try {
				// outputStream.write(fileAsByteArray);
				outputStream.write(name);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// outputStream.writeBytes(crlf);
		// outputStream.writeBytes(twoHyphens + boundary + twoHyphens + crlf);

		/*
		 * ContentBody mimePart = new ByteArrayBody(fileAsByteArray, name);
		 * MultipartEntity entity = new MultipartEntity();
		 * entity.addPart("file", mimePart); request.setEntity(entity);
		 */

	}

	/**
	 * AddParam method is used to store key-value pair. When we'll send our
	 * data/information to web service, that data will go as key-value pair.
	 * each key will have a specific value.
	 * 
	 * @param name
	 *            as String which indicates key.
	 * @param value
	 *            as String which indicates the value of that particular key.
	 * @see #BasicNameValuePair(name, value).
	 */

	public void addParam(String name, String value) {
		this.params.add(new BasicNameValuePair(name, value));
	}

	/**
	 * AddParams method is used to store an array list. On that Array List,
	 * entries are stored as Key-Value pair.
	 * 
	 * @param param
	 *            which is an ArrayList of NameValuePair.
	 */

	public void addParams(ArrayList<NameValuePair> paramList) {
		this.params = paramList;
	}

	/**
	 * AddHeader method is used to store key-value pair. When we'll send our
	 * data/information to web service, Basically on that time, we used to send
	 * our own Authentication Token value using key-value pair through this
	 * method.
	 * 
	 * @param name
	 *            as String which indicates key.
	 * @param value
	 *            as String which indicates the value of that particular key.
	 * @see #BasicNameValuePair(name, value).
	 */

	public void addHeader(String name, String value) {
		this.headers.add(new BasicNameValuePair(name, value));
	}

	/**
	 * AddHeader method is used to store key-value pair. When we'll send our
	 * data/information to web service, Basically on that time, we used to send
	 * our own Authentication Token value using key-value pair through this
	 * method.
	 * 
	 * @param name
	 *            as String which indicates key.
	 * @param value
	 *            as String which indicates the value of that particular key.
	 * @see #BasicNameValuePair(name, value).
	 */

	public void addHeaders(ArrayList<NameValuePair> headersList) {
		this.headers = headersList;
	}

	/**
	 * @return the headerFieldsMap
	 */
	public Map<String, List<String>> getHeaderFieldsMap() {
		return headerFieldsMap;
	}

	/**
	 * @return the headerFieldKey
	 */
	public String getHeaderFieldKey() {
		return headerFieldKey;
	}

	/**
	 * @param headerFieldKey
	 *            the headerFieldKey to set
	 */
	public void setHeaderFieldKey(String headerFieldKey) {
		this.headerFieldKey = headerFieldKey;
	}

	/**
	 * @return the headerFieldValue
	 */
	public String getHeaderFieldValue() {
		return headerFieldValue;
	}

	/**
	 * Returns Response of web service as String. When a user asked for an web
	 * service & if it is executed, it'll return it's response.
	 * 
	 * @return response of web service.
	 * @see String.
	 */

	public String getResponse() {
		return response;
	}

	/**
	 * Returns Error message as String. When a user asked for an web service &
	 * if it fails, it'll return it's error message.
	 * 
	 * @return error message of web service.
	 * @see String.
	 */

	public String getErrorMessage() {
		return message;
	}

	/**
	 * Returns Response code(Status Code) as Integer. When a user asked for an
	 * web service & executes it & based on the result, it'll return an Integer
	 * value which indicates it's status.
	 * 
	 * @return Status Code.
	 * @see Integer.
	 */

	public int getResponseCode() {
		return responseCode;
	}

	/**
	 * @return the readTimeout
	 */
	public int getReadTimeout() {
		return readTimeout;
	}

	/**
	 * @param readTimeout
	 *            the readTimeout to set
	 */
	public void setReadTimeout(int readTimeoutInMilliseconds) {
		this.readTimeout = readTimeoutInMilliseconds;
	}

	/**
	 * @return the socketTimeout
	 */
	public int getSocketTimeout() {
		return socketTimeout;
	}

	/**
	 * @param socketTimeout
	 *            the socketTimeout to set
	 */
	public void setSocketTimeout(int socketTimeoutInMilliseconds) {
		this.socketTimeout = socketTimeoutInMilliseconds;
	}

	/**
	 * Get json response/data.
	 * 
	 * @return the jsonData
	 */
	public String getJsonData() {
		return jsonData;
	}

	/**
	 * Set json data.
	 * 
	 * @param jsonData
	 *            the jsonData to set
	 */
	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}

	/**
	 * Set security.
	 * 
	 * @param isSecured
	 *            true if secured, false otherwise.
	 */
	public void isSecured(boolean isSecured) {
		this.isSecured = isSecured;
	}

	/**
	 * Returns the multi part form of data.
	 * 
	 * @return A boolean value.
	 */
	public boolean isMultipartFormData() {
		return isMultipartFormData;
	}

	/**
	 * Sets up multi part form of data.
	 * 
	 * @param isMultipartFormData
	 *            true if data is multi part form, false otherwise.
	 */
	public void setMultipartFormData(boolean isMultipartFormData) {
		this.isMultipartFormData = isMultipartFormData;
	}

	/**
	 * @return the fileAsByteArray
	 */
	public byte[] getFileAsByteArray() {
		return fileAsByteArray;
	}

	/**
	 * @param fileAsByteArray
	 *            the fileAsByteArray to set
	 */
	public void setFileAsByteArray(byte[] fileAsByteArray) {
		this.fileAsByteArray = fileAsByteArray;
	}

	/**
	 * @param isJSONRequest
	 *            the isJSONRequest to set
	 */
	public void setJSONRequest(boolean isJSONRequest) {
		this.isJSONRequest = isJSONRequest;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	
	
	private void forHttps(){
	    TrustManager[] trustAllCertificates = new TrustManager[] {
	    		new X509TrustManager() {
					
					@Override
					public X509Certificate[] getAcceptedIssuers() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public void checkServerTrusted(X509Certificate[] chain, String authType)
							throws CertificateException {
						// Do nothing. Just allow them all.
					}
					
					@Override
					public void checkClientTrusted(X509Certificate[] chain, String authType)
							throws CertificateException {
						 // Do nothing. Just allow them all.
					}
				}
				
	    };

	    HostnameVerifier trustAllHostnames = new HostnameVerifier() {

			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true; // Just allow them all.
			}
	      
	    };

	    try {
	        System.setProperty("jsse.enableSNIExtension", "false");
	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCertificates, new SecureRandom());
	        
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	        HttpsURLConnection.setDefaultHostnameVerifier(trustAllHostnames);
	    }
	    catch (GeneralSecurityException e) {
	        throw new ExceptionInInitializerError(e);
	    }
	}
	

}
