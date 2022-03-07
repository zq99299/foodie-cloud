package cn.mrcode.controller;

import org.springframework.stereotype.Controller;

@Controller
public class BaseController {
    public static final Integer COMMON_PAGE_SIZE = 10;
    public static final Integer PAGE_SIZE = 20;
    public static final String FOODIE_SHOPCART = "shopcart";
    public static final String REDIS_USER_TOKEN = "redis_user_token";

    // 支付中心的调用地址
    public String paymentUrl = "http://payment.t.mukewang.com/foodie-payment/payment/createMerchantOrder";        // produce

    // 微信支付成功 -> 支付中心 -> 天天吃货平台
    //                       |-> 回调通知的url
    public String payReturnUrl = "http://localhost:8088/orders/notifyMerchantOrderPaid";
}
