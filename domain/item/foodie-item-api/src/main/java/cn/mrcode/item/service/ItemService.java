package cn.mrcode.item.service;


import cn.mrcode.item.pojo.Items;
import cn.mrcode.item.pojo.ItemsImg;
import cn.mrcode.item.pojo.ItemsParam;
import cn.mrcode.item.pojo.ItemsSpec;
import cn.mrcode.item.pojo.vo.CommentLevelCountsVO;
import cn.mrcode.item.pojo.vo.ShopcartVO;
import cn.mrcode.pojo.PagedGridResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("item-api")
public interface ItemService {

    /**
     * 根据商品 ID 查询详情
     *
     * @param itemId
     * @return
     */
    @GetMapping("item")
    Items queryItemById(@RequestParam("itemId") String itemId);

    /**
     * 根据商品 id 查询商品图片列表
     *
     * @param itemId
     * @return
     */
    @GetMapping("itemImags")
    List<ItemsImg> queryItemImgList(@RequestParam("itemId") String itemId);

    /**
     * 根据商品 id 查询商品规格
     *
     * @param itemId
     * @return
     */
    @GetMapping("itemSpecs")
    List<ItemsSpec> queryItemSpecList(@RequestParam("itemId") String itemId);

    /**
     * 根据商品  id 查询商品参数
     *
     * @param itemId
     * @return
     */
    @GetMapping("itemParam")
    ItemsParam queryItemParam(@RequestParam("itemId") String itemId);

    /**
     * 根据商品id查询商品的评价等级数量
     *
     * @param itemId
     */
    @GetMapping("countComments")
    CommentLevelCountsVO queryCommentCounts(@RequestParam("itemId") String itemId);

    /**
     * 根据商品id查询商品的评价（分页）
     *
     * @param itemId
     * @param level
     * @return
     */
    @GetMapping("pageComments")
    PagedGridResult queryPagedComments(@RequestParam("itemId") String itemId,
                                       @RequestParam(value = "level", required = false) Integer level,
                                       @RequestParam(value = "page", required = false) Integer page,
                                       @RequestParam(value = "pageSize", required = false) Integer pageSize);
// 迁移到主搜模块
//    /**
//     * 搜索商品列表（分页）
//     *
//     * @param keywords 搜索词
//     * @param sort     排序
//     * @return
//     */
//    PagedGridResult searchItems(String keywords, String sort,
//                                Integer page, Integer pageSize);
//
//    /**
//     * 搜索商品列表，分类搜索（分页）
//     *
//     * @param catId 搜索词
//     * @param sort  排序
//     * @return
//     */
//    PagedGridResult searchItemsByThirdCat(Integer catId, String sort,
//                                          Integer page, Integer pageSize);

    /**
     * 根据规格 ids 查询最新的购物车中商品数据（用于刷新渲染购物车中的商品数据）
     *
     * @param specIds
     * @return
     */
    @GetMapping("getCartBySpecIds")
    List<ShopcartVO> queryItemsBySpecIds(@RequestParam("specIds") String specIds);

    /**
     * 根据商品规格 id 获取规格对象的具体信息
     *
     * @param specId
     * @return
     */
    @GetMapping("itemSpac")
    ItemsSpec queryItemSpecById(@RequestParam("specId") String specId);

    /**
     * 根据商品 id 获得商品图片主图 url
     *
     * @param itemId
     * @return
     */
    @GetMapping("primaryImage")
    String queryItemMainImgById(@RequestParam("itemId") String itemId);

    /**
     * 减少库存
     *
     * @param specId
     * @param buyCounts
     */
    @PostMapping("decreaseSpecStock")
    void decreaseItemSpecStock(@RequestParam("specId") String specId, @RequestParam("buyCounts") int buyCounts);
}
