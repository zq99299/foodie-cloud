package cn.mrcode.order.service.impl.fallback.itemservice;

import cn.mrcode.item.service.ItemCommentsService;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author mrcode
 * @date 2022/3/21 20:37
 */
@FeignClient(
        value = "foodie-item-service",
        path = "item-comments-api",
//        fallback = ItemCommentsFallback.class
        fallbackFactory = ItemCommentsFallbackFactory.class
)
public interface ItemCommentsFeignClient extends ItemCommentsService {
}
