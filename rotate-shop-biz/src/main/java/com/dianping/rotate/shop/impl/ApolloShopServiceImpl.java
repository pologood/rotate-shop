package com.dianping.rotate.shop.impl;

import com.dianping.rotate.shop.api.ApolloShopService;
import com.dianping.rotate.shop.dao.ApolloShopDAO;
import com.dianping.rotate.shop.dao.ApolloShopExtendDAO;
import com.dianping.rotate.shop.dao.ShopCategoryDAO;
import com.dianping.rotate.shop.dao.ShopRegionDAO;
import com.dianping.rotate.shop.dto.ApolloShopDTO;
import com.dianping.rotate.shop.entity.ApolloShopEntity;
import com.dianping.rotate.shop.entity.ApolloShopExtendEntity;
import com.dianping.rotate.shop.entity.ShopCategoryEntity;
import com.dianping.rotate.shop.entity.ShopRegionEntity;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by luoming on 15/1/6.
 */

@Service("apolloShopService")
public class ApolloShopServiceImpl implements ApolloShopService {

    @Autowired
    private ApolloShopDAO apolloShopDAO;

    @Autowired
    private ApolloShopExtendDAO apolloShopExtendDAO;

    @Autowired
    private ShopRegionDAO shopRegionDAO;

    @Autowired
    private ShopCategoryDAO shopCategoryDAO;

    @Override
    public ApolloShopDTO getApolloShop(int shopID, int bizID) {
        ApolloShopDTO apolloShopDTO = new ApolloShopDTO();
        processShopExtend(apolloShopDTO, shopID, bizID);
        processShop(apolloShopDTO, shopID);
        processRegion(apolloShopDTO, shopID);
        processCategory(apolloShopDTO, shopID);
        return apolloShopDTO;
    }

    @Override
    public void deleteApolloShopByShopID(int shopId) {
        apolloShopDAO.deleteApolloShopByShopID(shopId);
    }

    @Override
    public void restoreApolloShopByShopID(int shopId) {
        apolloShopDAO.restoreApolloShopByShopID(shopId);
    }

    private void processShopExtend(ApolloShopDTO apolloShopDTO, int shopID, int bizID) {
        List<ApolloShopExtendEntity> apolloShopExtendList = apolloShopExtendDAO.queryApolloShopExtendByShopIDAndBizID(shopID, bizID);
        if(CollectionUtils.isNotEmpty(apolloShopExtendList)) {
            ApolloShopExtendEntity apolloShopExtend = apolloShopExtendList.get(0);
            apolloShopDTO.setBizID(apolloShopExtend.getBizID());
            apolloShopDTO.setType(apolloShopExtend.getType());
            apolloShopDTO.setRating(apolloShopExtend.getRating());
            apolloShopDTO.setShopExtendStatus(apolloShopExtend.getStatus());
        }
    }

    private void processShop(ApolloShopDTO apolloShopDTO, int shopID) {
        List<ApolloShopEntity> apolloShopList = apolloShopDAO.queryApolloShopByShopID(shopID);
        if(CollectionUtils.isNotEmpty(apolloShopList)) {
            ApolloShopEntity apolloShop = apolloShopList.get(0);
            apolloShopDTO.setShopID(apolloShop.getShopID());
            apolloShopDTO.setShopGroupID(apolloShop.getShopGroupID());
            apolloShopDTO.setCityID(apolloShop.getCityID());
            apolloShopDTO.setDistrict(apolloShop.getDistrict());
            apolloShopDTO.setShopType(apolloShop.getShopType());
            apolloShopDTO.setShopStatus(apolloShop.getShopStatus());
        }
    }

    private void processRegion(ApolloShopDTO apolloShopDTO, int shopID) {
        List<ShopRegionEntity> shopRegionList = shopRegionDAO.queryShopMainRegionByShopID(shopID);
        if(CollectionUtils.isNotEmpty(shopRegionList)) {
            ShopRegionEntity shopRegion = shopRegionList.get(0);
            apolloShopDTO.setMainRegionID(shopRegion.getRegionID());
        }
    }

    private void processCategory(ApolloShopDTO apolloShopDTO, int shopID) {
        List<ShopCategoryEntity> shopCategoryList = shopCategoryDAO.queryShopMainCategoryByShopID(shopID);
        if(CollectionUtils.isNotEmpty(shopCategoryList)) {
            ShopCategoryEntity shopCategory = shopCategoryList.get(0);
            apolloShopDTO.setMainCategoryID(shopCategory.getCategoryID());
        }
    }

}
