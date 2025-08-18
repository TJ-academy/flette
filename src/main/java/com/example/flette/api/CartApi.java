package com.example.flette.api;

import com.example.flette.entity.*;
import com.example.flette.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartApi {

    @Autowired private CartRepository cartRepository;
    @Autowired private BouquetRepository bouquetRepository;
    @Autowired private MemberRepository memberRepository;
    
    // 장바구니 목록 조회
    @GetMapping("/list/{userId}")
    public List<Cart> getCartList(@PathVariable String userId) {
        Member member = memberRepository.findById(userId)
        		.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return cartRepository.findByMember(member);
    }
    
    @PostMapping("/insert")
    public Map<String, Object> insertCart(@RequestBody Cart req) {
    	Map<String, Object> map = new HashMap<>();
    	try {
    		cartRepository.save(req);
    		map.put("success", true);
    	} catch (Exception e) {
    		map.put("success", false);
    		map.put("message", "장바구니 저장 실패");
    	}
    	return map;
    }

    // 장바구니에서 항목 삭제
    @DeleteMapping("/remove/{cartId}")
    public void removeFromCart(@PathVariable Integer cartId) {
        cartRepository.deleteById(cartId);
    }

    // 장바구니 항목 업데이트 (수량)
    @PatchMapping("/update/{cartId}")
    public Cart updateCartItem(@PathVariable Integer cartId, 
    		@RequestBody Cart req) {
        Cart cart = cartRepository.findById(cartId)
        		.orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));
        cart.setQuantity(req.getQuantity());

        // 가격 계산 로직은 엔티티의 @PostUpdate가 처리
        return cartRepository.save(cart);
    }

    // 주문하기
    @PostMapping("/checkout/{userId}")
    public String checkout(@PathVariable String userId) {
        Member member = memberRepository.findById(userId)
        		.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<Cart> cartItems = cartRepository.findByMember(member);
        int totalAmount = cartItems.stream().mapToInt(Cart::getTotalPrice).sum();

        // 주문 처리 로직... (예: 주문 생성, 결제 처리 등)
        
        // 주문 후 장바구니 비우기
        cartRepository.deleteAll(cartItems);

        return "주문이 완료되었습니다. 총 금액: " + totalAmount + "원";
    }
}