package com.example.flette.api;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flette.dto.ProductDTO;
import com.example.flette.entity.Product;
import com.example.flette.repository.ProductRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/shop/*")
public class ShopApi {
	@Autowired
	ProductRepository pr;
	
	@Autowired
	ModelMapper modelMapper;
	
	@GetMapping
	public List<ProductDTO> list() {
		return pr.findAll().stream().map(product -> {
			ProductDTO dto = modelMapper.map(product, ProductDTO.class);
			dto.setProductId(product.getProductId());
			return dto;
		}).collect(Collectors.toList());
	}
	
	@GetMapping("{productId}")
	public Product detail(@PathVariable(name = "productId") Integer productId) {
		return pr.findById(productId).orElse(null);
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