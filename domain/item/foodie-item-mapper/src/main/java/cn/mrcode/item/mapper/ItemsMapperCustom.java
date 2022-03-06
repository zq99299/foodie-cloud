package cn.mrcode.item.mapper;

import cn.mrcode.item.pojo.vo.ItemCommentVO;
import cn.mrcode.item.pojo.vo.ShopcartVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author mrcode
 * @date 2021/2/15 15:40
 */
public interface ItemsMapperCustom {
    List<ItemCommentVO> queryItemComments(@Param("paramsMap") Map<String, Object> map);

    // 迁移到 foodie-search 模块
//    List<SearchItemsVO> searchItems(@Param("paramsMap") Map<String, Object> map);
//    List<SearchItemsVO> searchItemsByThirdCat(@Param("paramsMap") Map<String, Object> map);

    List<ShopcartVO> queryItemsBySpecIds(@Param("paramsList") List specIdsList);

    int decreaseItemSpecStock(@Param("specId") String specId,
                              @Param("pendingCounts") int pendingCounts);
}
