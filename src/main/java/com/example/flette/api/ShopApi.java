package com.example.flette.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import com.example.flette.dto.ProductDTO;
import com.example.flette.dto.QnADTO;
import com.example.flette.entity.Answer;
import com.example.flette.entity.Bouquet;
import com.example.flette.entity.Decoration;
import com.example.flette.entity.Flower;
import com.example.flette.entity.Product;
import com.example.flette.entity.Question;
import com.example.flette.entity.Review;
import com.example.flette.repository.AnswerRepository;
import com.example.flette.repository.BouquetRepository;
import com.example.flette.repository.DecorationRepository;
import com.example.flette.repository.FlowerRepository;
import com.example.flette.repository.MemberRepository;
import com.example.flette.repository.ProductRepository;
import com.example.flette.repository.QuestionRepository;
import com.example.flette.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import org.apache.commons.codec.digest.DigestUtils;


@RestController
@RequestMapping("/api/shop")
public class ShopApi {

    @Autowired
    private ProductRepository pr;

    @Autowired
    private FlowerRepository fr;

    @Autowired
    private DecorationRepository dr;

    @Autowired
    private BouquetRepository br;

    @Autowired
    private ReviewRepository rr;

    @Autowired
    private QuestionRepository qr;

    @Autowired
    private AnswerRepository ar;

    @Autowired
    private MemberRepository mr;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public List<Product> list() {
        return pr.findAll();
    }

    // 상품 상세
    @GetMapping("/{productId}/detail")
    public ResponseEntity<?> detail(@PathVariable Integer productId) {
        Optional<Product> opt = pr.findById(productId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "상품 없음", "productId", productId));
        }
        ProductDTO dto = modelMapper.map(opt.get(), ProductDTO.class);
        return ResponseEntity.ok(Map.of("dto", dto));  // ✅ dto key로 감싸서 반환
    }

    // 상품 정보 (부자재 포함)
    @GetMapping("/{productId}/info")
    public Map<String, Object> info(@PathVariable(name = "productId") Integer productId) {
        Optional<Product> opt = pr.findById(productId);

        if (opt.isEmpty()) {
            throw new RuntimeException("상품을 찾을 수 없습니다. productId=" + productId);
        }

        Product p = opt.get();
        ProductDTO dto = modelMapper.map(p, ProductDTO.class);

        Map<String, Object> map = new HashMap<>();
        map.put("dto", dto);

        // ✅ Decoration 관련된 부분은 제거

        return map;
    }

    // 리뷰
    @GetMapping("/{productId}/review")
    public Map<String, Object> reviewList(@PathVariable Integer productId) {
        List<Review> reviewList = rr.findByProductId(productId);
        Map<String, Object> map = new HashMap<>();
        map.put("rcount", reviewList.size());
        map.put("rlist", reviewList);
        return map;
    }

    // Q&A 리스트 (페이징)
    @GetMapping("/{productId}/qa")
    public Map<String, Object> qaList(@PathVariable Integer productId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("questionId").descending());
        Page<Question> questionPage = qr.findByProductId(productId, pageable);

        List<QnADTO> dtoList = questionPage.getContent().stream().map(q -> {
            QnADTO dto = new QnADTO();
            dto.setQuestionId(q.getQuestionId());
            dto.setProductId(q.getProductId());
            dto.setUserid(q.getUserid());
            dto.setTitle(q.getTitle());
            dto.setContent(q.getContent());
            dto.setStatus(q.isStatus());
            dto.setPasswd(q.getPasswd());
            dto.setQuestionDate(q.getQuestionDate());

            if (q.isStatus()) {
                ar.findByQuestion_QuestionId(q.getQuestionId())
                        .ifPresent(a -> {
                            dto.setAnswerId(a.getAnswerId());
                            dto.setAnswerContent(a.getAnswerContent());
                            dto.setAnswerDate(a.getAnswerDate());
                        });
            }
            return dto;
        }).toList();

        Map<String, Object> map = new HashMap<>();
        map.put("list", dtoList);
        map.put("currentPage", questionPage.getNumber());
        map.put("totalItems", questionPage.getTotalElements());
        map.put("totalPages", questionPage.getTotalPages());
        return map;
    }

    // Q&A 작성
 // Q&A 작성
    @PostMapping("/{productId}/qa/write")
    public void addQues(@RequestBody Question ques) {
        ques.setStatus(false);   // 답변 안 된 상태
        ques.setQuestionDate(LocalDateTime.now());
        
        // 비밀번호 암호화
        if (ques.getPasswd() != null && !ques.getPasswd().trim().isEmpty()) {
            ques.setPasswd(DigestUtils.sha256Hex(ques.getPasswd()));
        }

        qr.save(ques);   // ✅ JPA가 자동으로 INSERT
    }

    // Q&A 비밀번호 체크
    @PostMapping("/{productId}/qa/{questionId}/check")
    public Map<String, Object> checkPassword(@PathVariable Integer productId,
                                             @PathVariable Integer questionId,
                                             @RequestBody Map<String, String> paswd) {
        Map<String, Object> map = new HashMap<>();
        try {
            String passwd = paswd.get("passwd");
            if (qr.checkPassword(questionId, passwd).isEmpty()) {
                map.put("success", false);
                map.put("message", "비밀번호가 일치하지 않습니다.");
            } else {
                map.put("success", true);
                map.put("message", "비밀번호가 일치합니다.");
            }
        } catch (Exception e) {
            map.put("success", false);
            map.put("message", "비밀번호 확인을 실패했습니다.");
        }
        return map;
    }
}
