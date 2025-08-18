package com.example.flette.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class DecorationDTO {
	private int decorationId;
	private String decorationName;
	private Integer utilPrice; 
	private String description;
	private String category;
	private String imageName;
	private boolean show;
}
