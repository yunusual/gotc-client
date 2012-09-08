package com.cribcaged.getoffthecouch.entities;

/**
 * Entity class representing an event photo.
 * @author Yunus Evren
 */
public class Photo {
	private String photoId;
	private int eventId;
	private String userName;
	private String dateAndTime;
	private String smallURL;
	private String largeURL;
	
	/**
	 * Constructor of the class.
	 * @param photoId - Flickr id of photo
	 * @param eventId - id of the event that the photo belongs
	 * @param userName - name of the user that uploaded the photo
	 */
	public Photo(String photoId, int eventId, String userName) {
		this.photoId = photoId;
		this.eventId = eventId;
		this.userName = userName;
		this.dateAndTime = "";
		this.smallURL = "";
		this.largeURL = "";
	}

	/**
	 * Returns the date and time the photo was uploaded
	 * @return date and time when photo was uploaded
	 */
	public String getDateAndTime() {
		return dateAndTime;
	}

	/**
	 * Sets the date and time when the photo was uploaded
	 * @param dateAndTime - date and time to be set
	 */
	public void setDateAndTime(String dateAndTime) {
		this.dateAndTime = dateAndTime;
	}

	/**
	 * Returns the URL of small size photo
	 * @return URL of small size photo
	 */
	public String getSmallURL() {
		return smallURL;
	}

	/**
	 * Sets the URL of small size photo
	 * @param smallURL - URL to be set
	 */
	public void setSmallURL(String smallURL) {
		this.smallURL = smallURL;
	}

	/**
	 * Returns the URL of large size photo
	 * @return URL of large size photo
	 */
	public String getLargeURL() {
		return largeURL;
	}

	/**
	 * Sets the URL of large size photo
	 * @param largeURL - URL to be set
	 */
	public void setLargeURL(String largeURL) {
		this.largeURL = largeURL;
	}

	/**
	 * Returns the Flickr id of photo
	 * @return Flickr id of photo
	 */
	public String getPhotoId() {
		return photoId;
	}

	/**
	 * Return the event id of photo
	 * @return event id of photo
	 */
	public int getEventId() {
		return eventId;
	}

	/**
	 * Returns the name of the uploader of photo
	 * @return photo uploader's name
	 */
	public String getUserName() {
		return userName;
	}
	
	
}
