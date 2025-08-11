package com.example.flette.api;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flette.entity.Flower;
import com.example.flette.repository.FlowerRepository;

@RestController
@RequestMapping("/api/flower")
public class FlowerApi {

    @Autowired
    FlowerRepository flowerRepository;

    @Autowired
    ModelMapper modelMapper;

    @GetMapping("/list")
    public List<Flower> getFlowerList() {
        return flowerRepository.findAll();
    }

    @GetMapping("/detail/{flowerId}")
    public Optional<Flower> getFlowerDetail(@PathVariable("flowerId") int flowerId) {
        return flowerRepository.findById(flowerId);
    }
}