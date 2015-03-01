package com.helal.csconnector;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Utilities {
	
	public static boolean isConnectionAvailble(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}
	
	/**
	 * Determines a String contains any character or not.
	 * 
	 * @param value
	 *            String which needs to be validated.
	 * @return true if it contains at least one character, false otherwise.
	 * @see String
	 */

	public static boolean isValidString(String value) {
		if (value != null)
			if (!value.equals(""))
				return true;
		return false;
	}
	
	public static void showLog(String tag, String messgae) {

		showLog(tag, messgae, 0);

	}

	public static void showLog(String tag, String messgae, int type) {

		// Type 0 == Info; 1 == Warning; 2 == Error 3 == Debug

		if (messgae != null && tag != null) {
			if (BuildConfig.DEBUG) {
				if (type == 0) {

					Log.i(tag, messgae);

				} else if (type == 1) {
					Log.w(tag, messgae);

				} else if (type == 2) {
					Log.e(tag, messgae);

				} else if (type == 3) {
					Log.d(tag, messgae);

				}
			}

		} else {

			Log.e("TAG is Null", "Message is null");
		}

	}

}
