package com.example.flette.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.example.flette.dto.ProductDTO;
import com.example.flette.entity.Flower;
import com.example.flette.entity.Product;
import com.example.flette.entity.Review;
import com.example.flette.repository.FlowerRepository;
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
	ReviewRepository rr;
	
	@Autowired
	QuestionRepository qr;
	
	@Autowired
	ModelMapper modelMapper;
	
	@GetMapping
	public List<Product> list() {
		System.out.println("list:"+pr.findAll());
		return pr.findAll();
	}
	
	@GetMapping("/{productId}")
	public Map<String, Object> detail(@PathVariable(name = "productId") Integer productId, 
			ModelAndView mav) {
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
		List<Review> reviewList = rr.findAll();
		//System.out.println("리뷰 수 : " + rr.count());
		map.put("dto", dto);
		map.put("flowerList", flowerList);
		map.put("reviewCount", rr.count());
		map.put("reviewList", reviewList);
		return map;
	}
	
//	@PostMapping("insert")
//	public void insert(ProductDTO dto, HttpServletRequest request) {
//		String filename = "-";
//		if(dto.getImg() != null && !dto.getImg().isEmpty()) {
//			filename = dto.getImg().getOriginalFilename();
//			try {
//				String path = request.getSession().getServletContext().getRealPath("/images/");
//				new File(path).mkdir();
//				dto.getImg().transferTo(new File(path + filename));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		dto.setFilename(filename);
//		Product p = modelMapper.map(dto, Product.class);
//		pr.save(p);
//	}
//	
//	
//	
//	@PostMapping("update")
//	public void update(ProductDTO dto, 
//			HttpServletRequest request) {
//		String filename = "-";
//		if(dto.getImg() != null && !dto.getImg().isEmpty()) {
//			filename = dto.getImg().getOriginalFilename();
//			try {
//				String path = request.getSession().getServletContext().getRealPath("/images/");
//				new File(path).mkdir();
//				dto.getImg().transferTo(new File(path + filename));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			dto.setFilename(filename);
//		} else {
//			Optional<Product> opt = pr.findById(dto.getProductCode());
//			Product dto2 = opt.get();
//			dto.setFilename(dto2.getFilename());
//		}
//		Product p = modelMapper.map(dto, Product.class);
//		pr.save(p);
//	}
//	
//	@GetMapping("delete/{productCode}")
//	public void delete(@PathVariable(name = "productCode") long productCode, 
//			HttpServletRequest request) {
//		Optional<Product> opt = pr.findById(productCode);
//		Product dto = opt.get();
//		String filename = dto.getFilename();
//		if(filename != null && !filename.equals("-")) {
//			String path = request.getSession().getServletContext().getRealPath("/images/");
//			File f = new File(path + filename);
//			if(f.exists()) {
//				f.delete();
//			}
//		}
//		pr.deleteById(productCode);
//	}
}