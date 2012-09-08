package com.cribcaged.getoffthecouch.entities;

/**
 * Entity class representing an event category.
 * @author Yunus Evren
 */
public class Category {
	private int id;
	private String name;
	
	/**
	 * Constructor of the class.
	 * @param id - id of the event category
	 * @param name - name of the category
	 */
	public Category(int id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Returns the category id
	 * @return category id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the category name
	 * @return category name
	 */
	public String getName() {
		return name;
	}
}
