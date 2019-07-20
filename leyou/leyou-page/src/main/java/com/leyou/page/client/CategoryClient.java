package com.leyou.page.client;

import com.leyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "leyou-item-service")
public interface CategoryClient extends CategoryApi {
}
