package com.dianping.rotate.shop.dao;

import com.dianping.avatar.dao.GenericDao;
import com.dianping.avatar.dao.annotation.DAOAction;
import com.dianping.avatar.dao.annotation.DAOActionType;
import com.dianping.avatar.dao.annotation.DAOParam;
import com.dianping.rotate.shop.entity.ApolloShopBusinessStatusEntity;

import java.util.List;

/**
 * Created by luoming on 15/1/4.
 */
public interface ApolloShopBusinessStatusDAO extends GenericDao {

    @DAOAction(action = DAOActionType.QUERY)
    List<ApolloShopBusinessStatusEntity> queryApolloShopBusinessStatusByShopID(@DAOParam("shopID") int shopID);

    @DAOAction(action = DAOActionType.QUERY)
    List<ApolloShopBusinessStatusEntity> queryApolloShopBusinessStatusByShopIDAndBusinessType(@DAOParam("shopID") int shopID, @DAOParam("businessType") int bussinessType);

    @DAOAction(action = DAOActionType.INSERT)
    int addApolloShopBusinessStatus(@DAOParam("apolloShopBusinessStatus") ApolloShopBusinessStatusEntity apolloShopBusiness);

    @DAOAction(action = DAOActionType.UPDATE)
    int deleteApolloShopBusinessStatusByShopID(@DAOParam("shopID") int shopID);

    @DAOAction(action = DAOActionType.UPDATE)
    int updateApolloShopBusinessStatus(@DAOParam("apolloShopBusinessStatus") ApolloShopBusinessStatusEntity apolloShopBusiness);

}