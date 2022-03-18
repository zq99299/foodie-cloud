package cn.mrcode.order.service.impl.center;

import cn.mrcode.enums.YesOrNo;
import cn.mrcode.item.service.ItemCommentsService;
import cn.mrcode.order.api.center.MyCommentsService;
import cn.mrcode.order.mapper.OrderItemsMapper;
import cn.mrcode.order.mapper.OrderStatusMapper;
import cn.mrcode.order.mapper.OrdersMapper;
import cn.mrcode.order.pojo.OrderItems;
import cn.mrcode.order.pojo.OrderStatus;
import cn.mrcode.order.pojo.Orders;
import cn.mrcode.order.pojo.bo.center.OrderItemsCommentBO;
import cn.mrcode.service.BaseService;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
@RequestMapping("order-comments-api")
public class MyCommentsServiceImpl extends BaseService implements MyCommentsService {

    @Autowired
    public OrderItemsMapper orderItemsMapper;

    @Autowired
    public OrdersMapper ordersMapper;

    @Autowired
    public OrderStatusMapper orderStatusMapper;

    // 这里使用了 商品中心 的 mapper，需要替换成远程调用暴露出来的服务
    // @Autowired
    //public ItemsCommentsMapperCustom itemsCommentsMapperCustom;

    // 注入 FeignClient
    @Autowired
    private ItemCommentsService itemCommentsService;

    // 这里使用 eureka 的方式先进行调用
    // 后续学到 feign 的时候再缓存对方暴露出来的 service 方式
//    @Autowired
//    private LoadBalancerClient loadBalancerClient;
//    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private Sid sid;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<OrderItems> queryPendingComment(String orderId) {
        OrderItems query = new OrderItems();
        query.setOrderId(orderId);
        return orderItemsMapper.select(query);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveComments(String orderId, String userId,
                             List<OrderItemsCommentBO> commentList) {

        // 1. 保存评价 items_comments
        for (OrderItemsCommentBO oic : commentList) {
            oic.setCommentId(sid.nextShort());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("commentList", commentList);

        itemCommentsService.saveComments(map);
//        ServiceInstance instance = loadBalancerClient.choose("FOODIE-USER-SERVICE");
//        String target = String.format("http://%s:%s/item-comments-api/saveComments",
//                instance.getHost(), instance.getPort());
//        restTemplate.postForLocation(target, map);

        // 2. 修改订单表改已评价 orders
        Orders order = new Orders();
        order.setId(orderId);
        order.setIsComment(YesOrNo.YES.type);
        ordersMapper.updateByPrimaryKeySelective(order);

        // 3. 修改订单状态表的留言时间 order_status
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCommentTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
    }
}
