package com.cribcaged.getoffthecouch.entities;

/**
 * Entity class representing an event location.
 * @author Yunus Evren
 */
public class Location {
	private int id;
	private String name;
	private String desc;
	private String photoThumb;
	private String photoLarge;
	private float latitude;
	private float longitude;
	private int score;
	
	/**
	 * Constructor of the class.
	 * @param id - id of the location
	 * @param name - name of the location
	 * @param desc - description of the location
	 * @param photoThumb - thumbnail image URL of the location
	 * @param photoLarge - large image URL of the location
	 * @param latitude - latitude of the location
	 * @param longitude - longitude of the location
	 * @param score - score per each person upon completing an event in this location
	 */
	public Location(int id, String name, String desc,
			String photoThumb, String photoLarge, float latitude, float longitude,
			int score) {
		this.id = id;
		this.name = name;
		this.desc = desc;
		this.photoThumb = photoThumb;
		this.photoLarge = photoLarge;
		this.latitude = latitude;
		this.longitude = longitude;
		this.score = score;
	}
	
	/**
	 * Returns the location id
	 * @return id of the location
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the location name
	 * @return name of the location
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the location's description
	 * @return description of the location
	 */
	public String getDesc() {
		return desc;
	}
	
	/**
	 * Returns the  thumbnail image URL of the location
	 * @return URL for thumbnail photo of the location
	 */
	public String getPhotoThumb() {
		return photoThumb;
	}

	/**
	 * Returns the  large image URL of the location
	 * @return URL for large photo of the location
	 */
	public String getPhotoLarge() {
		return photoLarge;
	}

	/**
	 * Returns the latitude of location
	 * @return latitude of the location
	 */
	public float getLatitude() {
		return latitude;
	}
	/**
	 * Returns the longitude of location
	 * @return longitude of the location
	 */
	public float getLongitude() {
		return longitude;
	}
	
	/**
	 * Returns the score per person of location
	 * @return score per person of the location
	 */
	public int getScore() {
		return score;
	}
}
