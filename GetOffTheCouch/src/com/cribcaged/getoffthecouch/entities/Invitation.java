package com.cribcaged.getoffthecouch.entities;

/**
 * Entity class representing an event invitation.
 * @author Yunus Evren
 */
public class Invitation {

	private int invitationId;
	private String locationName;
	private String dateTime;
	private String founderName;
	private int score;
	private int categoryId;
	private int eventId;
	private String photoURL;
	
	/**
	 * Constructor of the class.
	 * @param invitationId - id of the invitation
	 * @param locationName - name of the location for the invited event
	 * @param dateTime - date and time of the event
	 * @param founderName - name of the organiser of the event
	 * @param score - total score that will be earned by completing the event
	 * @param categoryId - category id of the event
	 * @param eventId - id of the event
	 * @param photoURL - URL of the thumbnail image of event location
	 */
	public Invitation(int invitationId, String locationName, String dateTime,
			String founderName, int score, int categoryId, int eventId, String photoURL) {
		this.invitationId = invitationId;
		this.locationName = locationName;
		this.dateTime = dateTime;
		this.founderName = founderName;
		this.score = score;
		this.categoryId = categoryId;
		this.eventId = eventId;
		this.photoURL = photoURL;
	}

	/**
	 * Returns the invitation id
	 * @return id of invitation
	 */
	public int getInvitationId() {
		return invitationId;
	}

	/**
	 * Returns the name of the event location
	 * @return name of location
	 */
	public String getLocationName() {
		return locationName;
	}

	/**
	 * Returns the date and time of the event
	 * @return date and time of event
	 */
	public String getDateTime() {
		return dateTime;
	}

	/**
	 * Returns the name of the friend who is organising the event
	 * @return name of the organiser
	 */
	public String getFounderName() {
		return founderName;
	}

	/**
	 * Returns the total score that will be earned by completing the event
	 * @return total score of event
	 */
	public int getScore() {
		return score;
	}

	/**
	 * Returns the category id of event
	 * @return category id of event
	 */
	public int getCategoryId() {
		return categoryId;
	}

	/**
	 * Returns the event id
	 * @return id of the event
	 */
	public int getEventId() {
		return eventId;
	}

	/**
	 * Returns the URL of the thumbnail image of event's location
	 * @return URL of event location's thumbnail image
	 */
	public String getPhotoURL() {
		return photoURL;
	}
}
