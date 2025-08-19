package com.example.flette.api;

import com.example.flette.entity.Review;
import com.example.flette.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/all/reviews")
public class ReviewAllApi {

    @Autowired
    private ReviewRepository reviewRepository;

    // 모든 리뷰 조회 (페이지네이션 지원)
    @GetMapping
    public Page<Review> getAllReviews(
            @RequestParam(name = "page", defaultValue = "1") int page, 
            @RequestParam(name = "size", defaultValue = "8") int size) {
        
        return reviewRepository.findAll(PageRequest.of(page - 1, size));
    }
}
