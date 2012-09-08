package com.cribcaged.getoffthecouch.util;

import com.google.android.maps.GeoPoint;  

/**
 * Class that calculates the distance between two geolocations using the Haversine formula.
 */
public class DistanceCalculator {  

	private final double RADIUS_OF_EARTH;  

	/**
	 * Constructor of the class.
	 */
	public DistanceCalculator() {  
		RADIUS_OF_EARTH = 6378.1;
	}  

	/**
	 * Returns the distance between two given locations
	 * @param point1 - first location
	 * @param point2 - second location
	 * @return the distance in kilometers
	 */
	public double calculateDistance(GeoPoint point1, GeoPoint point2) {  
		double lat1 = point1.getLatitudeE6()/1E6;  
		double lat2 = point2.getLatitudeE6()/1E6;  
		double lon1 = point1.getLongitudeE6()/1E6;  
		double lon2 = point2.getLongitudeE6()/1E6;  
		double dLat = Math.toRadians(lat2-lat1);  
		double dLon = Math.toRadians(lon2-lon1);  
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +  
		Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *  
		Math.sin(dLon/2) * Math.sin(dLon/2);  
		double c = 2 * Math.asin(Math.sqrt(a));  
		return RADIUS_OF_EARTH * c;  
	}
}
