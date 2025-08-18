package com.example.flette.api;

import com.example.flette.dto.IamportResponse;
import com.example.flette.dto.OrderDetailDTO;
import com.example.flette.dto.OrderHistoryDTO;
import com.example.flette.dto.PaymentInfo;
import com.example.flette.entity.Orders;
import com.example.flette.repository.OrdersRepository;
import com.example.flette.service.IamportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import com.example.flette.entity.Bouquet;
import com.example.flette.entity.OrderDetail;
import com.example.flette.entity.Product;
import com.example.flette.repository.BouquetRepository;
import com.example.flette.repository.OrderDetailRepository;
import com.example.flette.repository.ProductRepository;

@RestController
@RequestMapping("/api/orders")
public class OrdersApiController {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private IamportService iamportService;
    
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private BouquetRepository bouquetRepository;

    @Autowired
    private ProductRepository productRepository;

    //------결제 관련 로직------------------------------------------------
    // 결제 요청 DTO (필요에 따라 필드 추가)
    public static class PaymentRequestDto {
        public String merchant_uid;
        public int amount;
        public String name;
        public String buyerName;
        public String buyerTel;
        // 기타 필요한 필드 추가
    }

    // 결제 위변조 검증 DTO
    public static class PaymentVerificationDto {
        public String imp_uid;
        public String merchant_uid;
    }

    // 아임포트 결제 사전 등록 API
    // 프론트엔드에서 결제창을 띄우기 전에 호출
    @PostMapping("/prepare")
    public ResponseEntity<?> preparePayment(@RequestBody PaymentRequestDto requestDto) {
        try {
            String accessToken = iamportService.getAccessToken();
            if (accessToken == null) {
                return new ResponseEntity<>("아임포트 토큰 발급 실패", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Orders newOrder = new Orders();
            newOrder.setMerchantUid(requestDto.merchant_uid);
            newOrder.setTotalMoney(requestDto.amount);
            newOrder.setStatus("결제대기");
            ordersRepository.save(newOrder);

            return new ResponseEntity<>(requestDto.merchant_uid, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("결제 사전 등록 실패: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 결제 위변조 검증 API
    // 프론트엔드에서 결제 완료 후 호출
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationDto verifyDto) {
        try {
            String accessToken = iamportService.getAccessToken();
            if (accessToken == null) {
                return new ResponseEntity<>("아임포트 토큰 발급 실패", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            IamportResponse<PaymentInfo> paymentInfoResponse = iamportService.getPaymentInfo(accessToken, verifyDto.imp_uid);
            
            if (paymentInfoResponse == null || paymentInfoResponse.getCode() != 0 || paymentInfoResponse.getResponse() == null) {
                return new ResponseEntity<>("아임포트 결제 정보 조회 실패", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            PaymentInfo paymentInfo = paymentInfoResponse.getResponse();
            int actualAmount = paymentInfo.getAmount();
            String status = paymentInfo.getStatus();

            Optional<Orders> orderOptional = ordersRepository.findByMerchantUid(verifyDto.merchant_uid);

            if (orderOptional.isEmpty()) {
                return new ResponseEntity<>("DB에 존재하지 않는 주문 번호입니다.", HttpStatus.BAD_REQUEST);
            }
            
            Orders savedOrder = orderOptional.get();
            int savedAmount = savedOrder.getTotalMoney();

            if (savedAmount == actualAmount && "paid".equals(status)) {
                savedOrder.setImpUid(verifyDto.imp_uid);
                savedOrder.setStatus("결제완료");
                ordersRepository.save(savedOrder);
                return new ResponseEntity<>("결제 성공 및 DB 업데이트 완료", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("결제 위변조 또는 실패", HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("결제 검증 실패: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Orders>> getAllOrders() {
        List<Orders> ordersList = ordersRepository.findAll();
        return new ResponseEntity<>(ordersList, HttpStatus.OK);
    }
    //--------------------------------------------------------------------

    /**
     * 특정 사용자의 주문 내역을 상세 정보와 함께 조회하는 API.
     * @param userId 조회할 사용자의 ID
     * @return 주문 내역 리스트 (OrderHistoryDto)
     */
    @GetMapping("/history/{userid}")
    public ResponseEntity<List<OrderHistoryDTO>> getUserOrdersWithDetails(@PathVariable("userid") String userid) {
        // 1. userId로 해당 사용자의 모든 주문(Orders) 정보를 조회
        List<Orders> ordersList = ordersRepository.findByUseridOrderByOrderDateDesc(userid);

        // 2. 각 주문에 대해 주문 상세 정보(OrderDetail)를 포함한 DTO를 생성
        List<OrderHistoryDTO> historyList = new ArrayList<>();
        
        for (Orders order : ordersList) {
            OrderHistoryDTO historyDto = new OrderHistoryDTO();
            historyDto.setOrderId(order.getOrderId());
            historyDto.setOrderDate(order.getOrderDate());
            historyDto.setStatus(order.getStatus());

            // 3. 해당 order_id를 가진 모든 order_detail 정보를 조회
            List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getOrderId());

            // 4. 각 order_detail에 대해 상품 정보(Product)를 조회
            List<OrderDetailDTO> detailDtos = new ArrayList<>();
            for (OrderDetail detail : details) {
                OrderDetailDTO detailDto = new OrderDetailDTO();
                detailDto.setDetailId(detail.getDetailId());
                detailDto.setMoney(detail.getMoney());

                // 5. bouquet_code로 bouquet 테이블에서 product_id를 조회
                Optional<Bouquet> bouquetOpt = bouquetRepository.findById(detail.getBouquetCode());
                if (bouquetOpt.isPresent()) {
                    Bouquet bouquet = bouquetOpt.get();
                    
                    // 6. product_id로 product 테이블에서 image_name과 product_name을 조회
                    Optional<Product> productOpt = productRepository.findById(bouquet.getProductId());
                    if (productOpt.isPresent()) {
                        Product product = productOpt.get();
                        detailDto.setProductName(product.getProductName());
                        detailDto.setImageName(product.getImageName());
                    }
                }
                detailDtos.add(detailDto);
            }
            historyDto.setDetails(detailDtos);
            historyList.add(historyDto);
        }

        // 7. 완성된 주문 내역 리스트를 반환
        return new ResponseEntity<>(historyList, HttpStatus.OK);
    }
}