package com.helal.csconnector;

import java.util.ArrayList;

import org.apache.http.NameValuePair;

import android.content.Context;
import android.os.AsyncTask;

import com.helal.csconnector.RestClient.RequestMethod;

public class APIInBackgrount extends AsyncTask<RequestMethod, Void, String> {

	private static final String TAG = APIInBackgrount.class.getSimpleName();
	private Context context;
	private APIListener callback;
	private String response;
	private int statusCode;
	private int requestType;
	private String extraData;
	private RestClient restClient;

	public APIInBackgrount(Context context,String url, APIListener callback) {
		this.context = context;
		this.callback = callback;
		
		this.restClient =  new RestClient(url);
	
	
	}

	@Override
	protected void onPreExecute() {
		if (Utilities.isConnectionAvailble(context)) {
			super.onPreExecute();
		} else {
			Utilities.showLog(TAG, "Please Connect Internet.");
			this.cancel(true);
		}
	}

	@Override
	protected String doInBackground(RequestMethod... arg0) {
		try {
			restClient.execute(arg0[0]);

			response = restClient.getResponse();
			statusCode = restClient.getResponseCode();

			if (Utilities.isValidString(restClient.getHeaderFieldKey())) {
				extraData = restClient.getHeaderFieldValue();
			}
		} catch (Exception e) {
			Utilities.showLog(TAG, e.getMessage());
		}
		return "";
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if (callback != null) {
			callback.onRequestComplete(statusCode, response, this.getRequestType(), extraData);
		}
	}
	
		

	/**
	 * @return the requestType
	 */
	public int getRequestType() {
		return requestType;
	}

	/**
	 * @param requestType the requestType to set
	 */
	public void setRequestType(int requestType) {
		this.requestType = requestType;
	}

	public void setJSONRequest(boolean isJsonRequest) {
		restClient.setJSONRequest(isJsonRequest);
	}

	public void setJsonData(String jsonData) {
		restClient.setJsonData(jsonData);
	}

	public void setHeaderFieldKey(String headerdetkey) {
		restClient.setHeaderFieldKey(headerdetkey);
	}

	public void addParam(String key, String value) {
		restClient.addParam(key, value);
	}

	public void addParams(ArrayList<NameValuePair> params) {
		restClient.addParams(params);
	}
	
	public void addHeader(String key, String value) {
		restClient.addHeader(key, value);
	}
	
	public void addHeader(ArrayList<NameValuePair> headerList) {
		restClient.addHeaders(headerList);
	}

	/**
	 * @param readTimeout
	 *            the readTimeout to set
	 */
	public void setReadTimeout(int readTimeoutInMilliseconds) {
		restClient.setReadTimeout(readTimeoutInMilliseconds);
	}

	/**
	 * @param socketTimeout
	 *            the socketTimeout to set
	 */
	public void setSocketTimeout(int socketTimeoutInMilliseconds) {
		restClient.setSocketTimeout(socketTimeoutInMilliseconds);
	}

}
