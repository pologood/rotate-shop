package com.dianping.rotate.shop.service;

import com.dianping.rotate.shop.json.MessageEntity;

/**
 * Created by zaza on 15/1/20.
 */
public interface MessageProcessor{
    public void process(MessageEntity message) throws Exception;
}
