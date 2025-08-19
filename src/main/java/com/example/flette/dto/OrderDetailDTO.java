package com.example.flette.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class OrderDetailDTO {
    // Information from the Orders table (total order info)
    private Integer orderId;
    private String impUid;
    private LocalDateTime orderDate;
    private String status;
    private Integer totalMoney;
    private String userid; // Consistent with Java conventions
    private String orderAddress;

    // Information from the OrderDetail table (list of detailed items)
    private List<ProductDetail> details;

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    public static class ProductDetail {
        private Integer detailId;
        private Integer bouquetCode;
        private Integer money;
        private String productName;
        private String imageName;
        private boolean hasReview;
        private List<BouquetComponent> components; // Added list of bouquet components
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    public static class BouquetComponent {
        private String type; // e.g., "MAIN", "SUB", "ADDITIONAL"
        private String name; // e.g., "장미", "안개꽃"
        private Integer addPrice; // The price of the component
    }
}
