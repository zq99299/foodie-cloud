package cn.mrcode.order.api;


import cn.mrcode.order.pojo.OrderStatus;
import cn.mrcode.order.pojo.bo.PlaceOrder;
import cn.mrcode.order.pojo.vo.OrderVO;
import org.springframework.web.bind.annotation.*;

/**
 * @author mrcode
 * @date 2021/2/16 20:10
 */
@RequestMapping("order-api")
public interface OrderService {
    /**
     * 用于创建订单相关信息
     */
    @PostMapping("/placeOrder")
    OrderVO createOrder(@RequestBody PlaceOrder placeOrder);

    /**
     * 修改订单状态
     *
     * @param orderId
     * @param orderStatus
     */
    @PutMapping("/updateOrderStatus")
    void updateOrderStatus(@RequestParam("orderId") String orderId,
                           @RequestParam("orderStatus") Integer orderStatus);

    /**
     * 查询订单状态
     *
     * @param orderId
     * @return
     */
    @GetMapping("/orderStatus")
    OrderStatus queryOrderStatusInfo(@RequestParam("orderId") String orderId);

    /**
     * 关闭超时未支付订单
     */
    @PutMapping("/closePendingOrder")
    void closeOrder();
}
