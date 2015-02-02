package com.tencent.service;

import com.tencent.common.Configure;
import com.tencent.protocol.unify_pay_protocol.UnifyPayReqData;

/**
 * @author:  JeffChen
 * @date:    2015年1月28日
 * @time:    下午6:32:54
 */

public class UnifyPayService extends BaseService{
	
	 public UnifyPayService() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
	        super(Configure.UNIFY_PAY_API);
	    }

	    /**
	     * 请求支付服务
	     * @param scanPayReqData 这个数据对象里面包含了API要求提交的各种数据字段
	     * @return API返回的数据
	     * @throws Exception
	     */
	    public String request(UnifyPayReqData unifyPayReqData) throws Exception {

	        //--------------------------------------------------------------------
	        //发送HTTPS的Post请求到API地址
	        //--------------------------------------------------------------------
	        String responseString = sendPost(unifyPayReqData);

	        return responseString;
	    }

}
