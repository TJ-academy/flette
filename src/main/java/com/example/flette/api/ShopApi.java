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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flette.dto.ProductDTO;
import com.example.flette.dto.QnADTO;
import com.example.flette.entity.Answer;
import com.example.flette.entity.Bouquet;
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

@RestController
@RequestMapping("/api/shop")
public class ShopApi {
	@Autowired
	ProductRepository pr;
	
	@Autowired
	FlowerRepository fr;
	
	@Autowired
	DecorationRepository dr;
	
	@Autowired
	BouquetRepository br;
	
	@Autowired
	ReviewRepository rr;
	
	@Autowired
	QuestionRepository qr;
	
	@Autowired
	AnswerRepository ar;
	
	@Autowired
	MemberRepository mr;
	
	@Autowired
	ModelMapper modelMapper;
	
	@GetMapping
	public List<Product> list() {
		System.out.println(pr.findAll());
		return pr.findAll();
	}
	
	@GetMapping("/{productId}/detail")
	public Map<String, Object> detail(@PathVariable(name = "productId") Integer productId) {
		Optional<Product> opt = pr.findById(productId);
		Product p = opt.get();
		ProductDTO dto = modelMapper.map(p, ProductDTO.class);
		
		Map<String, Object> map = new HashMap<>();
		map.put("dto", dto);
		return map;
	}
	
	@GetMapping("/{productId}/info")
	public Map<String, Object> info(@PathVariable(name = "productId") Integer productId) {
		Optional<Product> opt = pr.findById(productId);
		
		Product p = opt.get();
		ProductDTO dto = modelMapper.map(p, ProductDTO.class);
		
		Map<String, Object> map = new HashMap<>();
		
		map.put("dto", dto);
		map.put("mfl", fr.findByCategoryAndShowTrue("메인"));
		map.put("sfl", fr.findByCategoryAndShowTrue("서브"));
		map.put("ffl", fr.findByCategoryAndShowTrue("잎사귀"));
		
		map.put("wdl", dr.findByCategoryAndShowTrue("포장지"));
		map.put("adl", dr.findByCategoryAndShowTrue("기타"));
		return map;
	}
	
	@PostMapping("/{productId}/bouquet/insert")
	public Map<String, Object> bouquetInsert(@PathVariable(name = "productId") Integer productId, 
			@RequestBody Bouquet bouquet) {
		Map<String, Object> map = new HashMap<>();
		try {
			br.save(bouquet);
			map.put("success", true);
			map.put("message", "저장 성공");
			map.put("bouquetCode", bouquet.getBouquetCode());
			map.put("totalMoney", bouquet.getTotalMoney());
		} catch(Exception e) {
			e.printStackTrace();
			map.put("success", false);
			map.put("message", "에러 발생: " + e.getMessage());
		}
		return map;
	}
	
	@GetMapping("/{productId}/review")
	public Map<String, Object> reviewList(@PathVariable(name = "productId") Integer productId) {
		Map<String, Object> map = new HashMap<>();
		List<Review> reviewList = rr.findByProductId(productId);
		//System.out.println("리뷰 수 : " + rr.count());
		map.put("rcount", reviewList.size());
		map.put("rlist", reviewList);
		return map;
	}
	
	@PostMapping("/{productId}/review/{id}/like")
	public void reviewLuv(@PathVariable(name = "productId") Integer productId,
			@PathVariable(name = "id") Integer reviewId) {
		Optional<Review> optr = rr.findById(reviewId);
		Review ret = optr.get();
		ret.setLuv(ret.getLuv() + 1);
		rr.save(ret);
	}
	
	@GetMapping("/{productId}/qa")
	public Map<String, Object> qaList(@PathVariable(name = "productId") Integer productId, 
			@RequestParam(name = "page",defaultValue = "0") int page, 
			@RequestParam(name = "size", defaultValue = "10") int size) {
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

           // 답변이 있는 경우만 추가
           if (q.isStatus()) {
               Optional<Answer> answerOpt = ar.findByQuestion_QuestionId(q.getQuestionId());
               answerOpt.ifPresent(a -> {
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
   
   @PostMapping("/{productId}/qa/write")
   public void addQues(@RequestBody Question ques) {
      String passwd = ques.getPasswd();
       if (passwd == null || passwd.trim().isEmpty()) {
           passwd = null;
       }
       
      qr.addQues(
            ques.getProductId(), 
            ques.getUserid(), 
            ques.getTitle(), 
            ques.getContent(), 
            passwd);
   }
   
   @PostMapping("/{productId}/qa/{questionId}/check")
   public Map<String, Object> checkPassword(@PathVariable("questionId") Integer questionId, 
         @RequestBody Map<String, String> paswd, 
         @PathVariable("productId") Integer productId) {
      Map<String, Object> map = new HashMap<>();
      try {
         String passwd = paswd.get("passwd");
         
            // 비밀번호가 맞는지 확인 (암호화된 비밀번호와 비교)
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
      //System.out.println(map);
      return map;
   }
}