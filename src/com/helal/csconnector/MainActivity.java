package com.helal.csconnector;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements APIListener {

	private final static String headerDefaultKey = "location";
	private static final String TAG = MainActivity.class.getSimpleName();
	public static final String serverUrlLocal = "http://117.58.241.66:8080/ecotality/api";
	private Bitmap bitmapLocation1;
	private TextView tvStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tvStatus = (TextView) findViewById(R.id.tvStatus);

		// GET Request:
		 getRequest();
		 //getRequestWithParameters();

		// POST Request:
		 //postRequest();
		// postRequestWithJsonBody();
		//postImage();

		// PUT Request:
		// putRequest();
		// putImage();

		// DELETE Request:
		 //deleteRequest();

	}

	private void getRequest() {

		// String url = "http://117.58.241.66:8080/ecotality/api/user/1";
		// String url =
		// "http://117.58.241.66:8080/ecotality/api/mobile_carrier";
		// String url =
		// "http://117.58.241.66:8080/ecotality/api/membership/plan";
		// String url =
		// "http://mobile.indieshuffle.com:8080/mobile/?json=1&key=04ffdb11a4c54c729c743eccc46da873&include=title,url,content,tags,date,author&count=15&page=1";

		// String url =
		// "http://192.168.1.49/testserver/web/app_dev.php/get/mehedi";

		//String url = "http://192.168.1.49/testserver/web/app_dev.php/get";
		
		
		String url = "http://192.168.1.75:8080/HelloWorldStruts2/login";

		APIInBackgrount backProcess = new APIInBackgrount(MainActivity.this,
				url, this);
		backProcess.setRequestType(199);

//		backProcess.addParam("id", String.valueOf(4));
//		backProcess.addHeader("name", "Helal");
//		backProcess.addHeader("name", "Mehadi");
//		backProcess.setHeaderFieldKey(headerDefaultKey);

		backProcess.execute(RestClient.RequestMethod.GET);
	}

	private void deleteRequest() {
		 //String url =
		 //"http://117.58.241.66:8080/ecotality/api/user/1/incard/1";
		//String url = "http://mobile.indieshuffle.com/mobile/user/43783/favorite/mp3-13007";
		 String url = "http://192.168.1.49/testserver/web/app_dev.php/delete/23";

		APIInBackgrount backProcess = new APIInBackgrount(MainActivity.this,
				url, this);
		backProcess.execute(RestClient.RequestMethod.DELETE);
	}

	private void putImage() {
		Bitmap bm = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_launcher);
		String byteAsString1 = Base64.encodeToString(bitMapToByteArray(bm),
				Base64.DEFAULT);

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("fileType", "PNG"));
		params.add(new BasicNameValuePair("content", byteAsString1));

		String url = "http://117.58.241.66:8080/ecotality/api/upload";
		APIInBackgrount backProcess = new APIInBackgrount(MainActivity.this,
				url, this);

		backProcess.addParams(params);
		// backProcess.setMultipartFormData(true);
		backProcess.setHeaderFieldKey(headerDefaultKey);

		backProcess.execute(RestClient.RequestMethod.PUT);
	}

	private void postImage() {
		try {

			String url = "http://192.168.1.49/testserver/web/app_dev.php/post";

			Bitmap bm = BitmapFactory.decodeResource(getResources(),
					R.drawable.ic_launcher);
			String byteAsString1 = Base64.encodeToString(bitMapToByteArray(bm),
					Base64.DEFAULT);

			APIInBackgrount backProcess = new APIInBackgrount(
					MainActivity.this, url, this);

			// backProcess.addParam("name", "cake");
			// backProcess.addParam("type", "PNG");
			// backProcess.addParam("image", byteAsString1);

			JSONObject o = new JSONObject();
			o.put("name", "helal");
			o.put("type", "PNG");
			o.put("image", byteAsString1);
			backProcess.setJsonData(o.toString());

			backProcess.execute(RestClient.RequestMethod.POST);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void putRequest() {
		try {

//			 String url = "http://117.58.241.66:8080/ecotality/api/user";
//			 JSONObject o = new JSONObject();
//			 o.put("name", "helal");
//			 o.put("password", "a1234567");

			String url = "http://117.58.241.66:8080/ecotality/api/user/1/payment_method";
			JSONObject o = new JSONObject();
			o.put("paymentMethod", "basic");

			APIInBackgrount backProcess = new APIInBackgrount(MainActivity.this, url, this);

			backProcess.setJsonData(o.toString());
			backProcess.setHeaderFieldKey(headerDefaultKey);

			backProcess.execute(RestClient.RequestMethod.PUT);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void getRequestWithParameters() {

		// String url = "http://117.58.241.66:8080/ecotality/api/user/1";
		// String url =
		// "http://117.58.241.66:8080/ecotality/api/mobile_carrier";
		// String url =
		// "http://117.58.241.66:8080/ecotality/api/membership/plan";
		String url = "http://117.58.241.66:8080/ecotality/api/map/location/" + 1;
		// authClient.addParam("userId", String.valueOf(userId));

		APIInBackgrount backProcess = new APIInBackgrount(MainActivity.this,
				url, this);
		backProcess.setRequestType(199);

		backProcess.addParam("userId", String.valueOf(1));// for map/location
															// api

		backProcess.execute(RestClient.RequestMethod.GET);
	}

	public void postRequest() {
		try {
			// String url =
			// "http://117.58.241.66:8080/ecotality/api/oauth/authenticate/guest";
			// String url =
			// "http://117.58.241.66:8080/ecotality/api/user/email@gmail.com/forget_password";
			String url = "http://192.168.1.49/testserver/web/app_dev.php/post";

			APIInBackgrount backProcess = new APIInBackgrount(MainActivity.this, url, this);
			// backProcess.setJSONRequest(true);

			JSONObject o = new JSONObject();
			o.put("username", "g@g.com");
			o.put("password", "a1234567");
//			backProcess.setJsonData(o.toString());

			 backProcess.addParam("username", "g@g.com");
			 backProcess.addParam("password", "a1234567");

			backProcess.setHeaderFieldKey(headerDefaultKey);

			backProcess.execute(RestClient.RequestMethod.POST);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void postRequestWithJsonBody() {
		try {
			// String url =
			// "http://117.58.241.66:8080/ecotality/api/oauth/authenticate";
			String url = "http://mobile.indieshuffle.com:8080/mobile/collection/new";

			JSONObject o = new JSONObject();
			o.put("username", "g@g.com");
			o.put("password", "a1234567");

			String postStringData = "{\"userId\":\"" + "43783"
					+ "\",\"name\":\"" + "frustated11" + "\",\"trackId\":\""
					+ "15336" + "\"}";

			APIInBackgrount backProcess = new APIInBackgrount(
					MainActivity.this, url, this);
			// backProcess.setJSONRequest(true);
			// backProcess.setJsonData(o.toString());
			// backProcess.setHeaderFieldKey(headerDefaultKey);

			backProcess.setJsonData(postStringData);

			backProcess.execute(RestClient.RequestMethod.POST);

		} catch (JSONException e) {
			Utilities.showLog(TAG, e.getMessage());
		} catch (Exception e) {
			Utilities.showLog(TAG, e.getMessage());
		}
	}


	private byte[] bitMapToByteArray(Bitmap image) {
		// /Bitmap bmp = intent.getExtras().get("data");
		byte[] byteArray = null;
		if (image != null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byteArray = stream.toByteArray();
			return byteArray;
		}

		return byteArray;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRequestComplete(int statusCode, String response, int requestType,
			Object header) {
		Utilities.showLog(TAG, "type: " + requestType + " \n statusCode: "
				+ statusCode + " \n response: " + response + " \n header: "
				+ header);
		String status = "Request Type: " + requestType + " \nStatus Code: " + statusCode
				+ " \nResponse: " + response + " \nHeader: " + header;
		tvStatus.setText(status);
	}
}
