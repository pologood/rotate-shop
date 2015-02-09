package com.dianping.rotate.shop.model;

/**
 * Created by zaza on 15/2/2.
 */
public class ServiceResult {
    public static final Integer INTERNAL_ERROR = 500;
    public static final Integer SERVICE_EXCEPTION = 501;
    public static final Integer AUTHORITY_ERROR = 401;
    public static final Integer SUCCESS = 200;
    public static final Integer INPUT_ERROR = 402;

    private Integer code = SUCCESS;
    private Object msg;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Object getMsg() {
        return msg;
    }

    public void setMsg(Object msg) {
        this.msg = msg;
    }

    public static ServiceResult fail(Object msg) {
        ServiceResult serviceResult = new ServiceResult();
        serviceResult.setCode(INTERNAL_ERROR);
        serviceResult.setMsg(msg);
        return serviceResult;
    }

    public static ServiceResult success(Object msg) {
        ServiceResult serviceResult = new ServiceResult();
        serviceResult.setCode(SUCCESS);
        serviceResult.setMsg(msg);
        return serviceResult;
    }
}
