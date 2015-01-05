package com.dianping.rotate.shop.dao

import com.dianping.rotate.shop.AbstractSpockTest
import com.dianping.rotate.shop.entity.ApolloShopEntity
import com.dianping.rotate.shop.entity.RegionEntity
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created by luoming on 15/1/4.
 */
class RegionDAOTest extends AbstractSpockTest {
    @Autowired
    RegionDAO regionDAO;

    def "add, query and delete one region"() {
        setup:
        int regionID = 1

        when:
        def s = new RegionEntity()
        s.setRegionID(regionID)
        s.setRegionName('上海')
        s.setParentID(2)
        s.setStatus(1)
        regionDAO.addRegion(s)

        then:
        1 == regionDAO.queryRegionByRegionID(regionID).get(0).getRegionID()
        s.setRegionName('北京')
        regionDAO.updateRegion(s)
        '北京'.equals(regionDAO.queryRegionByRegionID(regionID).get(0).getRegionName())

        cleanup:
        regionDAO.deleteRegionByRegionID(regionID)
    }
}
