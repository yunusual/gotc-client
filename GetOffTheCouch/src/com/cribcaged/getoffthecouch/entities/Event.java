package com.cribcaged.getoffthecouch.entities;

/**
 * Entity class representing an event.
 * @author Yunus Evren
 */
public class Event {
	private int eventId;
	private int locationId;
	private int categoryId;
	private String locationName;
	private String founderName;
	private String description;
	private String dateAndTime;
	private int totalScore;
	private String photoURL;
	
	/**
	 * Constructor of the class.
	 * @param eventId - id of the event
	 * @param locationId - location id of the event
	 * @param categoryId - category id of the event
	 * @param locationName - name of the event location
	 * @param founder - name of the person who organises the event
	 * @param description - description of the event
	 * @param dateAndTime - date and time of the event
	 * @param totalScore - total score to be earned from the event
	 * @param photoURL - URL of the thumbnail image of the event location
	 */
	public Event(int eventId, int locationId, int categoryId, String locationName,
			String founder, String description, String dateAndTime, int totalScore, String photoURL) {
		this.eventId = eventId;
		this.locationId = locationId;
		this.categoryId = categoryId;
		this.locationName = locationName;
		this.founderName = founder;
		this.description = description;
		this.dateAndTime = dateAndTime;
		this.totalScore = totalScore;
		this.photoURL = photoURL;
	}

	/**
	 * Returns the event id
	 * @return id of the event
	 */
	public int getEventId() {
		return eventId;
	}

	/**
	 * Returns the location id
	 * @return id of the location
	 */
	public int getLocationId() {
		return locationId;
	}

	/**
	 * Returns the category id
	 * @return id of the category
	 */
	public int getCategoryId() {
		return categoryId;
	}

	/**
	 * Returns the location name
	 * @return name of the location
	 */
	public String getLocationName() {
		return locationName;
	}

	/**
	 * Returns the name of the event organiser
	 * @return name of the event organiser
	 */
	public String getFounderName() {
		return founderName;
	}

	/**
	 * Returns the event description
	 * @return description of the event
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the date and time of the event
	 * @return date and time of the event
	 */
	public String getDateAndTime() {
		return dateAndTime;
	}

	/**
	 * Returns the total score that will be earned upon completing the event
	 * @return total score of the event
	 */
	public int getTotalScore() {
		return totalScore;
	}
	
	/**
	 * Returns the photo URL of the thumbnail image of event's location
	 * @return thumbnail image URL of the event
	 */
	public String getPhotoURL() {
		return photoURL;
	}
}
