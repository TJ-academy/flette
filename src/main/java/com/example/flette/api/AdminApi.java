package com.example.flette.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.flette.dto.MemberDTO;
import com.example.flette.dto.QnaItemDTO;
import com.example.flette.entity.Answer;
import com.example.flette.entity.Bouquet;
import com.example.flette.entity.Flower;
import com.example.flette.entity.Member;
import com.example.flette.entity.OrderDetail;
import com.example.flette.entity.Orders;
import com.example.flette.entity.Product;
import com.example.flette.entity.Question;
import com.example.flette.repository.AnswerRepository;
import com.example.flette.repository.BouquetRepository;
import com.example.flette.repository.FlowerRepository;
import com.example.flette.repository.MemberRepository;
import com.example.flette.repository.OrderDetailRepository;
import com.example.flette.repository.OrdersRepository;
import com.example.flette.repository.ProductRepository;
import com.example.flette.repository.QuestionRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/api/admin")
public class AdminApi {

    @Autowired MemberRepository memberRepository;
    @Autowired QuestionRepository questionRepository;
    @Autowired AnswerRepository answerRepository;
    @Autowired OrdersRepository ordersRepository;
    @Autowired OrderDetailRepository orderDetailRepository;
    @Autowired ProductRepository productRepository;
    @Autowired BouquetRepository bouquetRepository;
    @Autowired FlowerRepository flowerRepository;

    /**
     * 회원 목록을 페이지별로 조회합니다. GET /api/admin/members?page=0&size=10
     *
     * @param page 조회할 페이지 번호 (0부터 시작)
     * @param size 한 페이지당 보여줄 회원 수
     * @return 페이지네이션된 회원 목록
     */
    @GetMapping("/members")
    public ResponseEntity<Page<MemberDTO>> getMembers(
            @PageableDefault(size = 10) Pageable pageable) {

        Page<Member> page = memberRepository.findAll(pageable);
        Page<MemberDTO> body = page.map(MemberDTO::from);
        return ResponseEntity.ok(body);
    }

    /**
     * 특정 회원을 삭제합니다. DELETE /api/admin/members/{userid}
     *
     * @param userid 삭제할 회원의 아이디
     * @return 삭제 성공 메시지
     */
    @DeleteMapping("/members/{userid}")
    public ResponseEntity<String> deleteMember(@PathVariable("userid") String userid) {
        if (memberRepository.existsById(userid)) {
            memberRepository.deleteById(userid);
            return ResponseEntity.ok("회원 삭제 성공");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /** Q&A 목록 (페이징) */
    @GetMapping("/qna")
    public ResponseEntity<Page<QnaItemDTO>> getQnaList(
            @PageableDefault(size = 10, sort = "questionDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(name = "unanswered", required = false, defaultValue = "false") Boolean unanswered) {
        
        Page<Question> page;
        if (Boolean.TRUE.equals(unanswered)) {
            // 미답변만 조회
            page = questionRepository.findByStatusOrderByQuestionDateDesc(false, pageable);
        } else {
            // 전체 조회 (답변 완료 / 미답변 모두)
            page = questionRepository.findAll(pageable);
        }

        Page<QnaItemDTO> body = page.map(q -> {
            Optional<Answer> ans = answerRepository.findByQuestion_QuestionId(q.getQuestionId());
            return QnaItemDTO.builder()
                    .questionId(q.getQuestionId())
                    .title(q.getTitle())
                    .writerMasked(mask(q.getUserid()))
                    .answered(ans.isPresent() || q.isStatus())
                    .questionDate(q.getQuestionDate())
                    .questionContent(q.getContent())
                    .answerContent(ans.map(Answer::getAnswerContent).orElse(null))
                    .answerDate(ans.map(Answer::getAnswerDate).orElse(null))
                    .build();
        });

        return ResponseEntity.ok(body);
    }

    /** Q&A 상세 */
    @GetMapping("/qna/{questionId}")
    public ResponseEntity<QnaItemDTO> getQna(@PathVariable("questionId") Integer questionId) {
        return questionRepository.findById(questionId)
                .map(q -> {
                    Optional<Answer> ans = answerRepository.findByQuestion_QuestionId(q.getQuestionId());
                    return ResponseEntity.ok(
                            QnaItemDTO.builder()
                                    .questionId(q.getQuestionId())
                                    .title(q.getTitle())
                                    .writerMasked(mask(q.getUserid()))
                                    .answered(ans.isPresent() || q.isStatus())
                                    .questionDate(q.getQuestionDate())
                                    .questionContent(q.getContent())
                                    .answerContent(ans.map(Answer::getAnswerContent).orElse(null))
                                    .answerDate(ans.map(Answer::getAnswerDate).orElse(null))
                                    .build()
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** 답변 등록 */
    @PostMapping("/qna/{questionId}/answer")
    public ResponseEntity<Answer> createAnswer(
            @PathVariable("questionId") Integer questionId, 
            @RequestBody Answer answerDto) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        Answer newAnswer = new Answer();
        newAnswer.setQuestion(question);
        newAnswer.setAnswerContent(answerDto.getAnswerContent());
        newAnswer.setAnswerDate(LocalDateTime.now()); // ✅ LocalDateTime 사용

        Answer savedAnswer = answerRepository.save(newAnswer);

        question.setStatus(true);
        questionRepository.save(question);

        return ResponseEntity.ok(savedAnswer);
    }

    /** 답변 수정 */
    @PutMapping("/qna/{questionId}/answer")
    @Transactional
    public ResponseEntity<QnaItemDTO> updateAnswer(
            @PathVariable("questionId") Integer questionId, 
            @RequestBody Answer req) {

        Optional<Answer> ansOpt = answerRepository.findByQuestion_QuestionId(questionId);
        if (ansOpt.isEmpty()) return ResponseEntity.notFound().build();

        Answer a = ansOpt.get();
        a.setAnswerContent(req.getAnswerContent());
        a.setAnswerDate(LocalDateTime.now()); // ✅ LocalDateTime 사용
        answerRepository.save(a);

        return getQna(questionId);
    }

    /** 답변 삭제 */
    @DeleteMapping("/qna/{questionId}/answer")
    @Transactional
    public ResponseEntity<Void> deleteAnswer(@PathVariable("questionId") Integer questionId) {
        Optional<Answer> ansOpt = answerRepository.findByQuestion_QuestionId(questionId);
        if (ansOpt.isPresent()) {
            answerRepository.delete(ansOpt.get());
            questionRepository.findById(questionId).ifPresent(q -> {
                q.setStatus(false);
                questionRepository.save(q);
            });
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }


    /** 작성자 마스킹 */
    private String mask(String uid) {
        if (uid == null || uid.length() < 3) return "****";
        return uid.substring(0, Math.min(3, uid.length())) + "****";
    }
    
 // ========================= 주문 목록 (검색+페이징) =========================
    @GetMapping("/orders")
    public Page<OrderSummaryDto> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));

        Specification<Orders> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            if (userid != null && !userid.isBlank()) {
                predicates.add(cb.equal(root.get("userid"), userid));
            }
            
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), java.sql.Date.valueOf(from)));
            }
            
            if (to != null) {
                predicates.add(cb.lessThan(root.get("orderDate"), java.sql.Date.valueOf(to.plusDays(1))));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 이 줄은 이제 정상적으로 작동합니다.
        return ordersRepository.findAll(spec, pageable).map(OrderSummaryDto::from);
    }

    // ========================= 주문 단건 조회 (헤더 + 라인아이템) =========================
    @GetMapping("/orders/{orderId}")
    public OrderViewDto getOrder(@PathVariable("orderId") Integer orderId) {
        Orders o = ordersRepository.findById(orderId).orElseThrow();
        List<OrderDetail> lines = orderDetailRepository.findAll(
                Example.of(new OrderDetail(null, orderId, null, null))
        );

        List<OrderLineDto> items = new ArrayList<>();
        for (OrderDetail d : lines) {
            // Bouquet / Product 정보까지 매핑 (엔티티 연관관계가 없어 수동 조인)
            String productName = null;
            Integer productId = null;
            Integer bouquetTotal = null;

            if (d.getBouquetCode() != null) {
                Bouquet b = bouquetRepository.findById(d.getBouquetCode()).orElse(null);
                if (b != null) {
                    bouquetTotal = b.getTotalMoney();
                    productId = b.getProductId();
                    if (productId != null) {
                        Product p = productRepository.findById(productId).orElse(null);
                        if (p != null) productName = p.getProductName();
                    }
                }
            }

            items.add(new OrderLineDto(
                    d.getDetailId(),
                    d.getOrderId(),
                    d.getBouquetCode(),
                    productId,
                    productName,
                    d.getMoney(),
                    bouquetTotal
            ));
        }

        return new OrderViewDto(OrderSummaryDto.from(o), items);
    }

    // ========================= 주문 상태 변경 =========================
    @PatchMapping("/orders/{orderId}/status")
    public OrderSummaryDto updateOrderStatus(
            @PathVariable("orderId") Integer orderId,
            @RequestBody UpdateStatusReq req
    ) {
        Orders o = ordersRepository.findById(orderId).orElseThrow();
        o.setStatus(req.getStatus());
        ordersRepository.save(o);
        return OrderSummaryDto.from(o);
    }

    // ========================= 주문 환불 사유 메모(간단 버전) =========================
    @PatchMapping("/orders/{orderId}/refund")
    public OrderSummaryDto setRefundReason(
            @PathVariable("orderId") Integer orderId,
            @RequestBody RefundReq req
    ) {
        Orders o = ordersRepository.findById(orderId).orElseThrow();
        o.setRefundReason(req.getReason());
        o.setStatus("REFUND_REQUESTED");
        ordersRepository.save(o);
        return OrderSummaryDto.from(o);
    }

    // ========================= 상품 CRUD (관리자) =========================
    @GetMapping("/products")
    public Page<Product> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return productRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "productId")));
    }

    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable("id") Integer id) {
        return productRepository.findById(id).orElseThrow();
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody Product p) {
        return productRepository.save(p);
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(@PathVariable("id") Integer id, @RequestBody Product p) {
        p.setProductId(id);
        return productRepository.save(p);
    }

    @DeleteMapping("/products/{id}")
    public void deleteProduct(@PathVariable("id") Integer id) {
        productRepository.deleteById(id);
    }

    // ========================= DTO / 요청 객체 =========================
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class OrderSummaryDto {
        private Integer orderId;
        private String userid;
        private String money;
        private Integer delivery;
        private Integer totalMoney;
        private String status;
        private LocalDateTime orderDate; // ✅ java.util.Date → LocalDateTime
        private String method;
        private String bank;

        static OrderSummaryDto from(Orders o) {
            return new OrderSummaryDto(
                    o.getOrderId(), 
                    o.getUserid(), 
                    o.getMoney(), 
                    o.getDelivery(),
                    o.getTotalMoney(), 
                    o.getStatus(), 
                    o.getOrderDate(),  // ✅ Orders에서 LocalDateTime을 그대로 가져옴
                    o.getMethod(), 
                    o.getBank()
            );
        }
    }


    @Data @AllArgsConstructor @NoArgsConstructor
    static class OrderLineDto {
        private Integer detailId;
        private Integer orderId;
        private Integer bouquetCode;
        private Integer productId;
        private String productName;
        private Integer money;
        private Integer bouquetTotal;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    static class OrderViewDto {
        private OrderSummaryDto order;
        private List<OrderLineDto> items;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    static class UpdateStatusReq { private String status; }

    @Data @AllArgsConstructor @NoArgsConstructor
    static class RefundReq { private String reason; }
    
// ====== 꽃(Flower) CRUD ======
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @GetMapping("/flowers")
    public Page<Flower> listFlowers(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestParam(name = "category", required = false) String category,
        @RequestParam(name = "show", required = false) Boolean show,
        @RequestParam(name = "q", required = false) String q
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "flowerId"));

        Specification<Flower> spec = (root, query, cb) -> {
            var ps = new ArrayList<Predicate>();

            if (category != null && !category.isBlank()) {
                ps.add(cb.equal(root.get("category"), category));
            }
            if (show != null) {
                ps.add(cb.equal(root.get("show"), show));
            }
            if (q != null && !q.isBlank()) {
                var like = "%" + q.trim() + "%";
                ps.add(cb.or(
                    cb.like(root.get("flowerName"), like),
                    cb.like(root.get("description"), like),
                    cb.like(root.get("story"), like)
                ));
            }
            return cb.and(ps.toArray(Predicate[]::new));
        };

        return flowerRepository.findAll(spec, pageable);
    }

    @GetMapping("/flowers/{id}")
    public Flower getFlower(@PathVariable("id") Integer id) {
        return flowerRepository.findById(id).orElseThrow();
    }

    @PostMapping("/flowers")
    public Flower createFlower(@ModelAttribute FlowerAdminForm form) throws IOException {
        Flower f = new Flower();
        f.setAddPrice(form.getAddPrice());
        f.setCategory(form.getCategory());
        f.setDescription(form.getDescription());
        f.setFlowerName(form.getFlowerName());
        f.setStory(form.getStory());
        f.setShow(Boolean.TRUE.equals(form.getShow()));

        if (form.getFile() != null && !form.getFile().isEmpty()) {
            f.setImageName(saveImage(form.getFile(), form.getCategory()));
        } else {
            f.setImageName("");
        }
        return flowerRepository.save(f);
    }

    @PutMapping("/flowers/{id}")
    public Flower updateFlower(@PathVariable("id") Integer id, @ModelAttribute FlowerAdminForm form) throws IOException {
        Flower f = flowerRepository.findById(id).orElseThrow();

        if (form.getAddPrice() != null) f.setAddPrice(form.getAddPrice());
        if (form.getCategory() != null) f.setCategory(form.getCategory());
        if (form.getDescription() != null) f.setDescription(form.getDescription());
        if (form.getFlowerName() != null) f.setFlowerName(form.getFlowerName());
        if (form.getStory() != null) f.setStory(form.getStory());
        if (form.getShow() != null) f.setShow(form.getShow());

        if (form.getFile() != null && !form.getFile().isEmpty()) {
            f.setImageName(saveImage(form.getFile(), f.getCategory()));
        }

        return flowerRepository.save(f);
    }

    // 이미지 저장
    private String saveImage(MultipartFile file, String category) throws IOException {
        String folder = getCategoryFolder(category);
        String fileName = Paths.get(file.getOriginalFilename()).getFileName().toString();
        
        Path uploadPath = Paths.get(uploadDir, folder);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        return fileName;
    }

    // 카테고리별 폴더 구분
    private String getCategoryFolder(String category) {
        switch (category) {
            case "서브":
                return "sub";
            case "잎사귀":
                return "foliage";
            default:
                return "main";
        }
    }

    @PatchMapping("/flowers/{id}/show")
    public Flower toggleFlowerShow(@PathVariable("id") Integer id, @RequestBody ShowReq req) {
        Flower f = flowerRepository.findById(id).orElseThrow();
        if (req.getShow() != null) {
            f.setShow(req.getShow());
        } else {
            f.setShow(!f.isShow());
        }
        return flowerRepository.save(f);
    }

    @DeleteMapping("/flowers/{id}")
    public void deleteFlower(@PathVariable("id") Integer id) {
        flowerRepository.deleteById(id);
    }

    // ====== 요청 DTO ======
    @Data @NoArgsConstructor @AllArgsConstructor
    static class FlowerAdminForm {
        private Integer addPrice;
        private String category;
        private String description;
        private String flowerName;
        private String story;
        private Boolean show;
        private MultipartFile file;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    static class ShowReq {
        private Boolean show;
    }
}
/*
status 옵션
- 입금확인중
- 취소완료
- 결제완료
- 환불완료
- 배송중
- 배송완료
- 구매확정

상태별 프론트 버튼
- 입금확인중 : (주문취소)
- 취소완료 : (X)
- 결제완료 : (환불요청)
- 환불완료 : (X)
- 배송중 : (배송조회)
- 배송완료 : (구매확정)
- 구매확정 : (리뷰작성)
*/
