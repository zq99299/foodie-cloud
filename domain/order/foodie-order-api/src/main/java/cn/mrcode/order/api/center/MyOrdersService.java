package cn.mrcode.order.api.center;

import cn.mrcode.order.pojo.Orders;
import cn.mrcode.order.pojo.vo.OrderStatusCountsVO;
import cn.mrcode.pojo.JSONResult;
import cn.mrcode.pojo.PagedGridResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

//@RequestMapping("my-order-api")
@FeignClient(value = "foodie-order-service", path = "my-order-api")
public interface MyOrdersService {

    /**
     * 查询我的订单列表
     *
     * @param userId
     * @param orderStatus
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/order/query")
    PagedGridResult queryMyOrders(@RequestParam("userId") String userId,
                                  @RequestParam("orderStatus") Integer orderStatus,
                                  @RequestParam(value = "page", required = false) Integer page,
                                  @RequestParam(value = "pageSize", required = false) Integer pageSize);

    /**
     * @Description: 订单状态 --> 商家发货
     */
    @PostMapping("/order/delivered")
    void updateDeliverOrderStatus(@RequestParam("orderId") String orderId);

    /**
     * 查询我的订单
     *
     * @param userId
     * @param orderId
     * @return
     */
    @GetMapping("/order/details")
    Orders queryMyOrder(@RequestParam("userId") String userId,
                        @RequestParam("orderId") String orderId);

    /**
     * 更新订单状态 —> 确认收货
     *
     * @return
     */
    @PostMapping("/order/receive")
    boolean updateReceiveOrderStatus(@RequestParam("orderId") String orderId);

    /**
     * 删除订单（逻辑删除）
     *
     * @param userId
     * @param orderId
     * @return
     */
    @DeleteMapping("/order")
    boolean deleteOrder(@RequestParam("userId") String userId,
                        @RequestParam("orderId") String orderId);

    /**
     * 查询用户订单数
     *
     * @param userId
     */
    @GetMapping("/order/counts")
    OrderStatusCountsVO getOrderStatusCounts(@RequestParam("userId") String userId);

    /**
     * 获得分页的订单动向
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/order/trend")
    PagedGridResult getOrdersTrend(@RequestParam("userId") String userId,
                                   @RequestParam(value = "page", required = false) Integer page,
                                   @RequestParam(value = "pageSize", required = false) Integer pageSize);

    /**
     * 验证用户和订单是否有关联关系，避免非法用户调用
     *
     * @param userId
     * @param orderId
     * @return
     */
    @GetMapping("/checkUserOder")
    JSONResult checkUserOder(@RequestParam("userId") String userId,
                             @RequestParam("orderId") String orderId);
}