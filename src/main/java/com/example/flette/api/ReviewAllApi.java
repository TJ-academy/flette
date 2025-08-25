package com.example.flette.api;

import com.example.flette.entity.Review;
import com.example.flette.repository.ReviewRepository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/all/reviews")
public class ReviewAllApi {

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping
    public Map<String, Object> getAllReviews(
            @RequestParam(name = "page", defaultValue = "1") int page, 
            @RequestParam(name = "size", defaultValue = "8") int size) {
    	Map<String, Object> map = new HashMap<>();
    	Page<Review> fdate = reviewRepository.findByReviewImageIsNotNullOrderByReviewDateDesc(PageRequest.of(page - 1, size));
    	Page<Review> fluv = reviewRepository.findByReviewImageIsNotNullOrderByLuvDesc(PageRequest.of(page - 1, size));
    	Page<Review> fscore = reviewRepository.findByReviewImageIsNotNullOrderByScoreDesc(PageRequest.of(page - 1, size));
    	
    	map.put("fdate", fdate);
    	map.put("fluv", fluv);
    	map.put("fscore", fscore);
    	//System.out.println("map: " + map);
        return map;
    }
}
