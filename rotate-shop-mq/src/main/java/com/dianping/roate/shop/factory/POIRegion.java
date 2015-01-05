package com.dianping.roate.shop.factory;
import com.dianping.roate.shop.constants.MessageSource;
import com.dianping.roate.shop.constants.MessageStatus;
import com.dianping.roate.shop.constants.POIMessageType;
import com.dianping.rotate.shop.dao.MessageQueueDAO;
import com.dianping.rotate.shop.entity.MessageEntity;
import com.dianping.swallow.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by zaza on 15/1/5.
 */
public class POIRegion implements POIChange {
    @Autowired
    MessageQueueDAO messageQueueDAO;

    @Override
    public void addToMsgQueue(Message msg){
        MessageEntity msgEntity = new MessageEntity();
        msgEntity.setAttemptIndex(0);
        msgEntity.setMsg(msg.getContent());
        msgEntity.setSource(MessageSource.PERSON);
        msgEntity.setStatus(MessageStatus.NEW);
        msgEntity.setSwallowId(msg.getMessageId());
        msgEntity.setType(POIMessageType.SHOP_REGION);
        messageQueueDAO.addToMessageQueue(msgEntity);
    }
}
