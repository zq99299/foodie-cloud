package cn.mrcode.order.api.center;


import cn.mrcode.order.pojo.OrderItems;
import cn.mrcode.order.pojo.bo.center.OrderItemsCommentBO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("order-comments-api")
public interface MyCommentsService {

    /**
     * 根据订单id查询关联的商品
     *
     * @param orderId
     * @return
     */
    @GetMapping("/orderItems")
    List<OrderItems> queryPendingComment(@RequestParam("orderId") String orderId);

    /**
     * 保存用户的评论; 会调用到商品中心的一些服务
     *
     * @param orderId
     * @param userId
     * @param commentList
     */
    @PostMapping("/saveOrderComments")
    void saveComments(@RequestParam("orderId") String orderId,
                      @RequestParam("userId") String userId,
                      @RequestBody List<OrderItemsCommentBO> commentList);

// 已经迁移到商品中心了
    /**
     * 我的评价查询 分页
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
//    PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize);
}
