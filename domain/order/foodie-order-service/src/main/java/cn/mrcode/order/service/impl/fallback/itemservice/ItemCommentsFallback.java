package cn.mrcode.order.service.impl.fallback.itemservice;

import cn.mrcode.item.pojo.vo.MyCommentVO;
import cn.mrcode.pojo.PagedGridResult;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author mrcode
 * @date 2022/3/21 20:57
 */
@Component
public class ItemCommentsFallback implements ItemCommentsFeignClient {
    @Override
    public PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize) {
        // 采用静默处理，响应默认值
        MyCommentVO myCommentVO = new MyCommentVO();
        myCommentVO.setContent("正在加载中...");

        PagedGridResult result = new PagedGridResult();
        result.setRows(Lists.newArrayList(myCommentVO));
        result.setTotal(1);
        result.setRecords(1);
        return result;
    }

    @Override
    public void saveComments(Map<String, Object> map) {

    }
}
