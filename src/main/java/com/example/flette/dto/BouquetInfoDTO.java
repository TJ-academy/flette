package com.example.flette.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BouquetInfoDTO {
	private String category;
    private String name;
    private Integer addPrice;
}