package com.example.flette.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class OrderHistoryDTO {
	private int orderId;
	private LocalDateTime orderDate;
	private String status;
	
	private List<OrderHistoryDetailDTO> details;

    @Getter
    @Setter
    public static class OrderHistoryDetailDTO {
        private Integer detailId;
        private Integer money;
        private String productName;
        private String imageName;
        private Integer bouquetCode; // Needed to check review existence
        private boolean hasReview; // This is a new field to check if a review exists
    }
}