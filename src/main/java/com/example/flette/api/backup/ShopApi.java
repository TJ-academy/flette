package com.example.flette.api.backup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.example.flette.dto.ProductDTO;
import com.example.flette.dto.QnADTO;
import com.example.flette.entity.Answer;
import com.example.flette.entity.Flower;
import com.example.flette.entity.Member;
import com.example.flette.entity.Product;
import com.example.flette.entity.Question;
import com.example.flette.entity.Review;
import com.example.flette.repository.AnswerRepository;
import com.example.flette.repository.FlowerRepository;
import com.example.flette.repository.MemberRepository;
import com.example.flette.repository.ProductRepository;
import com.example.flette.repository.QuestionRepository;
import com.example.flette.repository.ReviewRepository;
/*
@RestController
@RequestMapping("/api/shop")
*/
public class ShopApi {
	@Autowired
	ProductRepository pr;
	
	@Autowired
	FlowerRepository fr;
	
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
		return pr.findAll();
	}
	
	@GetMapping("/{productId}/detail")
	public Map<String, Object> detail(@PathVariable(name = "productId") Integer productId) {
		Optional<Product> opt = pr.findById(productId);
		Product p = opt.get();
		ProductDTO dto = new ProductDTO();
		dto.setProductId(p.getProductId());
		dto.setProductName(p.getProductName());
		dto.setImageName(p.getImageName());
		dto.setBasicPrice(p.getBasicPrice());
		dto.setDescription(p.getDescription());
		Map<String, Object> map = new HashMap<>();
		
		List<Flower> flowerList = fr.findAll();
		//System.out.println("리뷰 수 : " + rr.count());
		map.put("dto", dto);
		map.put("flowerList", flowerList);
		return map;
	}
	
	@GetMapping("/{productId}/review")
	public Map<String, Object> reviewList(@PathVariable(name = "productId") Integer productId) {
		Map<String, Object> map = new HashMap<>();
		List<Review> reviewList = rr.findByProductId(productId);
		//System.out.println("리뷰 수 : " + rr.count());
		map.put("rcount", rr.count());
		map.put("rlist", reviewList);
		return map;
	}
	
//	@GetMapping("/{productId}/qa")
//	public List<Question> qaList(@PathVariable(name = "productId") Integer productId) {
//		return qr.findByProductId(productId);
//	}
	
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
			@RequestBody Map<String, String> paswd) {
		Map<String, Object> map = new HashMap<>();
		try {
			Optional<Question> oq = qr.findById(questionId);
			Question q = oq.get();
            
			String passwd = paswd.get("passwd");
			
            // 비밀번호가 맞는지 확인 (암호화된 비밀번호와 비교)
            if (qr.checkPassword(questionId, passwd) == null) {
                map.put("message", "비밀번호가 일치하지 않습니다.");
            	return map;
            }
            
            // 비밀번호가 일치
            QnADTO dto = new QnADTO();
            dto.setQuestionId(q.getQuestionId());
			dto.setProductId(q.getProductId());
			dto.setUserid(q.getUserid());
			dto.setTitle(q.getTitle());
			dto.setContent(q.getContent());
			dto.setStatus(q.isStatus());
			dto.setQuestionDate(q.getQuestionDate());
			
			if(q.isStatus()) {
				// 🚨 수정: findByQuestionId -> findByQuestion_QuestionId
				Optional<Answer> aq = ar.findByQuestion_QuestionId(questionId);
                Answer a = aq.get();
    			dto.setAnswerId(a.getAnswerId());
    			dto.setAnswerContent(a.getAnswerContent());
    			dto.setAnswerDate(a.getAnswerDate());
			}
			
			map.put("dto", dto);
        } catch (Exception e) {
        	map.put("message", "비밀번호 확인을 실패했습니다.");
        }
		return map;
	}
}
