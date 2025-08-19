package com.example.flette.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flette.dto.BouquetDTO;
import com.example.flette.dto.BouquetInfoDTO;
import com.example.flette.dto.CartDTO;
import com.example.flette.entity.Bouquet;
import com.example.flette.entity.Cart;
import com.example.flette.entity.Member;
import com.example.flette.entity.Product;
import com.example.flette.repository.BouquetRepository;
import com.example.flette.repository.CartRepository;
import com.example.flette.repository.DecorationRepository;
import com.example.flette.repository.FlowerRepository;
import com.example.flette.repository.MemberRepository;
import com.example.flette.repository.ProductRepository;
import com.example.flette.util.BouquetUtils;

@RestController
@RequestMapping("/api/cart")
public class CartApi {

    @Autowired private CartRepository cartRepository;
    @Autowired private BouquetRepository bouquetRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private FlowerRepository flowerRepository;
    @Autowired private DecorationRepository decorationRepository;
    
    
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private BouquetUtils bouquetUtils;
    
    // 장바구니 목록 조회
    @GetMapping("/list/{userid}")
    public Map<String, Object> getCartList(@PathVariable(name = "userid") String userid) {
    	Map<String, Object> map = new HashMap<>();
    	//System.out.println("userid: " + userid);

        List<Cart> carts = cartRepository.findByMember_UseridOrderByCartIdAsc(userid);
        //System.out.println("carts: " + carts);
        
        List<CartDTO> cdtos = new ArrayList<>();
        //List<BouquetDTO> bdtos = new ArrayList<>();
        
        for(Cart cart : carts) {
        	CartDTO cdto = new CartDTO();
        	Bouquet bouquet = cart.getBouquet();
        	BouquetDTO bdto = modelMapper.map(bouquet, BouquetDTO.class);
        	
        	Optional<Product> op = productRepository.findById(bdto.getProductId());
        	
        	cdto.setCartId(cart.getCartId());
        	cdto.setBouquetCode(cart.getBouquet().getBouquetCode());
        	cdto.setPrice(cart.getPrice());
        	cdto.setQuantity(cart.getQuantity());
        	cdto.setTotalPrice(cart.getTotalPrice());
        	cdto.setProductName(op.get().getProductName());
        	
        	List<BouquetInfoDTO> infoList = bouquetUtils.extractBouquetInfo(bouquet);
            cdto.setBouquetInfoList(infoList);
        	
        	cdtos.add(cdto);
        }
        map.put("carts", cdtos);
        //map.put("bouquets", bdtos);
        //System.out.println("cdtos: " + cdtos);
        return map;
    }
    
    @PostMapping("/insert")
    public Map<String, Object> insertCart(@RequestBody CartDTO req) {
    	Map<String, Object> map = new HashMap<>();
    	//System.out.println("insertCart 요청: userid=" + req.getUserid() + ", bouquetCode=" + req.getBouquetCode());
    	try {
    		Optional<Member> member = memberRepository.findById(req.getUserid());
    		Optional<Bouquet> bouquet = bouquetRepository.findById(req.getBouquetCode());
    		
    		if (member.isEmpty()) {
                throw new IllegalArgumentException("해당 사용자가 존재하지 않습니다: " + req.getUserid());
            }
            if (bouquet.isEmpty()) {
                throw new IllegalArgumentException("해당 부케가 존재하지 않습니다: " + req.getBouquetCode());
            }
            
            Cart cart = new Cart();
    		cart.setMember(member.get());
    		cart.setBouquet(bouquet.get());
    		
    		cart.setPrice(req.getPrice());
            cart.setQuantity(req.getQuantity());
            cart.setTotalPrice(req.getPrice() * req.getQuantity());

    		cartRepository.save(cart);
    		
    		map.put("success", true);
    	} catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("message", "입력 오류: " + e.getMessage());
    	} catch (Exception e) {
    		map.put("success", false);
    		map.put("message", "spring에서 장바구니 저장 실패: " + e.getMessage());
    	}
    	return map;
    }

    // 장바구니에서 항목 삭제
    @DeleteMapping("/remove/{cartId}")
    public void removeFromCart(@PathVariable(name = "cartId") Integer cartId) {
        cartRepository.deleteById(cartId);
    }

    // 장바구니 항목 업데이트 (수량)
    @PatchMapping("/update/{cartId}")
    public CartDTO updateCartItem(@PathVariable(name = "cartId") Integer cartId, 
    		@RequestBody Map<String, Integer> body) {
    	Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

        cart.setQuantity(body.get("quantity"));
        cart.setTotalPrice(cart.getPrice() * body.get("quantity"));

        CartDTO dto = new CartDTO();
        dto.setCartId(cart.getCartId());
        dto.setBouquetCode(cart.getBouquet().getBouquetCode());
        dto.setUserid(cart.getMember().getUserid());
        dto.setPrice(cart.getPrice());
        dto.setQuantity(cart.getQuantity());
        dto.setTotalPrice(cart.getTotalPrice());
        
        return dto;
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