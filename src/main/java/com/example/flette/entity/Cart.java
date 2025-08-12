package com.example.flette.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private String userId;  // 장바구니 사용자 (회원 ID)

    @ManyToOne
    @JoinColumn(name = "flower_id")
    private Flower flower;  // 꽃 정보

    @ManyToOne
    @JoinColumn(name = "bouquet_code")
    private Bouquet bouquet;  // 부케 정보

    @ManyToOne
    @JoinColumn(name = "decoration_id")
    private Decoration decoration;  // 장식 정보

    @Column(name = "price")
    private Integer price;  // 각 항목의 가격 (가격 * 수량 계산)

    @Column(name = "quantity")
    private Integer quantity;  // 수량

    @Column(name = "total_price")
    private Integer totalPrice;  // 총 금액 (수량 * 가격 계산)

    // 장바구니에서 해당 항목의 가격 계산
    public void calculateTotalPrice() {
        int itemPrice = (flower != null ? flower.getAddPrice() : 0) 
                      + (bouquet != null ? bouquet.getTotalMoney() : 0)
                      + (decoration != null ? decoration.getUtilPrice() : 0);
        this.totalPrice = itemPrice * this.quantity;
    }
}
