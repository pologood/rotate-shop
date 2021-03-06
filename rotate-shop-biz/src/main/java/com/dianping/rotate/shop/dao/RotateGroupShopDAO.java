package com.dianping.rotate.shop.dao;

import com.dianping.avatar.dao.GenericDao;
import com.dianping.avatar.dao.annotation.DAOAction;
import com.dianping.avatar.dao.annotation.DAOActionType;
import com.dianping.avatar.dao.annotation.DAOParam;
import com.dianping.rotate.shop.json.RotateGroupShopEntity;

import java.util.List;

/**
 * User: zhenwei.wang
 * Date: 15-1-4
 */
public interface RotateGroupShopDAO extends GenericDao {

	@DAOAction(action = DAOActionType.INSERT)
	public int addToRotateGroupShop(@DAOParam("rotateGroupShop")RotateGroupShopEntity rotateGroupShop);

	@DAOAction(action = DAOActionType.UPDATE)
	public void deleteRotateGroupShop(@DAOParam("id")int id);


	@DAOAction(action = DAOActionType.UPDATE)
	public void deleteRotateGroupShopByShopId(@DAOParam("shopId")int id);

	@DAOAction(action = DAOActionType.UPDATE)
	public void restoreRotateGroupShopByShopId(@DAOParam("shopId")int id);

	@DAOAction(action = DAOActionType.UPDATE)
	public void updateRotateGroupShop(@DAOParam("rotateGroupShop")RotateGroupShopEntity rotateGroupShop);

	@DAOAction(action = DAOActionType.LOAD)
	public RotateGroupShopEntity queryRotateGroupShop(@DAOParam("id")int id);

	@DAOAction(action = DAOActionType.QUERY)
	public List<RotateGroupShopEntity> queryRotateGroupShopByRotateGroupID(@DAOParam("rotateGroupID")int rotateGroupID);

	@DAOAction(action = DAOActionType.QUERY)
	public List<RotateGroupShopEntity> queryRotateGroupShopByShopID(@DAOParam("shopId")int shopId);

	@DAOAction(action = DAOActionType.QUERY)
	public List<RotateGroupShopEntity> queryRotateGroupShopByShopGroupIDAndBizID(@DAOParam("shopGroupID")int shopGroupId, @DAOParam("bizID")int bizID);

	@DAOAction(action = DAOActionType.INSERT)
	public void addToRotateGroupShopByList(@DAOParam("rotateGroupShopList")List<RotateGroupShopEntity> rotateGroupShopEntityList);

	@DAOAction(action = DAOActionType.LOAD)
	public int getShopNumInGroup(@DAOParam("rotateGroupID") int rotateGroupID);

	@DAOAction(action = DAOActionType.UPDATE)
	public void updateRotateGroupShopByShopID(@DAOParam("shopID") int shopId, @DAOParam("shopGroupID")int shopGroupId);
}
