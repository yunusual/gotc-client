package com.cribcaged.getoffthecouch.entities;

/**
 * Entity class representing the application users.
 * @author Yunus Evren
 */
public class User {
	private String id;
	private String firstName;
	private String lastName;
	private String fullName;
	
	/**
	 * Constructor of the class.
	 * @param id - Facebook id of the user
	 * @param firstName - first name of the user
	 * @param lastName - last name of the user
	 * @param fullName - full name of the user
	 */
	public User(String id, String firstName, String lastName, String fullName) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.fullName = fullName;
	}

	/**
	 * Returns the Facebook id of the user
	 * @return Facebook id of user
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Returns the first name of the user
	 * @return first name of user
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Returns the last name of the user
	 * @return last name of user
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Returns the full name of the user
	 * @return full name of user
	 */
	public String getFullName() {
		return fullName;
	}
}
