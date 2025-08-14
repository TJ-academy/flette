package com.example.flette.api;

import com.example.flette.dto.IamportResponse;
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

@RestController
@RequestMapping("/api/orders")
public class OrdersApiController {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private IamportService iamportService;

    // 결제 요청 DTO (필요에 따라 필드 추가)
    public static class PaymentRequestDto {
        public String merchant_uid;
        public int amount;
        public String name;
        public String buyerName;
        public String buyerTel;
        // 기타 필요한 필드 추가
    }

    // 결제 검증 DTO
    public static class PaymentVerificationDto {
        public String imp_uid;
        public String merchant_uid;
    }

    // 아임포트 결제 사전 등록 API
    // 프론트엔드에서 결제창을 띄우기 전에 호출
    @PostMapping("/prepare")
    public ResponseEntity<?> preparePayment(@RequestBody PaymentRequestDto requestDto) {
        try {
            // 여기서는 merchant_uid를 프론트엔드에서 생성했다고 가정합니다.
            // 백엔드에서 생성하고 싶다면 UUID.randomUUID() 등을 사용할 수 있습니다.

            // 아임포트 API 토큰 발급
            String accessToken = iamportService.getAccessToken();
            if (accessToken == null) {
                return new ResponseEntity<>("아임포트 토큰 발급 실패", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // DB에 주문 정보를 미리 저장 (상태는 '결제대기' 등)
            Orders newOrder = new Orders();
            newOrder.setMerchantUid(requestDto.merchant_uid);
            newOrder.setTotalMoney(requestDto.amount); // 금액 저장
            newOrder.setStatus("결제대기"); // 초기 상태 설정
            // ... 다른 정보도 설정
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
            // 1. 아임포트 API 토큰 발급
            String accessToken = iamportService.getAccessToken();
            if (accessToken == null) {
                return new ResponseEntity<>("아임포트 토큰 발급 실패", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // 2. imp_uid로 아임포트 서버에서 실제 결제 정보 조회
            IamportResponse<PaymentInfo> paymentInfoResponse = iamportService.getPaymentInfo(accessToken, verifyDto.imp_uid);
            
            // 결제 정보가 존재하지 않거나, 응답 코드가 0이 아닌 경우 에러 처리
            if (paymentInfoResponse == null || paymentInfoResponse.getCode() != 0 || paymentInfoResponse.getResponse() == null) {
                return new ResponseEntity<>("아임포트 결제 정보 조회 실패", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            PaymentInfo paymentInfo = paymentInfoResponse.getResponse();
            int actualAmount = paymentInfo.getAmount(); // 실제 결제 금액
            String status = paymentInfo.getStatus(); // 결제 상태

            // 3. merchant_uid로 DB에 저장된 주문 금액 조회
            Optional<Orders> orderOptional = ordersRepository.findByMerchantUid(verifyDto.merchant_uid);

            if (orderOptional.isEmpty()) {
                // 이 경우, 사전 등록된 주문이 없으므로 결제 취소 등의 추가 로직이 필요
                return new ResponseEntity<>("DB에 존재하지 않는 주문 번호입니다.", HttpStatus.BAD_REQUEST);
            }
            
            Orders savedOrder = orderOptional.get();
            int savedAmount = savedOrder.getTotalMoney(); // DB에 저장된 금액

            // 4. 결제 금액 검증 및 상태 업데이트
            if (savedAmount == actualAmount && "paid".equals(status)) {
                savedOrder.setImpUid(verifyDto.imp_uid);
                savedOrder.setStatus("결제완료"); // 주문 상태 업데이트
                ordersRepository.save(savedOrder);
                return new ResponseEntity<>("결제 성공 및 DB 업데이트 완료", HttpStatus.OK);
            } else {
                // 결제 위변조 또는 실패한 경우
                // TODO: 결제 취소 로직을 여기에 추가할 수 있습니다.
                // iamportService.cancelPayment(accessToken, verifyDto.imp_uid);
                return new ResponseEntity<>("결제 위변조 또는 실패", HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("결제 검증 실패: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Orders>> getAllOrders() {
        // ordersRepository를 사용하여 Orders 테이블의 모든 데이터를 조회합니다.
        List<Orders> ordersList = ordersRepository.findAll();
        // 조회된 리스트를 성공 응답과 함께 반환합니다.
        return new ResponseEntity<>(ordersList, HttpStatus.OK);
    }

}
