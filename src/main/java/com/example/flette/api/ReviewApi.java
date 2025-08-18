package com.example.flette.api;

import com.example.flette.dto.ReviewProductDetailDTO;
import com.example.flette.dto.ReviewRequestDTO;
import com.example.flette.entity.Bouquet;
import com.example.flette.entity.Flower;
import com.example.flette.entity.OrderDetail;
import com.example.flette.entity.Orders;
import com.example.flette.entity.Product;
import com.example.flette.entity.Review; // Review 엔티티 임포트
import com.example.flette.repository.*; // 필요한 Repository 임포트
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // PostMapping, RequestBody 임포트

import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewApi {

    @Autowired
    private BouquetRepository bouquetRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FlowerRepository flowerRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ReviewRepository reviewRepository; // ReviewRepository 추가

    /**
     * 리뷰 작성 페이지에 필요한 상품 및 주문 정보를 조회하는 API
     * URL: /api/reviews/write-info/{bouquetCode}
     *
     * @param bouquetCode 리뷰를 작성할 꽃다발의 고유 코드
     * @return ReviewProductDetailDTO 객체 또는 에러 메시지
     */
    @GetMapping("/write-info/{bouquetCode}")
    public ResponseEntity<?> getReviewWriteInfo(@PathVariable("bouquetCode") Integer bouquetCode) {
        ReviewProductDetailDTO reviewDto = new ReviewProductDetailDTO();
        reviewDto.setBouquetCode(bouquetCode);

        Optional<Bouquet> bouquetOpt = bouquetRepository.findById(bouquetCode);
        if (bouquetOpt.isEmpty()) {
            return new ResponseEntity<>("해당 bouquetCode를 가진 꽃다발 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        Bouquet bouquet = bouquetOpt.get();

        reviewDto.setTotalMoney(bouquet.getTotalMoney());
        reviewDto.setProductId(bouquet.getProductId());

        Optional<Product> productOpt = productRepository.findById(bouquet.getProductId());
        if (productOpt.isEmpty()) {
            return new ResponseEntity<>("해당 productId를 가진 상품 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        Product product = productOpt.get();
        reviewDto.setProductName(product.getProductName());
        reviewDto.setImageName(product.getImageName());

        if (bouquet.getMainA() != null) {
            Optional<Flower> mainAFlowerOpt = flowerRepository.findById(bouquet.getMainA());
            mainAFlowerOpt.ifPresent(flower -> {
                reviewDto.setMainACode(bouquet.getMainA());
                reviewDto.setMainAFlowerName(flower.getFlowerName());
            });
        }
        if (bouquet.getMainB() != null) {
            Optional<Flower> mainBFlowerOpt = flowerRepository.findById(bouquet.getMainB());
            mainBFlowerOpt.ifPresent(flower -> {
                reviewDto.setMainBCode(bouquet.getMainB());
                reviewDto.setMainBFlowerName(flower.getFlowerName());
            });
        }
        if (bouquet.getMainC() != null) {
            Optional<Flower> mainCFlowerOpt = flowerRepository.findById(bouquet.getMainC());
            mainCFlowerOpt.ifPresent(flower -> {
                reviewDto.setMainCCode(bouquet.getMainC());
                reviewDto.setMainCFlowerName(flower.getFlowerName());
            });
        }

        Optional<OrderDetail> orderDetailOpt = orderDetailRepository.findByBouquetCode(bouquetCode);
        if (orderDetailOpt.isEmpty()) {
            return new ResponseEntity<>("해당 bouquetCode를 가진 주문 상세 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        OrderDetail orderDetail = orderDetailOpt.get();
        
        Optional<Orders> orderOpt = ordersRepository.findById(orderDetail.getOrderId());
        if (orderOpt.isEmpty()) {
            return new ResponseEntity<>("해당 orderId를 가진 주문 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        Orders order = orderOpt.get();
        reviewDto.setOrderId(order.getOrderId());
        reviewDto.setOrderDate(order.getOrderDate());
        reviewDto.setUserId(order.getUserid());

        return new ResponseEntity<>(reviewDto, HttpStatus.OK);
    }
    
    //-------------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * 리뷰를 저장하는 API
     * URL: /api/reviews/save
     *
     * @param reviewDto 리뷰 정보를 담고 있는 DTO
     * @return 성공 메시지 또는 에러 메시지
     */
    @PostMapping("/save")
    public ResponseEntity<?> saveReview(@RequestBody ReviewRequestDTO reviewDto) {
        try {
            // DTO를 Review 엔티티로 변환
            Review review = new Review();
            review.setBouquetCode(reviewDto.getBouquetCode());
            review.setLuv(0); // 초기 좋아요 수는 0으로 설정
            review.setProductId(reviewDto.getProductId());
            review.setReviewContent(reviewDto.getReviewContent());
            review.setReviewImage(reviewDto.getReviewImage());
            review.setScore(reviewDto.getScore());
            review.setWriter(reviewDto.getWriter());
            review.setReviewDate(new java.util.Date()); // 현재 날짜로 설정

            // Repository를 사용하여 DB에 저장
            Review savedReview = reviewRepository.save(review);
            
            return new ResponseEntity<>("리뷰가 성공적으로 저장되었습니다. (ID: " + savedReview.getReviewId() + ")", HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("리뷰 저장에 실패했습니다. 오류: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}