package com.example.flette.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FlowerDTO {
	private int flowerId;
	private Integer addPrice;
	private String category;
	private String description;
	private String flowerName;
	private String imageName;
	private String story;
	private boolean show;
}
