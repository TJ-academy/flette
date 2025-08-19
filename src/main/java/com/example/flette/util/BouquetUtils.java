package com.example.flette.util;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.flette.dto.BouquetInfoDTO;
import com.example.flette.entity.Bouquet;
import com.example.flette.repository.DecorationRepository;
import com.example.flette.repository.FlowerRepository;

@Component
public class BouquetUtils {
	@Autowired
    private FlowerRepository flowerRepo;

    @Autowired
    private DecorationRepository decoRepo;
    
	public List<BouquetInfoDTO> extractBouquetInfo(Bouquet bouquet) {
		List<BouquetInfoDTO> list = new ArrayList<>();

        Map<String, List<Integer>> flowerMap = Map.of(
            "MAIN", List.of(bouquet.getMainA(), bouquet.getMainB(), bouquet.getMainC()),
            "SUB", List.of(bouquet.getSubA(), bouquet.getSubB(), bouquet.getSubC()),
            "FOLIAGE", List.of(bouquet.getLeafA(), bouquet.getLeafB(), bouquet.getLeafC())
        );

        for (String category : flowerMap.keySet()) {
            for (Integer id : flowerMap.get(category)) {
                if (id != null && id > 0) {
                    flowerRepo.findById(id).ifPresent(flower ->
                        list.add(new BouquetInfoDTO(category, flower.getFlowerName(), flower.getAddPrice()))
                    );
                }
            }
        }

        // 포장지
        if (bouquet.getWrapping() != null && bouquet.getWrapping() > 0) {
            decoRepo.findById(bouquet.getWrapping()).ifPresent(deco ->
                list.add(new BouquetInfoDTO("WRAPPING", deco.getDecorationName(), deco.getUtilPrice()))
            );
        }

        // 기타 추가
        List<Integer> addItems = List.of(bouquet.getAddA(), bouquet.getAddB(), bouquet.getAddC());
        for (Integer id : addItems) {
            if (id != null && id > 0) {
                decoRepo.findById(id).ifPresent(deco ->
                    list.add(new BouquetInfoDTO("ADDITIONAL", deco.getDecorationName(), deco.getUtilPrice()))
                );
            }
        }

        return list;
    }
}