package com.myapp.iib.backend;

public class Item {

	private final Category category;
	private final String name;
	private final String desc;
	private final double price;
	private final String vendor;
	private final int stock;
	private final int sold;

	public enum Category {
		HEALTHCARE("Healthcare"), DENTAL("Dental"), CONSTRUCTION(
				"Construction");

		private final String name;

		Category(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public Item(Category category, String name, String desc, String vendor,
	            double price, int stock, int sold) {
		this.category = category;
		this.name = name;
		this.desc = desc;
		this.price = price;
		this.vendor = vendor;
		this.stock = stock;
		this.sold = sold;
	}

	public Category getCategory() {
		return category;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public double getPrice() {
		return price;
	}

	public String getVendor() {
		return vendor;
	}

	public int getStock() {
		return stock;
	}

	public int getSold() {
		return sold;
	}

}
