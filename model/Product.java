package com.ecom.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(length = 500)
	private String title;
	@Column(length = 5000)
	private String description;
	private String category;
	private Double price;
	private int stock;
	private String image;
	private int discount;
	private Double discountedPrice;
	private Boolean isActive;
	public Product() {
	}
	public Product(int id, String title, String description, String category, Double price, int stock, String image,
			int discount, Double discountedPrice, Boolean isActive) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.category = category;
		this.price = price;
		this.stock = stock;
		this.image = image;
		this.discount = discount;
		this.discountedPrice = discountedPrice;
		this.isActive = isActive;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public int getStock() {
		return stock;
	}
	public void setStock(int stock) {
		this.stock = stock;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public int getDiscount() {
		return discount;
	}
	public void setDiscount(int discount) {
		this.discount = discount;
	}
	public Double getDiscountedPrice() {
		return discountedPrice;
	}
	public void setDiscountedPrice(Double discountedPrice) {
		this.discountedPrice = discountedPrice;
	}
	public Boolean getIsActive() {
		return isActive;
	}
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
	@Override
	public String toString() {
		return "Product [id=" + id + ", title=" + title + ", description=" + description + ", category=" + category
				+ ", price=" + price + ", stock=" + stock + ", image=" + image + ", discount=" + discount
				+ ", discountedPrice=" + discountedPrice + ", isActive=" + isActive + "]";
	}
}
