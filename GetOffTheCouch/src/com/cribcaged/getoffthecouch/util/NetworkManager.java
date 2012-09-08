package com.cribcaged.getoffthecouch.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Class that is responsible for detecting if the device has an active network connection.
 * @author Yunus Evren
 */
public class NetworkManager {
	/**
	 * Checks whether the current Android device is connected to an internet network
	 * @param ctx - caller activity
	 * @return true if an active network connection exists
	 */
	public static boolean isNetworkAvailable(Context ctx) {
		ConnectivityManager connectivityManager 
		= (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}
}
