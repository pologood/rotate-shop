package com.dianping.rotate.shop.service.impl;

import com.dianping.combiz.service.CityService;
import com.dianping.rotate.core.api.dto.RotateGroupUserDTO;
import com.dianping.rotate.core.api.service.RotateGroupUserService;
import com.dianping.rotate.shop.constants.BizTypeEnum;
import com.dianping.rotate.shop.constants.RotateGroupTypeEnum;
import com.dianping.rotate.shop.constants.WrongOperEnum;
import com.dianping.rotate.shop.dao.*;
import com.dianping.rotate.shop.exceptions.WrongShopInfoException;
import com.dianping.rotate.shop.factory.ApolloShopExtendFactory;
import com.dianping.rotate.shop.json.*;
import com.dianping.rotate.shop.service.ShopService;
import com.dianping.shopremote.remote.dto.ShopCategoryDTO;
import com.dianping.shopremote.remote.dto.ShopDTO;
import com.dianping.shopremote.remote.dto.ShopRegionDTO;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by yangjie on 1/13/15.
 */
public class ShopServiceImpl implements ShopService {

	Logger logger = LoggerFactory.getLogger(ShopServiceImpl.class);

	@Autowired
	com.dianping.shopremote.remote.ShopService shopService;

	@Autowired
	RotateGroupUserService rotateGroupUserService;

	@Autowired
	CityService cityService;

	@Autowired
	ApolloShopDAO apolloShopDAO;

	@Autowired
	ApolloShopExtendDAO apolloShopExtendDAO;

	@Autowired
	RotateGroupDAO rotateGroupDAO;

	@Autowired
	RotateGroupShopDAO rotateGroupShopDAO;

	@Autowired
	ShopCategoryDAO shopCategoryDAO;

	@Autowired
	ShopRegionDAO shopRegionDAO;

	@Autowired
	WrongOperDAO wrongOperDAO;

	List<ApolloShopExtendFactory> extendFactories;

	public void setExtendFactories(List<ApolloShopExtendFactory> extendFactories) {
		this.extendFactories = extendFactories;
	}

	@Override
	public void addShop(int shopId) {
		ShopDTO shopDTO = shopService.loadShop(shopId);
		if (shopDTO == null) {
			throw new WrongShopInfoException("add shop info failed with wrong shopId!");
		}

		if (apolloShopDAO.queryApolloShopByShopIDWithNoStatus(shopId) != null) {
			throw new WrongShopInfoException("add shop info failed with shop existed!");
		}

		List<ShopCategoryDTO> shopCategoryDTOList = shopService.findShopCategories(shopId, shopDTO.getCityId());
		List<ShopRegionDTO> shopRegionDTOList = shopService.findShopRegions(shopId);

		checkEnv(shopId);
		ApolloShopEntity apolloShopEntity = insertApolloShop(shopDTO);
		if (CollectionUtils.isNotEmpty(shopCategoryDTOList))
			insertShopCategoryList(shopCategoryDTOList, shopDTO);
		if (CollectionUtils.isNotEmpty(shopRegionDTOList))
			insertShopRegionList(shopRegionDTOList, shopDTO);
		List<ApolloShopExtendEntity> apolloShopExtendList = insertApolloShopExtendList(shopId);
		for (ApolloShopExtendEntity apolloShopExtend : apolloShopExtendList) {
			List<RotateGroupShopEntity> rotateGroupShopList =
					rotateGroupShopDAO.queryRotateGroupShopByShopGroupIDAndBizID(shopDTO.getShopGroupId(), apolloShopExtend.getBizID());
			if (rotateGroupShopList.size() == 0) {
				int rotateGroupID = insertRotateGroup(apolloShopExtend);
				insertRotateGroupShop(rotateGroupID, apolloShopEntity);
			} else {
				int rotateGroupID = rotateGroupShopList.get(0).getRotateGroupID();
				// 多个轮转组属于不同的人员时候，需要记录，等待运营人员修复
				Integer rotateGroupID_ = processWrongOper(shopId, rotateGroupShopList);
				if(rotateGroupID_ > 0) {
					rotateGroupID = rotateGroupID_;
				}
				int apolloShopID = rotateGroupShopList.get(0).getShopID();
				int apolloShopExtendType = apolloShopExtendDAO.queryApolloShopExtendByShopIDAndBizID(apolloShopID,
						apolloShopExtend.getBizID()).get(0).getType();
				apolloShopExtend.setType(apolloShopExtendType);
				changeApolloShopExtendType(apolloShopExtend);
				changeRotateGroupType(rotateGroupID);//更新轮转组type
				insertRotateGroupShop(rotateGroupID, apolloShopEntity);
			}
		}
	}

	private Integer processWrongOper(int shopId, List<RotateGroupShopEntity> rotateGroupShopList) {
		Set<Integer> rotateGroupIDSet = procecssRotateGroupID(rotateGroupShopList);
		Integer rotateGroupID_ = isRotateGroupBelongToMore(rotateGroupIDSet);
		if(rotateGroupID_ == 0) {
			addWrongOper(shopId, rotateGroupIDSet);
		}
		return rotateGroupID_;
	}

	/**
	 * 去除重复的RotageGroupID
	 * @param rotateGroupShopList
	 * @return
	 */
	private Set<Integer> procecssRotateGroupID(List<RotateGroupShopEntity> rotateGroupShopList) {
		Set<Integer> rotateGroupIDSet = null;
		if(CollectionUtils.isNotEmpty(rotateGroupShopList)) {
			rotateGroupIDSet = new HashSet<Integer>();
			for(RotateGroupShopEntity rotateGroupShopEntity : rotateGroupShopList) {
				rotateGroupIDSet.add(rotateGroupShopEntity.getRotateGroupID());
			}
		}
		return rotateGroupIDSet;
	}

	/**
	 * 判断轮转组是否属于多人
	 * @param rotateGroupIDSet
	 * @return -1 轮转组集合为空；-2 轮转组都属于公海；0 轮转组属于多人；其它 轮转组属于单人(有所属人的轮转组ID）
	 */
	private Integer isRotateGroupBelongToMore(Set<Integer> rotateGroupIDSet) {
		if(rotateGroupIDSet != null && rotateGroupIDSet.size() >= 1) {
			Set<Integer> userIDSet = new HashSet<Integer>();
			Integer rotateGroupID_ = -1;
			for(Integer rotateGroupID : rotateGroupIDSet) {
				RotateGroupUserDTO rotateGroupUserDTO = rotateGroupUserService.findByRotateGroupId(rotateGroupID);
				if(rotateGroupUserDTO != null) {
					userIDSet.add(rotateGroupUserDTO.getUserId());
					rotateGroupID_ = rotateGroupID;
				}
			}
			if(userIDSet.size() >= 2) {
				return 0;
			} else if(userIDSet.size() == 1) {
				return rotateGroupID_;
			} else {
				return -2;
			}
		}
		return -1;
	}

	/**
	 * 添加错误操作
	 * @param shopID
	 * @param rotateGroupIDSet
	 */
	private void addWrongOper(int shopID, Set<Integer> rotateGroupIDSet) {
		WrongOperEntity wrongOperEntity = new WrongOperEntity();
		wrongOperEntity.setSource(String.valueOf(shopID));
		wrongOperEntity.setTarget(processTarget(rotateGroupIDSet));
		wrongOperEntity.setType(WrongOperEnum.SHOP_ROTATEGROUP_MERGE.getCode());
		wrongOperDAO.addWrongOper(wrongOperEntity);
	}

	private String processTarget(Set<Integer> rotateGroupIDSet) {
		String target = "";
		for(Integer rotateGroupID : rotateGroupIDSet) {
			target += rotateGroupID + ",";
		}
		if(target.endsWith(",")) {
			target = target.substring(0, target.length() - 1);
		}
		return target;
	}

	@Override
	public void updateShop(int shopId) {
		ShopDTO shopDTO = shopService.loadShop(shopId);
		if (shopDTO == null)
			throw new WrongShopInfoException("update shop info failed with wrong shopId!");
		ApolloShopEntity apolloShopEntity = apolloShopDAO.queryApolloShopByShopIDWithNoStatus(shopId);
		if (apolloShopEntity == null)
			throw new WrongShopInfoException("update shop is not existed!");

		List<ShopCategoryDTO> shopCategoryDTOList = shopService.findShopCategories(shopId, shopDTO.getCityId());
		List<ShopRegionDTO> shopRegionDTOList = shopService.findShopRegions(shopId);

		boolean isStatusChanged = (apolloShopEntity.getShopStatus() != shopDTO.getPower());
		if (apolloShopEntity.getShopGroupID() != shopDTO.getShopGroupId()) {
			updateRotateGroupShopByShopID(shopId, shopDTO.getShopGroupId());
			isStatusChanged = true;
		}

		updateApolloShop(apolloShopEntity, shopDTO);

		//如果前台想要去掉Category和Region时，无法做到
		if (CollectionUtils.isNotEmpty(shopCategoryDTOList))
			updateShopCategoryList(shopCategoryDTOList, shopDTO);
		if (CollectionUtils.isNotEmpty(shopRegionDTOList))
			updateShopRegionList(shopRegionDTOList, shopDTO);

		if (isStatusChanged) {
			updateRotateGroupTypeByShopID(shopId);
		}
	}

	@Override
	public void closeShop(int shopId) {
		apolloShopDAO.deleteApolloShopByShopID(shopId);
		updateRotateGroupTypeByShopID(shopId);
	}

	@Override
	public void openShop(int shopId) {
		apolloShopDAO.restoreApolloShopByShopID(shopId);
		updateRotateGroupTypeByShopID(shopId);
	}

	@Override
	public void updateRotateGroupTypeByShopID(int shopId) {
		for (RotateGroupShopEntity group : rotateGroupShopDAO.queryRotateGroupShopByShopID(shopId)) {
			updateRotateGroupTypeByRotateGroupId(group.getRotateGroupID());
		}
	}

	@Override
	public void updateRotateGroupTypeByRotateGroupId(int rotateGroupID) {

		// 这里需要取出所有的轮转组，包括已经删掉的，因为这里需要根据轮转组下的门店状态重置轮转组的status
		RotateGroupEntity rotateGroup = rotateGroupDAO.getRotateGroupIgnoreStatus(rotateGroupID);
		// 如果没有这个轮转组，就不操作
		if (rotateGroup == null) {
			return;
		}

		int shopCountInThisRotateGroup;

		List<RotateGroupShopEntity> r = rotateGroupShopDAO.queryRotateGroupShopByRotateGroupID(rotateGroupID);

		if (r.size() == 0) {
			shopCountInThisRotateGroup = 0;
		} else {
			// 这里过滤，只取正常营业的门店
			List<ApolloShopEntity> shops = apolloShopDAO.queryApolloShopByShopIDList(Lists.transform(r, new Function<RotateGroupShopEntity, Integer>() {
				@Override
				public Integer apply(RotateGroupShopEntity rotateGroupShopEntity) {
					return rotateGroupShopEntity.getShopID();
				}
			}));

			shopCountInThisRotateGroup = shops.size();
		}


		if (shopCountInThisRotateGroup > 1) {
			// 大于1家门店,设为连锁店
			rotateGroup.setType(RotateGroupTypeEnum.CHAIN.getCode());
		} else if(shopCountInThisRotateGroup == 1){
			// 单店
			rotateGroup.setType(RotateGroupTypeEnum.SINGLE.getCode());
		} else {
			rotateGroup.setType(RotateGroupTypeEnum.CLOSE.getCode());
		}

		rotateGroupDAO.updateRotateGroup(rotateGroup);
	}

	@Override
	public void updateRotateGroupType(int rotateGroupID) {
		RotateGroupEntity rotateGroup = rotateGroupDAO.getRotateGroupIgnoreStatus(rotateGroupID);
		// 如果没有这个轮转组，就不操作
		if (rotateGroup == null) {
			return;
		}

		boolean flag = false;
		int shopCountInThisRotateGroup = rotateGroupShopDAO.getShopNumInGroup(rotateGroupID);
		int rotateGroupType = rotateGroup.getType();

		if (shopCountInThisRotateGroup > 1 && rotateGroupType != 1) {
			// 大于1家门店,设为连锁店
			rotateGroup.setType(RotateGroupTypeEnum.CHAIN.getCode());
			flag = true;
		}
		if (shopCountInThisRotateGroup == 1 && rotateGroupType != 0) {
			// 单店
			rotateGroup.setType(RotateGroupTypeEnum.SINGLE.getCode());
			flag = true;
		}
		if(shopCountInThisRotateGroup < 1 && rotateGroupType != -1){
			//轮转组关闭
			rotateGroup.setType(RotateGroupTypeEnum.CLOSE.getCode());
			flag = true;
		}

		if (flag) {
			rotateGroupDAO.updateRotateGroup(rotateGroup);
		}
	}

	@Override
	public List<RotateGroupEntity> getRotateGroupList(int pageSize, int offset) {
		return rotateGroupDAO.queryRotateGroupIDList(pageSize, offset);
	}

	@Override
	public int getRotateGroupNum() {
		return rotateGroupDAO.getRotateGroupNum();
	}

	@Override
	public void updateShopExtendTypeByRotateGroupID(int rotateGroupID, int bizID) {
		int vipShopNum = apolloShopExtendDAO.queryVipShopExtendNumByRotateGroupID(rotateGroupID, bizID);
		if (vipShopNum > 0)
			apolloShopExtendDAO.updateApolloShopExtendTypeByRotateGroupID(rotateGroupID, bizID);
	}

	private ApolloShopEntity insertApolloShop(ShopDTO shopDTO) {
		ApolloShopEntity apolloShopEntity = new ApolloShopEntity();
		apolloShopEntity.setShopID(shopDTO.getShopId());
		apolloShopEntity.setShopGroupID(shopDTO.getShopGroupId());
		apolloShopEntity.setCityID(shopDTO.getCityId());
		apolloShopEntity.setDistrict(shopDTO.getDistrict() == null ? 0 : shopDTO.getDistrict()); // 无商圈的时候默认为0
		apolloShopEntity.setShopType(shopDTO.getShopType());
		apolloShopEntity.setShopStatus(getApolloShopStatus(shopDTO.getPower(), shopDTO.getDisplayStatus()));
		apolloShopEntity.setProvinceID(cityService.loadCity(shopDTO.getCityId()).getProvinceID());

		int id = apolloShopDAO.addApolloShop(apolloShopEntity);
		apolloShopEntity.setId(id);
		return apolloShopEntity;
	}

	private void insertShopRegionList(List<ShopRegionDTO> shopRegionDTOList, ShopDTO shopDTO) {
		List<ShopRegionEntity> shopRegionEntityList = Lists.newArrayList();
		int mainRegionId = shopDTO.getMainRegionId();
		int shopId = shopDTO.getShopId();
		for (ShopRegionDTO shopRegionDTO : shopRegionDTOList) {
			ShopRegionEntity shopRegionEntity = new ShopRegionEntity();
			shopRegionEntity.setShopID(shopId);
			shopRegionEntity.setRegionID(shopRegionDTO.getRegionId());
			shopRegionEntity.setIsMain(mainRegionId == shopRegionDTO.getRegionId() ? 1 : 0);
			shopRegionEntity.setStatus(1);
			shopRegionEntityList.add(shopRegionEntity);
		}
		shopRegionDAO.addShopRegionByList(shopRegionEntityList);
	}

	private void insertShopCategoryList(List<ShopCategoryDTO> shopCategoryDTOList, ShopDTO shopDTO) {
		List<ShopCategoryEntity> shopCategoryEntityList = Lists.newArrayList();
		int mainCategoryId = shopDTO.getMainCategoryId();
		int shopId = shopDTO.getShopId();
		for (ShopCategoryDTO shopCategoryDTO : shopCategoryDTOList) {
			ShopCategoryEntity shopCategoryEntity = new ShopCategoryEntity();
			shopCategoryEntity.setShopID(shopId);
			shopCategoryEntity.setCategoryID(shopCategoryDTO.getId());
			shopCategoryEntity.setIsMain(mainCategoryId == shopCategoryDTO.getId() ? 1 : 0);
			shopCategoryEntity.setStatus(1);
			shopCategoryEntityList.add(shopCategoryEntity);
		}
		shopCategoryDAO.addShopCategoryByList(shopCategoryEntityList);
	}

	private RotateGroupShopEntity insertRotateGroupShop(int rotateGroupID, ApolloShopEntity apolloShopEntity) {
		List<Integer> rotateGroupIDList = Lists.newArrayList(rotateGroupID);
		List<RotateGroupShopEntity> rotateGroupShopEntityList = insertRotateGroupShopList(rotateGroupIDList, apolloShopEntity);
		return rotateGroupShopEntityList.get(0);
	}

	private List<RotateGroupShopEntity> insertRotateGroupShopList(List<Integer> rotateGroupIDList, ApolloShopEntity apolloShopEntity) {
		List<RotateGroupShopEntity> rotateGroupShopEntityList = Lists.newArrayList();
		for (Integer rotateGroupID : rotateGroupIDList) {
			RotateGroupShopEntity rotateGroupShopEntity = new RotateGroupShopEntity();
			rotateGroupShopEntity.setRotateGroupID(rotateGroupID);
			rotateGroupShopEntity.setShopID(apolloShopEntity.getShopID());
			rotateGroupShopEntity.setShopGroupID(apolloShopEntity.getShopGroupID());
			rotateGroupShopEntity.setStatus(1);
			rotateGroupShopEntityList.add(rotateGroupShopEntity);
		}
		rotateGroupShopDAO.addToRotateGroupShopByList(rotateGroupShopEntityList);
		return rotateGroupShopEntityList;
	}

	public int insertRotateGroup(ApolloShopExtendEntity apolloShopExtend) {
		List<ApolloShopExtendEntity> apolloShopExtendList = Lists.newArrayList(apolloShopExtend);
		List<Integer> rotateGroupIDList = insertRotateGroupList(apolloShopExtendList);
		return rotateGroupIDList.get(0);
	}

	private List<Integer> insertRotateGroupList(List<ApolloShopExtendEntity> apolloShopExtendList) {
		List<Integer> rotateGroupIDList = Lists.newArrayList();
		for (ApolloShopExtendEntity apolloShopExtend : apolloShopExtendList) {
			if (apolloShopExtend != null) {
				RotateGroupEntity rotateGroupEntity = new RotateGroupEntity();
				rotateGroupEntity.setBizID(apolloShopExtend.getBizID());
				rotateGroupEntity.setType(RotateGroupTypeEnum.SINGLE.getCode());//0：单店；1：连锁店
				rotateGroupEntity.setStatus(1);
				int rotateGroupID = rotateGroupDAO.addToRotateGroup(rotateGroupEntity);
				rotateGroupIDList.add(rotateGroupID);
			}
		}
		return rotateGroupIDList;
	}

	private void changeRotateGroupType(int rotateGroupID) {
		RotateGroupEntity rotateGroupEntity = rotateGroupDAO.getRotateGroup(rotateGroupID);
		rotateGroupEntity.setType(RotateGroupTypeEnum.CHAIN.getCode());//0：单店；1：连锁店
		rotateGroupDAO.updateRotateGroup(rotateGroupEntity);
	}

	private void changeApolloShopExtendType(ApolloShopExtendEntity apolloShopExtend) {
		apolloShopExtendDAO.updateApolloShopExtend(apolloShopExtend);
	}

	/**
	 * 根据门店的自然状态和审核状态返回门店的最终状态
	 * displayStatus=1&&businessStatus=x   ======> businessStatus = x
	 * displayStatus=0&&businessStatus=x   ======> businessStatus = 2
	 * displayStatus=2&&businessStatus=x   ======> businessStatus = 1
	 *
	 * @param businessStatus 门店的自然状态
	 * @param displayStatus  门店的审核状态
	 * @return
	 */
	private int getApolloShopStatus(Short businessStatus, Short displayStatus) {
		if (displayStatus == 0)
			return 2;
		if (displayStatus == 1)
			return businessStatus;
		return 1;
	}

	/**
	 * 按biz为门店创建shopExtend
	 *
	 * @param shopID
	 * @return
	 */
	private List<ApolloShopExtendEntity> insertApolloShopExtendList(int shopID) {
		List<ApolloShopExtendEntity> extendEntities = Lists.newArrayList();
		for (ApolloShopExtendFactory factory : extendFactories) {
			extendEntities.add(factory.createApolloShopExtend(shopID));
		}
		apolloShopExtendDAO.addApolloShopExtendByList(extendEntities);
		return extendEntities;
	}

	private void updateApolloShop(ApolloShopEntity apolloShopEntity, ShopDTO shopDTO) {
		apolloShopEntity.setShopGroupID(shopDTO.getShopGroupId());
		apolloShopEntity.setShopType(shopDTO.getShopType());
		apolloShopEntity.setCityID(shopDTO.getCityId());
		apolloShopEntity.setDistrict(shopDTO.getDistrict());
		apolloShopEntity.setShopStatus(getApolloShopStatus(shopDTO.getPower(), shopDTO.getDisplayStatus()));
		apolloShopEntity.setProvinceID(cityService.loadCity(shopDTO.getCityId()).getProvinceID());

		apolloShopDAO.updateApolloShop(apolloShopEntity);
	}

	private void updateShopRegionList(List<ShopRegionDTO> shopRegionDTOList, ShopDTO shopDTO) {
		shopRegionDAO.deleteShopRegionDirectlyByShopID(shopDTO.getShopId());
		insertShopRegionList(shopRegionDTOList, shopDTO);
	}

	private void updateShopCategoryList(List<ShopCategoryDTO> shopCategoryDTOList, ShopDTO shopDTO) {
		shopCategoryDAO.deleteShopCategoryDirectlyByShopID(shopDTO.getShopId());
		insertShopCategoryList(shopCategoryDTOList, shopDTO);
	}


	/**
	 * 对于合并/拆分连锁店：
	 * 1.如果被变化的门店在公海中，则将被变化门店的rotateGroupID改成同一shopGroup下最小的rotateGroupID
	 * 2.如果被变化的门店在私海中，则遍历同一shopGroup，将rotateGroupID最小的且属于公海的门店的rotateGroupID改成被变化门店的rotateGroupID
	 * 2.1 同一shopGroup下所有门店都在私海则不合并
	 * PS: // 私海属于同一人是否需要合并? 有公海且有私海，私海是否属于同一人?  门店属于公海私海为什么要区分?
	 *
	 * @param shopId      变化的门店ID
	 * @param shopGroupId 变化后门店的shopGroupID
	 */
	private void updateRotateGroupShopByShopID(int shopId, int shopGroupId) {
		for (BizTypeEnum bizTypeEnum : BizTypeEnum.values()) {
			int bizId = bizTypeEnum.getCode();
			List<RotateGroupShopEntity> rotateGroupShopEntities = rotateGroupShopDAO.queryRotateGroupShopByShopGroupIDAndBizID(shopGroupId, bizId);
			RotateGroupShopEntity changedRotateGroupShop = rotateGroupShopDAO.queryRotateGroupShopByShopIDAndBizID(shopId, bizId);
			if (changedRotateGroupShop == null)
				continue;
			if (CollectionUtils.isNotEmpty(rotateGroupShopEntities)) {
				if (rotateGroupUserService.findByShopIdAndBizId(shopId, bizId) == null) {//门店在公海里
					int rotateGroupID = rotateGroupShopEntities.get(0).getRotateGroupID();
					// 多个轮转组属于不同的人员时候，需要记录，等待运营人员修复
					Integer rotateGroupID_ = processWrongOper(shopId, rotateGroupShopEntities);
					if(rotateGroupID_ > 0) {
						rotateGroupID = rotateGroupID_;
					}
					changedRotateGroupShop.setRotateGroupID(rotateGroupID);
				} else {//门店在私海里
					for (RotateGroupShopEntity rotateGroupShop : rotateGroupShopEntities) {
						if (rotateGroupUserService.findByRotateGroupId(rotateGroupShop.getRotateGroupID()) == null) {
							rotateGroupShop.setRotateGroupID(changedRotateGroupShop.getRotateGroupID());
							rotateGroupShopDAO.updateRotateGroupShop(rotateGroupShop);
							break;
						}
					}
				}
			}
			changedRotateGroupShop.setShopGroupID(shopGroupId);
			rotateGroupShopDAO.updateRotateGroupShop(changedRotateGroupShop);
		}
	}

	private void checkEnv(int shopId) {
		try {
			apolloShopExtendDAO.deleteApolloShopExtendDirectlyByShopID(shopId);
			shopCategoryDAO.deleteShopCategoryDirectlyByShopID(shopId);
			shopRegionDAO.deleteShopRegionDirectlyByShopID(shopId);
			rotateGroupShopDAO.deleteRotateGroupShopDirectlyByShopId(shopId);
		} catch (Exception e) {
			logger.warn("clean environment failed!", e);
		}
	}

}
