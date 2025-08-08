package com.example.flette.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Flower {
	@Id
	private int flowerId;
	
	private String flowerName;
	private Integer addPrice;
	private String description;
	private String imageName;
	private String story;
	private String category;
	private boolean show;
}