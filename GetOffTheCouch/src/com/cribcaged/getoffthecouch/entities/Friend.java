package com.cribcaged.getoffthecouch.entities;

/**
 * Entity class representing a Facebook friend.
 * @author Yunus Evren
 */
public class Friend implements Comparable<Friend>{
	private String id;
	private String fullName;
	private int score;
	
	/**
	 * Constructor of the class.
	 * @param id - Facebook id of the friend
	 * @param fullName - full name of the friend
	 * @param score - the score of the friend on the scoreboard
	 */
	public Friend(String id, String fullName, int score) {
		this.id = id;
		this.fullName = fullName;
		this.score = score;
	}

	/**
	 * Returns the Facebook id of the friend
	 * @return Facebook id of the friend
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the full name of the friend
	 * @return full name of the friend
	 */
	public String getFullName() {
		return fullName;
	}
	
	/**
	 * Returns the total score of the friend
	 * @return total score of the friend
	 */
	public int getScore(){
		return score;
	}

	@Override
	public int compareTo(Friend another) {
		String thisName = this.getFullName();
		String anotherName = another.getFullName();
		return thisName.compareTo(anotherName);
	}
	
}
