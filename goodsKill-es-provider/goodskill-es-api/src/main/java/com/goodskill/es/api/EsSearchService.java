package com.goodskill.es.api;

import com.goodskill.es.dto.GoodsDto;

import java.util.List;

/**
 * @author heng
 */
public interface EsSearchService {

    List<GoodsDto> searchWithNameByPage(String input);
}
