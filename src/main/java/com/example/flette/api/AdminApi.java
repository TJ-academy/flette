package com.example.flette.api;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

import jakarta.validation.Valid;
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

	    Page<Member> page = memberRepository.findAll(pageable); // ✅ pageable 그대로 사용
	    Page<MemberDTO> body = page.map(MemberDTO::from);       // DTO 변환
	    return ResponseEntity.ok(body);
	}

	/**
	 * 특정 회원을 삭제합니다. DELETE /api/admin/members/{userid}
	 *
	 * @param userid 삭제할 회원의 아이디
	 * @return 삭제 성공 메시지
	 */
	@DeleteMapping("/members/{userid}")
	public ResponseEntity<String> deleteMember(@PathVariable String userid) {
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
            @RequestParam(required = false, defaultValue = "false") boolean unanswered) {
        
        Page<Question> page;
        if (unanswered) {
            // 미답변만 조회
            page = questionRepository.findByStatusOrderByQuestionDateDesc(false, pageable);
        } else {
            // 전체 조회 (답변 완료 / 미답변 모두)
            page = questionRepository.findAll(pageable);
        }

        Page<QnaItemDTO> body = page.map(q -> {
            Optional<Answer> ans = answerRepository.findByQuestionId(q.getQuestionId());
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
    public ResponseEntity<QnaItemDTO> getQna(@PathVariable Integer questionId) {
        return questionRepository.findById(questionId)
                .map(q -> {
                    Optional<Answer> ans = answerRepository.findByQuestionId(q.getQuestionId());
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
    @Transactional
    public ResponseEntity<QnaItemDTO> createAnswer(@PathVariable Integer questionId, @RequestBody Answer req) {
        Question q = questionRepository.findById(questionId).orElse(null);
        if (q == null) return ResponseEntity.notFound().build();

        Answer a = new Answer();
        a.setQuestionId(questionId); // ⚠ qeustionId 오타 매핑 주의
        a.setAnswerContent(req.getAnswerContent());
        a.setAnswerDate(new java.util.Date());
        answerRepository.save(a);

        q.setStatus(true);
        questionRepository.save(q);

        return getQna(questionId);
    }

    /** 답변 수정 */
    @PutMapping("/qna/{questionId}/answer")
    @Transactional
    public ResponseEntity<QnaItemDTO> updateAnswer(@PathVariable Integer questionId, @RequestBody Answer req) {
        Optional<Answer> ansOpt = answerRepository.findByQuestionId(questionId);
        if (ansOpt.isEmpty()) return ResponseEntity.notFound().build();

        Answer a = ansOpt.get();
        a.setAnswerContent(req.getAnswerContent());
        a.setAnswerDate(new java.util.Date());
        answerRepository.save(a);

        return getQna(questionId);
    }

    /** 답변 삭제 */
    @DeleteMapping("/qna/{questionId}/answer")
    @Transactional
    public ResponseEntity<Void> deleteAnswer(@PathVariable Integer questionId) {
        Optional<Answer> ansOpt = answerRepository.findByQuestionId(questionId);
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
            List<Predicate> ps = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                ps.add(cb.equal(root.get("status"), status));
            }
            if (userid != null && !userid.isBlank()) {
                ps.add(cb.equal(root.get("userid"), userid));
            }
            if (from != null) {
                ps.add(cb.greaterThanOrEqualTo(root.<java.util.Date>get("orderDate"),
                        java.sql.Date.valueOf(from)));
            }
            if (to != null) {
                ps.add(cb.lessThan(root.<java.util.Date>get("orderDate"),
                        java.sql.Date.valueOf(to.plusDays(1))));
            }

            return cb.and(ps.toArray(new Predicate[0]));
        };

        Page<Orders> pageData = ordersRepository.findAll(spec, pageable);
        return pageData.map(OrderSummaryDto::from);
    }

    // ========================= 주문 단건 조회 (헤더 + 라인아이템) =========================
    @GetMapping("/orders/{orderId}")
    public OrderViewDto getOrder(@PathVariable Integer orderId) {
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
            @PathVariable Integer orderId,
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
            @PathVariable Integer orderId,
            @RequestBody RefundReq req
    ) {
        Orders o = ordersRepository.findById(orderId).orElseThrow();
        o.setRefundReason(req.getReason());
        o.setStatus("REFUND_REQUESTED"); // 정책에 맞게 상태값 사용
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
    public Product getProduct(@PathVariable Integer id) {
        return productRepository.findById(id).orElseThrow();
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody Product p) {
        return productRepository.save(p);
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(@PathVariable Integer id, @RequestBody Product p) {
        // 단순 치환 갱신
        p.setProductId(id);
        return productRepository.save(p);
    }

    @DeleteMapping("/products/{id}")
    public void deleteProduct(@PathVariable Integer id) {
        productRepository.deleteById(id);
    }

    // ========================= DTO / 요청 객체 =========================
    @Data @AllArgsConstructor @NoArgsConstructor
    static class OrderSummaryDto {
        private Integer orderId;
        private String userid;
        private Integer money;
        private Integer delivery;
        private Integer totalMoney;
        private String status;
        private java.util.Date orderDate;
        private String method;
        private String bank;

        static OrderSummaryDto from(Orders o) {
            return new OrderSummaryDto(
                    o.getOrderId(), o.getUserid(), o.getMoney(), o.getDelivery(),
                    o.getTotalMoney(), o.getStatus(), o.getOrderDate(),
                    o.getMethod(), o.getBank()
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
        private Integer money;          // 주문 당시 라인 금액
        private Integer bouquetTotal;   // 현재 부케 합계(참고용)
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
            var ps = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

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
            return cb.and(ps.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };

        return flowerRepository.findAll(spec, pageable);
    }

    @GetMapping("/flowers/{id}")
    public Flower getFlower(@PathVariable("id") Integer id) {
        return flowerRepository.findById(id).orElseThrow();
    }

    @PostMapping("/flowers")
    public Flower createFlower(@Valid @RequestBody FlowerCreateReq req) {
        Flower f = new Flower();
        f.setAddPrice(req.getAddPrice());
        f.setCategory(req.getCategory());
        f.setDescription(req.getDescription());
        f.setFlowerName(req.getFlowerName());
        f.setImageName(req.getImageName());
        f.setStory(req.getStory());
        f.setShow(Boolean.TRUE.equals(req.getShow())); // null이면 false
        return flowerRepository.save(f);
    }

    @PutMapping("/flowers/{id}")
    public Flower updateFlower(@PathVariable("id") Integer id, @RequestBody FlowerUpdateReq req) {
        Flower f = flowerRepository.findById(id).orElseThrow();
        if (req.getAddPrice() != null) f.setAddPrice(req.getAddPrice());
        if (req.getCategory() != null) f.setCategory(req.getCategory());
        if (req.getDescription() != null) f.setDescription(req.getDescription());
        if (req.getFlowerName() != null) f.setFlowerName(req.getFlowerName());
        if (req.getImageName() != null) f.setImageName(req.getImageName());
        if (req.getStory() != null) f.setStory(req.getStory());
        if (req.getShow() != null) f.setShow(req.getShow());
        return flowerRepository.save(f);
    }

    @PatchMapping("/flowers/{id}/show")
    public Flower toggleFlowerShow(@PathVariable("id") Integer id, @RequestBody ShowReq req) {
        Flower f = flowerRepository.findById(id).orElseThrow();
        if (req.getShow() != null) {
            f.setShow(req.getShow());
        } else {
            f.setShow(!f.isShow()); // 값 미지정 시 토글
        }
        return flowerRepository.save(f);
    }

    @DeleteMapping("/flowers/{id}")
    public void deleteFlower(@PathVariable("id") Integer id)  {
        flowerRepository.deleteById(id);
    }

    // ====== 요청 DTO ======
    @Data @NoArgsConstructor @AllArgsConstructor
    static class FlowerCreateReq {
        private Integer addPrice;
        private String category;
        private String description;
        private String flowerName;
        private String imageName;
        private String story;
        private Boolean show; // 생략 시 false로 저장
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    static class FlowerUpdateReq {
        private Integer addPrice;
        private String category;
        private String description;
        private String flowerName;
        private String imageName;
        private String story;
        private Boolean show;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    static class ShowReq {
        private Boolean show; // 없으면 토글
    }
}