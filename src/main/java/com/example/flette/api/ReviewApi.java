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
import org.springframework.web.multipart.MultipartFile; // MultipartFile 추가
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

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

	@PostMapping("/{id}/like")
	public void reviewLuv(@PathVariable(name = "id") Integer reviewId) {
		Optional<Review> optr = reviewRepository.findById(reviewId);
		Review ret = optr.get();
		ret.setLuv(ret.getLuv() + 1);
		reviewRepository.save(ret);
	}
	
    // 이미지 파일을 저장할 서버 내 경로
    private static final String UPLOAD_DIR = "src/main/resources/static/img/reviews/";

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
     * @param reviewData 리뷰 정보를 담고 있는 DTO (JSON 데이터)
     * @param reviewImages 업로드된 이미지 파일
     * @return 성공 메시지 또는 에러 메시지
     */
    @PostMapping("/save")
    public ResponseEntity<String> saveReview(@RequestPart("reviewData") ReviewRequestDTO reviewData,
                                             @RequestPart(value = "reviewImages", required = false) MultipartFile[] reviewImages) {
        try {
            // 리뷰 이미지 파일 저장
            if (reviewImages != null && reviewImages.length > 0) {
                MultipartFile firstImage = reviewImages[0];
                String originalFilename = firstImage.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
                Path uploadPath = Paths.get(UPLOAD_DIR);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(uniqueFilename);
                Files.copy(firstImage.getInputStream(), filePath);

                // DTO에 저장된 파일명 설정
                reviewData.setReviewImage(uniqueFilename);
            }

            // DTO를 Review 엔티티로 변환
            Review review = new Review();
            review.setBouquetCode(reviewData.getBouquetCode());
            review.setLuv(0);
            review.setProductId(reviewData.getProductId());
            review.setReviewContent(reviewData.getReviewContent());
            review.setReviewImage(reviewData.getReviewImage()); // 저장된 파일명으로 설정
            review.setScore(reviewData.getScore());
            review.setWriter(reviewData.getWriter());
            review.setReviewDate(new java.util.Date()); // 현재 날짜로 설정

            // Repository를 사용하여 DB에 저장
            reviewRepository.save(review);
            
            return ResponseEntity.ok("리뷰가 성공적으로 저장되었습니다.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 파일 저장에 실패했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 저장에 실패했습니다.");
        }
    }
}
