package com.example.flette.api;

import com.example.flette.entity.*;
import com.example.flette.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartApi {

    @Autowired private CartRepository cartRepository;
    @Autowired private FlowerRepository flowerRepository;
    @Autowired private BouquetRepository bouquetRepository;
    @Autowired private DecorationRepository decorationRepository;
    @Autowired private MemberRepository memberRepository;

    // 장바구니 추가 (꽃, 장식, 부케 등)
    @PostMapping("/add")
    public Cart addToCart(@RequestBody CartAddReq cartAddReq) {
        Member member = memberRepository.findById(cartAddReq.getUserId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Flower flower = flowerRepository.findById(cartAddReq.getFlowerId()).orElse(null);
        Bouquet bouquet = bouquetRepository.findById(cartAddReq.getBouquetCode()).orElse(null);
        Decoration decoration = decorationRepository.findById(cartAddReq.getDecorationId()).orElse(null);

        Cart cart = new Cart();
        cart.setMember(member);
        cart.setFlower(flower);
        cart.setBouquet(bouquet);
        cart.setDecoration(decoration);
        cart.setQuantity(cartAddReq.getQuantity());

        // 가격 계산 로직은 엔티티의 @PostPersist가 처리
        return cartRepository.save(cart);
    }

    // 장바구니 목록 조회
    @GetMapping("/list/{userId}")
    public List<Cart> getCartList(@PathVariable String userId) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return cartRepository.findByMember(member);
    }

    // 장바구니에서 항목 삭제
    @DeleteMapping("/remove/{cartId}")
    public void removeFromCart(@PathVariable Integer cartId) {
        cartRepository.deleteById(cartId);
    }

    // 장바구니 항목 업데이트 (수량)
    @PatchMapping("/update/{cartId}")
    public Cart updateCartItem(@PathVariable Integer cartId, @RequestBody CartUpdateReq req) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));
        cart.setQuantity(req.getQuantity());

        // 가격 계산 로직은 엔티티의 @PostUpdate가 처리
        return cartRepository.save(cart);
    }

    // 주문하기
    @PostMapping("/checkout/{userId}")
    public String checkout(@PathVariable String userId) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<Cart> cartItems = cartRepository.findByMember(member);
        int totalAmount = cartItems.stream().mapToInt(Cart::getTotalPrice).sum();

        // 주문 처리 로직... (예: 주문 생성, 결제 처리 등)
        
        // 주문 후 장바구니 비우기
        cartRepository.deleteAll(cartItems);

        return "주문이 완료되었습니다. 총 금액: " + totalAmount + "원";
    }

    // ===== 요청 DTO =====
    static class CartAddReq {
        private String userId;
        private Integer flowerId;
        private Integer bouquetCode;
        private Integer decorationId;
        private Integer quantity;

        public String getUserId() { return userId; }
        public Integer getFlowerId() { return flowerId; }
        public Integer getBouquetCode() { return bouquetCode; }
        public Integer getDecorationId() { return decorationId; }
        public Integer getQuantity() { return quantity; }
    }

    static class CartUpdateReq {
        private Integer quantity;

        public Integer getQuantity() { return quantity; }
    }
}