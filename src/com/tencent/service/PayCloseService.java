package com.tencent.service;

import com.tencent.common.Configure;
import com.tencent.protocol.pay_close_protocol.PayCloseReqData;


public class PayCloseService extends BaseService{

    public PayCloseService() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        super(Configure.PAY_CLOSE_API);
    }

    /**
     * 请求订单查询服务
     * @param payQueryReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的XML数据
     * @throws Exception
     */
    public String request(PayCloseReqData payCloseReqData) throws Exception {

        //--------------------------------------------------------------------
        //发送HTTPS的Post请求到API地址
        //--------------------------------------------------------------------
        String responseString = sendPost(payCloseReqData);

        return responseString;
    }


}