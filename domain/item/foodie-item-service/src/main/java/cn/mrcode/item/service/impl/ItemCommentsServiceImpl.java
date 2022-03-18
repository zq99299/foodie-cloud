package cn.mrcode.item.service.impl;

import cn.mrcode.item.mapper.ItemsCommentsMapperCustom;
import cn.mrcode.item.pojo.vo.MyCommentVO;
import cn.mrcode.item.service.ItemCommentsService;
import cn.mrcode.pojo.PagedGridResult;
import cn.mrcode.service.BaseService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mrcode
 * @date 2022/3/3 22:55
 */
@RestController
@Slf4j
@RequestMapping("item-comments-api")
public class ItemCommentsServiceImpl extends BaseService implements ItemCommentsService {
    @Resource
    private ItemsCommentsMapperCustom itemsCommentsMapperCustom;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult queryMyComments(String userId,
                                           Integer page,
                                           Integer pageSize) {

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);

        PageHelper.startPage(page, pageSize);
        List<MyCommentVO> list = itemsCommentsMapperCustom.queryMyComments(map);

        return setterPagedGrid(list, page);
    }

    @Override
    public void saveComments(Map<String, Object> map) {
        itemsCommentsMapperCustom.saveComments(map);
    }
}
