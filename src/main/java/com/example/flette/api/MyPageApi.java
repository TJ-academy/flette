package com.example.flette.api;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.flette.dto.MyPageStatsDTO;
import com.example.flette.entity.Bouquet;
import com.example.flette.entity.Member;
import com.example.flette.entity.OrderDetail;
import com.example.flette.entity.Orders;
import com.example.flette.entity.Product; // Product 엔티티 임포트
import com.example.flette.entity.Review;
import com.example.flette.entity.Question;
import com.example.flette.repository.MemberRepository;
import com.example.flette.repository.OrderDetailRepository;
import com.example.flette.repository.OrdersRepository;
import com.example.flette.repository.ReviewRepository;
import com.example.flette.repository.QuestionRepository;
import com.example.flette.repository.ProductRepository; // ProductRepository 임포트
import com.example.flette.repository.BouquetRepository; // BouquetRepository 추가


@RestController
@RequestMapping("/api/mypage")
public class MyPageApi {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductRepository productRepository; // ProductRepository 주입
    
    @Autowired
    private BouquetRepository bouquetRepository; // BouquetRepository 주입

    // 📌 마이페이지 통계
    @GetMapping("/stats")
    public ResponseEntity<MyPageStatsDTO> getStats(@RequestParam("userid") String userid) {
    	long orderCount = ordersRepository.countByUserid(userid);
    	long reviewCount = reviewRepository.countByWriter(userid);
    	long qnaCount = questionRepository.countByUserid(userid);
        MyPageStatsDTO stats = new MyPageStatsDTO(orderCount, reviewCount, qnaCount);
        return ResponseEntity.ok(stats);
    }

    // 📌 회원 정보 조회
    @GetMapping("/member/{userid}")
    public ResponseEntity<Member> getMemberInfo(@PathVariable("userid") String userid) {
        return memberRepository.findById(userid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // 📌 회원 정보 수정
    @PostMapping("/member/update/{userid}")
    public ResponseEntity<String> updateMemberInfo(@PathVariable("userid") String userid,
                                                   @RequestBody Map<String, String> updates) {
        try {
            Optional<Member> memberOptional = memberRepository.findById(userid);
            if (memberOptional.isPresent()) {
                Member member = memberOptional.get();

                if (updates.containsKey("username")) member.setUsername(updates.get("username"));
                if (updates.containsKey("address1")) member.setAddress1(updates.get("address1"));
                if (updates.containsKey("address2")) member.setAddress2(updates.get("address2"));
                if (updates.containsKey("tel")) member.setTel(updates.get("tel"));
                if (updates.containsKey("zipcode")) member.setZipcode(updates.get("zipcode"));

                memberRepository.save(member);
                return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("회원 정보를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("회원 정보 수정 실패: " + e.getMessage());
        }
    }

    // 📌 비밀번호 수정
    @PostMapping("/password/update/{userid}")
    public ResponseEntity<String> updatePassword(@PathVariable("userid") String userid,
                                                 @RequestBody Map<String, String> passwords) {
        try {
            Optional<Member> memberOptional = memberRepository.findById(userid);
            if (memberOptional.isPresent()) {
                String currentPwd = passwords.get("currentPwd");
                String newPwd = passwords.get("newPwd");

                if (memberRepository.checkPassword(userid, currentPwd) == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("기존 비밀번호가 올바르지 않습니다.");
                }
                memberRepository.updatePassword(userid, newPwd);
                return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("회원 정보를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("비밀번호 변경 실패: " + e.getMessage());
        }
    }

    // 📌 회원탈퇴
    @DeleteMapping("/member/delete/{userid}")
    public ResponseEntity<String> deleteMember(@PathVariable("userid") String userid) {
        try {
            Optional<Member> memberOptional = memberRepository.findById(userid);
            if (memberOptional.isPresent()) {
                memberRepository.deleteById(userid);
                return ResponseEntity.ok("이용해주셔서 감사합니다.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 회원 정보를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("회원탈퇴 처리 중 오류가 발생했습니다.");
        }
    }

    // 📌 주문내역 조회
    @GetMapping("/orders/{userid}")
    public ResponseEntity<List<Orders>> getUserOrders(@PathVariable("userid") String userid) {
    	List<Orders> orders = ordersRepository.findByUseridOrderByOrderDateDesc(userid);
        return ResponseEntity.ok(orders);
    }
    
 // 📌 리뷰 조회 (작성할 후기, 작성 완료 후기 분류)
    @GetMapping("/reviews/{userid}")
    public ResponseEntity<Map<String, Object>> getUserReviews(@PathVariable("userid") String userid) {
    	// 작성할 후기 리스트
        List<Map<String, Object>> todoList = new ArrayList<>();
        // 작성 완료 후기 리스트
        List<Map<String, Object>> doneList = new ArrayList<>();

    	// 유저의 모든 주문 내역 조회
        List<Orders> userOrders = ordersRepository.findByUseridOrderByOrderDateDesc(userid);

        for (Orders order : userOrders) {
            // 주문에 포함된 모든 상품(OrderDetails) 조회
            List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getOrderId());

            for (OrderDetail detail : orderDetails) {
                // 해당 bouquet_code로 작성된 리뷰가 있는지 확인하고 리뷰 객체를 가져옴
                Optional<Review> optionalReview = reviewRepository.findByBouquetCode(detail.getBouquetCode());

                Map<String, Object> item = new HashMap<>();
                item.put("orderId", order.getOrderId());
                item.put("bouquetCode", detail.getBouquetCode());
                
                // ⭐ 수정된 부분: productName과 imageName을 조회하는 로직을 재구성합니다.
                String productName = "알 수 없는 상품";
                String imageName = null;

                // 1. order_detail의 bouquet_code로 bouquet 테이블에서 product_id를 찾습니다.
                Optional<Bouquet> optionalBouquet = bouquetRepository.findByBouquetCode(detail.getBouquetCode());

                if (optionalBouquet.isPresent()) {
                    // 2. 찾은 product_id로 product 테이블에서 상품 정보를 조회합니다.
                    Optional<Product> optionalProduct = productRepository.findById(optionalBouquet.get().getProductId());
                    if (optionalProduct.isPresent()) {
                        Product product = optionalProduct.get();
                        productName = product.getProductName();
                        imageName = product.getImageName();
                    }
                }
                
                // 만약 리뷰가 존재한다면, 리뷰에 있는 product_id를 사용해 다시 한 번 조회 시도 (확실성을 위해)
                if (optionalReview.isPresent()) {
                    Review review = optionalReview.get();
                    Optional<Product> reviewProduct = productRepository.findById(review.getProductId());
                    if (reviewProduct.isPresent()) {
                         productName = reviewProduct.get().getProductName();
                         imageName = reviewProduct.get().getImageName();
                    }
                }
                
                item.put("productName", productName);
                item.put("imageName", imageName); 
                item.put("price", detail.getMoney());
                item.put("orderDate", order.getOrderDate());
                
                if (optionalReview.isPresent()) {
                    // 작성 완료 후기
                    Review review = optionalReview.get();
                    item.put("reviewId", review.getReviewId());
                    item.put("reviewContent", review.getReviewContent());
                    item.put("score", review.getScore());
                    item.put("reviewDate", review.getReviewDate());
                    item.put("reviewImage", review.getReviewImage());
                    item.put("writer", review.getWriter());
                    doneList.add(item);
                } else {
                    // 작성할 후기
                    todoList.add(item);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("todoList", todoList);
        result.put("doneList", doneList);
        return ResponseEntity.ok(result);
    }


    // 📌 문의(QnA) 조회
    @GetMapping("/qna/{userid}")
    public ResponseEntity<List<Question>> getUserQna(@PathVariable("userid") String userid) {
        List<Question> qlist = questionRepository.findByUseridOrderByQuestionDateDesc(userid);
        return ResponseEntity.ok(qlist);
    }
}