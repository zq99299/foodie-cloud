package cn.mrcode.item.service;


import cn.mrcode.pojo.PagedGridResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

//@RequestMapping("item-comments-api")
@FeignClient(value = "foodie-item-service", path = "item-comments-api")
public interface ItemCommentsService {
    // 原来 MyCommentsService 类中与订单关联的两个服务都剔除掉
//    /**
//     * 根据订单id查询关联的商品
//     *
//     * @param orderId
//     * @return
//     */
//    List<OrderItems> queryPendingComment(String orderId);
//
//    /**
//     * 保存用户的评论
//     *
//     * @param orderId
//     * @param userId
//     * @param commentList
//     */
//    void saveComments(String orderId, String userId, List<OrderItemsCommentBO> commentList);


    /**
     * 我的评价查询 分页
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/myComments")
    PagedGridResult queryMyComments(
            @RequestParam("userId") String userId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize);

    /**
     * 新增的一个接口：专门给订单中心调用
     *
     * @param map
     */
    @PostMapping("/saveComments")
    void saveComments(@RequestBody Map<String, Object> map);
}
