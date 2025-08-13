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

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 설정
    @JoinColumn(name = "user_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flower_id")
    private Flower flower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bouquet_code")
    private Bouquet bouquet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decoration_id")
    private Decoration decoration;

    @Column(name = "price")
    private Integer price; // 단가 (수량 1개당 가격)

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "total_price")
    private Integer totalPrice; // 총 금액 (단가 * 수량)

    // 장바구니 항목의 단가와 총 금액을 계산
    @PostPersist
    @PostUpdate
    public void calculatePrices() {
        int itemBasePrice = 0;
        if (this.flower != null) {
            itemBasePrice += this.flower.getAddPrice();
        }
        if (this.bouquet != null) {
            itemBasePrice += this.bouquet.getTotalMoney();
        }
        if (this.decoration != null) {
            itemBasePrice += this.decoration.getUtilPrice();
        }

        this.price = itemBasePrice;
        this.totalPrice = itemBasePrice * this.quantity;
    }
}