package com.goodskill.es.service.impl;

import com.goodskill.es.api.EsSearchService;
import com.goodskill.es.dto.GoodsDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class EsSearchServiceImpl implements EsSearchService {

    @Override
    public List<GoodsDto> searchWithNameByPage(String input) {
        log.info("searchWithNameByPage");
        // test dubbo service
        return new ArrayList<>();
    }
}
