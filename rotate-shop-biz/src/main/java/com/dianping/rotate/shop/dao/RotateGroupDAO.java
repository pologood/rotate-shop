package com.dianping.rotate.shop.dao;

import com.dianping.avatar.dao.GenericDao;
import com.dianping.avatar.dao.annotation.DAOAction;
import com.dianping.avatar.dao.annotation.DAOActionType;
import com.dianping.avatar.dao.annotation.DAOParam;
import com.dianping.rotate.shop.entity.RotateGroupEntity;

import java.util.List;

/**
 * User: zhenwei.wang
 * Date: 15-1-4
 */
public interface RotateGroupDAO extends GenericDao{

	@DAOAction(action = DAOActionType.INSERT)
	public int addToRotateGroup(@DAOParam("rotateGroup")RotateGroupEntity rotateGroup);

	@DAOAction(action = DAOActionType.UPDATE)
	public void deleteRotateGroup(@DAOParam("id")int rotateGroupID);

	@DAOAction(action = DAOActionType.UPDATE)
	public void restoreRotateGroup(@DAOParam("id")int rotateGroupID);

	@DAOAction(action = DAOActionType.UPDATE)
	public void updateRotateGroup(@DAOParam("rotateGroup")RotateGroupEntity rotateGroup);

	@DAOAction(action = DAOActionType.LOAD)
	public RotateGroupEntity getRotateGroup(@DAOParam("id") int rotateGroupID);

	@DAOAction(action = DAOActionType.QUERY)
	public List<RotateGroupEntity> getRotateGroupList(@DAOParam("rotateGroupIDList") List<Integer> rotateGroupIDList);

	@DAOAction(action = DAOActionType.LOAD)
	public RotateGroupEntity getRotateGroupIgnoreStatus(@DAOParam("id") int rotateGroupID);

	@DAOAction(action = DAOActionType.LOAD)
	public RotateGroupEntity findRotateShopByShopId(@DAOParam("shopID") int shopId);

	@DAOAction(action = DAOActionType.QUERY)
	public List<RotateGroupEntity> queryRotateGroupByBizIDAndShopID(@DAOParam("bizID")int bizID, @DAOParam("shopID")int shopID);
}
