package com.example.flette.api;

import com.example.flette.dto.IamportResponse;
import com.example.flette.dto.OrderDetailDTO;
import com.example.flette.dto.OrderHistoryDTO;
import com.example.flette.dto.OrderRefundRequestDTO;
import com.example.flette.dto.PaymentInfo;
import com.example.flette.entity.Orders;
import com.example.flette.repository.OrdersRepository;
import com.example.flette.service.IamportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.flette.dto.OrderCancelInfoDTO;
import com.example.flette.entity.Flower;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import com.example.flette.entity.Bouquet;
import com.example.flette.entity.Decoration; // 추가
import com.example.flette.entity.OrderDetail;
import com.example.flette.entity.Product;
import com.example.flette.repository.BouquetRepository;
import com.example.flette.repository.DecorationRepository; // 추가
import com.example.flette.repository.FlowerRepository;
import com.example.flette.repository.OrderDetailRepository;
import com.example.flette.repository.ProductRepository;
import com.example.flette.repository.ReviewRepository;

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

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private FlowerRepository flowerRepository;
    @Autowired
    private DecorationRepository decorationRepository; // 추가


    //------결제 관련 로직------------------------------------------------
    public static class PaymentRequestDto {
        public String merchant_uid;
        public int amount;
        public String name;
        public String buyerName;
        public String buyerTel;
    }

    public static class PaymentVerificationDto {
        public String imp_uid;
        public String merchant_uid;
    }

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
     * 특정 사용자의 주문 내역을 상세 정보와 함께 조회하는 API. (주문 목록 페이지용)
     * @param userId 조회할 사용자의 ID
     * @return 주문 내역 리스트 (OrderHistoryDTO)
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

            List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getOrderId());
            List<OrderHistoryDTO.OrderHistoryDetailDTO> detailDtos = new ArrayList<>();
            for (OrderDetail detail : details) {
                OrderHistoryDTO.OrderHistoryDetailDTO detailDto = new OrderHistoryDTO.OrderHistoryDetailDTO();
                detailDto.setDetailId(detail.getDetailId());
                detailDto.setMoney(detail.getMoney());

                Optional<Bouquet> bouquetOpt = bouquetRepository.findById(detail.getBouquetCode());
                if (bouquetOpt.isPresent()) {
                    Bouquet bouquet = bouquetOpt.get();
                    Optional<Product> productOpt = productRepository.findById(bouquet.getProductId());
                    if (productOpt.isPresent()) {
                        Product product = productOpt.get();
                        detailDto.setProductName(product.getProductName());
                        detailDto.setImageName(product.getImageName());
                        detailDto.setBouquetCode(bouquet.getBouquetCode());
                    }
                }
                detailDto.setHasReview(reviewRepository.existsByBouquetCode(detail.getBouquetCode()));
                detailDtos.add(detailDto);
            }
            historyDto.setDetails(detailDtos);
            historyList.add(historyDto);
        }
        return new ResponseEntity<>(historyList, HttpStatus.OK);
    }

    /**
     * 특정 주문의 상세 정보를 조회하는 API. (주문 상세 페이지용)
     *
     * @param orderId 조회할 주문의 ID
     * @return 주문 상세 정보 (OrderDetailDTO)
     */
    @GetMapping("/{orderId}/detail")
    public ResponseEntity<?> getOrderDetail(@PathVariable("orderId") int orderId) {
        Optional<Orders> orderOpt = ordersRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return new ResponseEntity<>("주문 정보가 없습니다.", HttpStatus.NOT_FOUND);
        }
        Orders order = orderOpt.get();

        List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getOrderId());
        if (details.isEmpty()) {
            return new ResponseEntity<>("주문 상세 품목이 없습니다.", HttpStatus.NOT_FOUND);
        }

        OrderDetailDTO orderDetailDto = new OrderDetailDTO();
        orderDetailDto.setOrderId(order.getOrderId());
        orderDetailDto.setImpUid(order.getImpUid());
        orderDetailDto.setOrderDate(order.getOrderDate());
        orderDetailDto.setStatus(order.getStatus());
        orderDetailDto.setTotalMoney(order.getTotalMoney());
        orderDetailDto.setUserid(order.getUserid());
        orderDetailDto.setOrderAddress(order.getOrderAddress());

        List<OrderDetailDTO.ProductDetail> productDetails = new ArrayList<>();
        for (OrderDetail detail : details) {
            OrderDetailDTO.ProductDetail productDetail = new OrderDetailDTO.ProductDetail();
            productDetail.setDetailId(detail.getDetailId());
            productDetail.setMoney(detail.getMoney());

            Optional<Bouquet> bouquetOpt = bouquetRepository.findById(detail.getBouquetCode());
            if (bouquetOpt.isPresent()) {
                Bouquet bouquet = bouquetOpt.get();
                Optional<Product> productOpt = productRepository.findById(bouquet.getProductId());
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    productDetail.setProductName(product.getProductName());
                    productDetail.setImageName(product.getImageName());
                    productDetail.setBouquetCode(bouquet.getBouquetCode());
                }

                // 부케 구성 요소 상세 정보 추가
                List<OrderDetailDTO.BouquetComponent> components = new ArrayList<>();

                // Main Flowers
                addFlowerComponents(components, "MAIN", bouquet.getMainA(), bouquet.getMainB(), bouquet.getMainC());

                // Sub Flowers
                addFlowerComponents(components, "SUB", bouquet.getSubA(), bouquet.getSubB(), bouquet.getSubC());

                // Foliage Flowers
                addFlowerComponents(components, "FOLIAGE", bouquet.getLeafA(), bouquet.getLeafB(), bouquet.getLeafC());

                // Additional Items
                addDecorationComponents(components, "ADDITIONAL", bouquet.getAddA(), bouquet.getAddB(), bouquet.getAddC());

                // Wrapping
                addDecorationComponents(components, "WRAPPING", bouquet.getWrapping());
                
                productDetail.setComponents(components);
            }
            productDetail.setHasReview(reviewRepository.existsByBouquetCode(detail.getBouquetCode()));

            productDetails.add(productDetail);
        }
        orderDetailDto.setDetails(productDetails);

        return new ResponseEntity<>(orderDetailDto, HttpStatus.OK);
    }

    // Helper method to add flower components
    private void addFlowerComponents(List<OrderDetailDTO.BouquetComponent> components, String type, Integer... flowerIds) {
        for (Integer id : flowerIds) {
            if (id != null && id != 0) {
                flowerRepository.findById(id).ifPresent(flower -> {
                    OrderDetailDTO.BouquetComponent comp = new OrderDetailDTO.BouquetComponent();
                    comp.setType(type);
                    comp.setName(flower.getFlowerName());
                    comp.setAddPrice(flower.getAddPrice());
                    components.add(comp);
                });
            }
        }
    }

    // Helper method to add decoration components
    private void addDecorationComponents(List<OrderDetailDTO.BouquetComponent> components, String type, Integer... decorationIds) {
        for (Integer id : decorationIds) {
            if (id != null && id != 0) {
                decorationRepository.findById(id).ifPresent(decoration -> {
                    OrderDetailDTO.BouquetComponent comp = new OrderDetailDTO.BouquetComponent();
                    comp.setType(type);
                    comp.setName(decoration.getDecorationName());
                    comp.setAddPrice(decoration.getUtilPrice());
                    components.add(comp);
                });
            }
        }
    }


    //주문취소 api
    @PatchMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable("orderId") Integer orderId) {
        Optional<Orders> orderOpt = ordersRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return new ResponseEntity<>("주문 정보가 없습니다.", HttpStatus.NOT_FOUND);
        }

        Orders order = orderOpt.get();
        // 주문 상태가 "입금확인중"일 때만 취소 가능
        if ("입금확인중".equals(order.getStatus())) {
            order.setStatus("취소완료");
            ordersRepository.save(order);
            return new ResponseEntity<>("주문이 성공적으로 취소되었습니다.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("현재 상태에서는 주문을 취소할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
    }
    //주문취소완료페이지
    @GetMapping("/cancel/{orderId}/info")
    public ResponseEntity<?> getCancelInfo(@PathVariable("orderId") int orderId) {
        Optional<Orders> orderOpt = ordersRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return new ResponseEntity<>("주문 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        Orders order = orderOpt.get();

        // OrderDetail에서 BouquetCode 가져오기
        List<OrderDetail> details = orderDetailRepository.findByOrderId(orderId);
        if (details.isEmpty()) {
            return new ResponseEntity<>("주문 상세 품목이 없습니다.", HttpStatus.NOT_FOUND);
        }
        // OrderCancelInfoDTO에 담을 데이터 준비
        OrderCancelInfoDTO cancelInfoDTO = new OrderCancelInfoDTO();
        cancelInfoDTO.setTotalMoney(order.getTotalMoney());

        // 각 주문 상세 항목에 대해 필요한 정보 추출
        for (OrderDetail detail : details) {
            Optional<Bouquet> bouquetOpt = bouquetRepository.findById(detail.getBouquetCode());
            if (bouquetOpt.isPresent()) {
                Bouquet bouquet = bouquetOpt.get();

                // Product 정보 (상품명, 이미지)
                Optional<Product> productOpt = productRepository.findById(bouquet.getProductId());
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    cancelInfoDTO.setProductName(product.getProductName());
                    cancelInfoDTO.setImageName(product.getImageName());
                }

                // Flower 정보 (MAIN, SUB, FOLIAGE)
                cancelInfoDTO.setMainFlowers(getFlowerOptions(bouquet.getMainA(), bouquet.getMainB(), bouquet.getMainC()));
                cancelInfoDTO.setSubFlowers(getFlowerOptions(bouquet.getSubA(), bouquet.getSubB(), bouquet.getSubC()));
                cancelInfoDTO.setFoliageFlowers(getFlowerOptions(bouquet.getLeafA(), bouquet.getLeafB(), bouquet.getLeafC()));

            }
        }
        return new ResponseEntity<>(cancelInfoDTO, HttpStatus.OK);
    }
    /**
     * 여러 플라워 ID에 해당하는 Flower 이름을 조회하는 헬퍼 메서드
     */
    private List<OrderCancelInfoDTO.FlowerOption> getFlowerOptions(Integer id1, Integer id2, Integer id3) {
        List<OrderCancelInfoDTO.FlowerOption> options = new ArrayList<>();
        addFlowerName(options, id1);
        addFlowerName(options, id2);
        addFlowerName(options, id3);
        return options;
    }

    /**
     * 단일 플라워 ID에 해당하는 Flower 이름을 조회하여 리스트에 추가하는 헬퍼 메서드
     */
    private void addFlowerName(List<OrderCancelInfoDTO.FlowerOption> options, Integer flowerId) {
        if (flowerId != null && flowerId != 0) {
            flowerRepository.findById(flowerId).ifPresent(flower -> {
                OrderCancelInfoDTO.FlowerOption option = new OrderCancelInfoDTO.FlowerOption();
                option.setName(flower.getFlowerName());
                options.add(option);
            });
        }
    }
    /* 환불 메서드(환불페이지로 데이터 전송) 취소메서드 재사용*/
    @GetMapping("/refund/{orderId}/info")
    public ResponseEntity<?> getRefundInfo(@PathVariable("orderId") int orderId) {
        Optional<Orders> orderOpt = ordersRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return new ResponseEntity<>("주문 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
        Orders order = orderOpt.get();

        List<OrderDetail> details = orderDetailRepository.findByOrderId(orderId);
        if (details.isEmpty()) {
            return new ResponseEntity<>("주문 상세 품목이 없습니다.", HttpStatus.NOT_FOUND);
        }
        OrderCancelInfoDTO cancelInfoDTO = new OrderCancelInfoDTO();
        cancelInfoDTO.setTotalMoney(order.getTotalMoney());

        for (OrderDetail detail : details) {
            Optional<Bouquet> bouquetOpt = bouquetRepository.findById(detail.getBouquetCode());
            if (bouquetOpt.isPresent()) {
                Bouquet bouquet = bouquetOpt.get();
                Optional<Product> productOpt = productRepository.findById(bouquet.getProductId());
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    cancelInfoDTO.setProductName(product.getProductName());
                    cancelInfoDTO.setImageName(product.getImageName());
                }

                cancelInfoDTO.setMainFlowers(getFlowerOptions(bouquet.getMainA(), bouquet.getMainB(), bouquet.getMainC()));
                cancelInfoDTO.setSubFlowers(getFlowerOptions(bouquet.getSubA(), bouquet.getSubB(), bouquet.getSubC()));
                cancelInfoDTO.setFoliageFlowers(getFlowerOptions(bouquet.getLeafA(), bouquet.getLeafB(), bouquet.getLeafC()));
            }
        }
        return new ResponseEntity<>(cancelInfoDTO, HttpStatus.OK);
    }
    //환불요청 api
    @PatchMapping("/refund/{orderId}")
    public ResponseEntity<String> refundOrder(@PathVariable("orderId") Integer orderId,
                                            @RequestBody OrderRefundRequestDTO requestDto) {
        Optional<Orders> orderOpt = ordersRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return new ResponseEntity<>("주문 정보가 없습니다.", HttpStatus.NOT_FOUND);
        }

        Orders order = orderOpt.get();
        // 주문 상태가 "결제완료"일 때만 환불 가능하도록 로직 유지
        if ("결제완료".equals(order.getStatus())) {
            // DTO에서 받은 정보로 Orders 엔티티 업데이트
            order.setRefundReason(requestDto.getRefundReason());
            order.setAccount(requestDto.getAccount());
            order.setBank(requestDto.getBank());
            // 상태를 "환불요청"으로 변경
            order.setStatus("환불요청");
            ordersRepository.save(order);
            return new ResponseEntity<>("환불 요청이 성공적으로 접수되었습니다.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("현재 상태에서는 환불 신청이 불가합니다.", HttpStatus.BAD_REQUEST);
        }
    }
    //주문확정 api
    @PatchMapping("/confirm/{orderId}")
    public ResponseEntity<String> confirmOrder(@PathVariable("orderId") Integer orderId) {
        Optional<Orders> orderOpt = ordersRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return new ResponseEntity<>("주문 정보가 없습니다.", HttpStatus.NOT_FOUND);
        }

        Orders order = orderOpt.get();
        // 주문 상태가 "배송완료"일 때만 확정 가능
        if ("배송완료".equals(order.getStatus())) {
            order.setStatus("구매확정");
            ordersRepository.save(order);
            return new ResponseEntity<>("구매가 확정되었습니다.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("현재 상태에서는 구매 확정이 불가합니다.", HttpStatus.BAD_REQUEST);
        }
    }
}
