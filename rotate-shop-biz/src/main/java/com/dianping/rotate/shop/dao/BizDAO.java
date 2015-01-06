package com.dianping.rotate.shop.dao;

import com.dianping.avatar.dao.GenericDao;
import com.dianping.avatar.dao.annotation.DAOAction;
import com.dianping.avatar.dao.annotation.DAOActionType;
import com.dianping.avatar.dao.annotation.DAOParam;
import com.dianping.rotate.shop.entity.BizEntity;

import java.util.List;

/**
 * User: zhenwei.wang
 * Date: 15-1-5
 */
public interface BizDAO extends GenericDao {

	@DAOAction(action = DAOActionType.INSERT)
	public void addToBiz(@DAOParam("biz")BizEntity biz);

	@DAOAction(action = DAOActionType.DELETE)
	public void deleteBiz(@DAOParam("bizID")int bizID);

	@DAOAction(action = DAOActionType.UPDATE)
	public void updateBiz(@DAOParam("biz")BizEntity biz);

	@DAOAction(action = DAOActionType.QUERY)
	public List<BizEntity> queryBiz(@DAOParam("bizID")int bizID);
}