package cn.mrcode.order.pojo.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * 下单
 *
 * @author mrcode
 * @date 2022/3/6 21:04
 */
@Data
@ToString
@NoArgsConstructor // 无参构造器
@AllArgsConstructor // 有参构造器
public class PlaceOrder {
    SubmitOrderBO order;
    List<ShopcartBO> items;
}
