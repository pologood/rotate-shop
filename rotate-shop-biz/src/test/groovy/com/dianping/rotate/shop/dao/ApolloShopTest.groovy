package com.dianping.rotate.shop.dao

import org.springframework.beans.factory.annotation.Autowired

/**
 * Created by luoming on 15/1/4.
 */
class ApolloShopInfoTest extends AbstractSpockTest {
    @Autowired
    ApolloShopDAO apolloShopDAO;

    def "add, query and delete apolloShop"() {
        setup:

        int shopID = 1
        when:
        def s = new ApolloShopEntity()
        s.setShopID(shopID)
        s.setShopGroupID(1)
        s.setCityID(1)
        s.setDistrict('上海')
        s.setShopType(1)
        s.setStatus(1)
        apolloShopDAO.addApolloShop(s)

        then:
        1 == apolloShopDAO.queryApolloShopByShopID(shopID).get(0).getShopID()

        cleanup:
        apolloShopDAO.deleteApolloShopByShopID(shopID)
    }
}
