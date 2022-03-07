package cn.mrcode.order.service.impl;

import cn.mrcode.enums.OrderStatusEnum;
import cn.mrcode.enums.YesOrNo;
import cn.mrcode.item.pojo.Items;
import cn.mrcode.item.pojo.ItemsSpec;
import cn.mrcode.order.api.OrderService;
import cn.mrcode.order.mapper.OrderItemsMapper;
import cn.mrcode.order.mapper.OrderStatusMapper;
import cn.mrcode.order.mapper.OrdersMapper;
import cn.mrcode.order.pojo.OrderItems;
import cn.mrcode.order.pojo.OrderStatus;
import cn.mrcode.order.pojo.Orders;
import cn.mrcode.order.pojo.bo.PlaceOrder;
import cn.mrcode.order.pojo.bo.ShopcartBO;
import cn.mrcode.order.pojo.bo.SubmitOrderBO;
import cn.mrcode.order.pojo.vo.MerchantOrdersVO;
import cn.mrcode.order.pojo.vo.OrderVO;
import cn.mrcode.user.pojo.UserAddress;
import cn.mrcode.utils.DateUtil;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author mrcode
 * @date 2021/2/16 20:11
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
public class OrderServiceImpl implements OrderService {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private Sid sid;
    // todo : 后续学习到 feign 再来使用 service 的方式
//    @Autowired
//    private AddressService addressService;
//    @Autowired
//    private ItemService itemService;
    @Autowired
    private LoadBalancerClient loadBalancerClient;
    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private OrderItemsMapper orderItemsMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public OrderVO createOrder(PlaceOrder placeOrder) {
        SubmitOrderBO submitOrderBO = placeOrder.getOrder();
        String userId = submitOrderBO.getUserId();
        String addressId = submitOrderBO.getAddressId();
        String itemSpecIds = submitOrderBO.getItemSpecIds();
        Integer payMethod = submitOrderBO.getPayMethod();
        String leftMsg = submitOrderBO.getLeftMsg();
        // 包邮费用设置为0
        Integer postAmount = 0;

        // 使用 sid 生成
        String orderId = sid.nextShort();

        // UserAddress address = addressService.queryUserAddres(userId, addressId);
        ServiceInstance instance = loadBalancerClient.choose("FOODIE-USER-SERVICE");
        String target = String.format("http://%s:%s/address-api/queryUserAddress" +
                        "?userId=%s&addressId=%s",
                instance.getHost(), instance.getPort(),
                userId, addressId);
        UserAddress address = restTemplate.getForObject(target, UserAddress.class);


        // 1. 新订单数据保存
        Orders newOrder = new Orders();
        newOrder.setId(orderId);
        newOrder.setUserId(userId);

        // 地址快照
        newOrder.setReceiverName(address.getReceiver());
        newOrder.setReceiverMobile(address.getMobile());
        newOrder.setReceiverAddress(address.getProvince() + " "
                + address.getCity() + " "
                + address.getDistrict() + " "
                + address.getDetail());

        // 在保存子订单后，计算出总价和真实价格后再填充
        //        newOrder.setTotalAmount();
        //        newOrder.setRealPayAmount();

        // 设置运费，真实的业务中，会动态设置的，要获取到
        newOrder.setPostAmount(postAmount);

        newOrder.setPayMethod(payMethod);
        newOrder.setLeftMsg(leftMsg); // 买家留言

        newOrder.setIsComment(YesOrNo.NO.type);
        newOrder.setIsDelete(YesOrNo.NO.type);
        newOrder.setCreatedTime(new Date());
        newOrder.setUpdatedTime(new Date());


        // 2. 循环根据 itemSpecIds 保存订单商品信息表
        String itemSpecIdArr[] = itemSpecIds.split(",");
        Integer totalAmount = 0;    // 商品原价累计
        Integer realPayAmount = 0;  // 优惠后的实际支付价格累计

        // 需要从购物车中清理的商品信息，该信息需要在 controller 中做
        List<ShopcartBO> toBeRemovedShopcatdList = new ArrayList<>();
        List<ShopcartBO> shopcartBOList = placeOrder.getItems();

        ServiceInstance itemInstance = loadBalancerClient.choose("FOODIE-ITEM-SERVICE");
        for (String itemSpecId : itemSpecIdArr) {

            // 整合 redis 后，商品购买的数量重新从 redis 的购物车中获取
            /*
              1. 从购物车中拿到对应的商品数量
              2. 将购买过的商品从购物车中删除
             */
            ShopcartBO shopcartBO = getBuyCountsFromShopcart(shopcartBOList, itemSpecId);
            // 添加到待清理的商品容器中
            toBeRemovedShopcatdList.add(shopcartBO);
            int buyCounts = shopcartBO.getBuyCounts();

            // 2.1 根据规格 id，查询规格的具体信息，主要获取价格
            //ItemsSpec itemSpec = itemService.queryItemSpecById(itemSpecId);

            String itemSpecTarget = String.format("http://%s:%s/item-api/itemSpac" +
                            "?specId=%s",
                    itemInstance.getHost(), itemInstance.getPort(),
                    itemSpecId);
            ItemsSpec itemSpec = restTemplate.getForObject(itemSpecTarget, ItemsSpec.class);

            totalAmount += itemSpec.getPriceNormal() * buyCounts;
            realPayAmount += itemSpec.getPriceDiscount() * buyCounts;

            // 2.2 根据商品 id，获得商品信息以及商品主图
            String itemId = itemSpec.getItemId();

            // Items item = itemService.queryItemById(itemId);
            String itemTarget = String.format("http://%s:%s/item-api/item" +
                            "?itemId=%s",
                    itemInstance.getHost(), itemInstance.getPort(),
                    itemId);
            Items item = restTemplate.getForObject(itemTarget, Items.class);

            // String imgUrl = itemService.queryItemMainImgById(itemId);
            String imgUrlTarget = String.format("http://%s:%s/item-api/primaryImage" +
                            "?itemId=%s",
                    itemInstance.getHost(), itemInstance.getPort(),
                    itemId);
            String imgUrl = restTemplate.getForObject(imgUrlTarget, String.class);

            // 2.3 循环保存子订单数据到数据库
            String subOrderId = sid.nextShort();
            OrderItems subOrderItem = new OrderItems();
            subOrderItem.setId(subOrderId);
            subOrderItem.setOrderId(orderId);
            subOrderItem.setItemId(itemId);
            subOrderItem.setItemName(item.getItemName());
            subOrderItem.setItemImg(imgUrl);
            subOrderItem.setBuyCounts(buyCounts);
            subOrderItem.setItemSpecId(itemSpecId);
            subOrderItem.setItemSpecName(itemSpec.getName());
            subOrderItem.setPrice(itemSpec.getPriceDiscount());
            orderItemsMapper.insert(subOrderItem);

            // 2.4 在用户提交订单以后，规格表中需要扣除库存
            //itemService.decreaseItemSpecStock(itemSpecId, buyCounts);
            String decreaseSpecStockTarget = String.format("http://%s:%s/item-api/decreaseSpecStock" +
                            "?specId=%s,buyCounts=%s",
                    itemInstance.getHost(), itemInstance.getPort(),
                    itemSpecId, buyCounts);
            restTemplate.postForLocation(decreaseSpecStockTarget, null);
        }

        newOrder.setTotalAmount(totalAmount);
        newOrder.setRealPayAmount(realPayAmount);
        ordersMapper.insert(newOrder);

        // 3. 保存订单状态表
        OrderStatus waitPayOrderStatus = new OrderStatus();
        waitPayOrderStatus.setOrderId(orderId);
        waitPayOrderStatus.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        waitPayOrderStatus.setCreatedTime(new Date());
        orderStatusMapper.insert(waitPayOrderStatus);

        // 4. 构建商户订单，用于传给支付中心
        MerchantOrdersVO merchantOrdersVO = new MerchantOrdersVO();
        merchantOrdersVO.setMerchantOrderId(orderId);
        merchantOrdersVO.setMerchantUserId(userId);
        merchantOrdersVO.setAmount(realPayAmount + postAmount);
        merchantOrdersVO.setPayMethod(payMethod);

        // 5. 构建自定义订单 vo
        OrderVO orderVO = new OrderVO();
        orderVO.setOrderId(orderId);
        orderVO.setMerchantOrdersVO(merchantOrdersVO);
        orderVO.setToBeRemovedShopcatdList(toBeRemovedShopcatdList);
        return orderVO;
    }

    /**
     * 从购物车中找到对应的商品
     *
     * @param shopcartList
     * @param specId
     * @return
     */
    private ShopcartBO getBuyCountsFromShopcart(List<ShopcartBO> shopcartList, String specId) {
        for (ShopcartBO cart : shopcartList) {
            if (cart.getSpecId().equals(specId)) {
                return cart;
            }
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateOrderStatus(String orderId, Integer orderStatus) {

        OrderStatus paidStatus = new OrderStatus();
        paidStatus.setOrderId(orderId);
        paidStatus.setOrderStatus(orderStatus);
        paidStatus.setPayTime(new Date());

        orderStatusMapper.updateByPrimaryKeySelective(paidStatus);
    }

    @Override
    public OrderStatus queryOrderStatusInfo(String orderId) {
        return orderStatusMapper.selectByPrimaryKey(orderId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void closeOrder() {

        // 查询所有未付款订单，判断时间是否超时（1天），超时则关闭交易
        OrderStatus queryOrder = new OrderStatus();
        queryOrder.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        List<OrderStatus> list = orderStatusMapper.select(queryOrder);
        for (OrderStatus os : list) {
            // 获得订单创建时间
            Date createdTime = os.getCreatedTime();
            // 和当前时间进行对比
            int days = DateUtil.daysBetween(createdTime, new Date());
            if (days >= 1) {
                // 超过1天，关闭订单
                doCloseOrder(os.getOrderId());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    void doCloseOrder(String orderId) {
        OrderStatus close = new OrderStatus();
        close.setOrderId(orderId);
        close.setOrderStatus(OrderStatusEnum.CLOSE.type);
        close.setCloseTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(close);
    }
}
