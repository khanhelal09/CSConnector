package com.helal.csconnector;


public interface APIListener {
	public void onRequestComplete(int statusCode, String response , int requestType, Object header);
}
