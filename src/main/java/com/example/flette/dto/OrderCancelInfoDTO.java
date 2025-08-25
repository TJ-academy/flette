package com.example.flette.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderCancelInfoDTO {
    private String productName;
    private String imageName;
    private int totalMoney;
    
    private String bank;
	private String account;
	
    private List<FlowerOption> mainFlowers;
    private List<FlowerOption> subFlowers;
    private List<FlowerOption> foliageFlowers;

    @Data
    public static class FlowerOption {
        private String name;
    }
}