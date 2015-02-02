/**
 * Tencent
 */
package com.tencent.service;

import com.tencent.protocol.callback.NativeOneCallBackRes;

/**
 * @author:  JeffChen
 * @date:    2015年2月1日
 * @time:    下午3:17:01
 */
public class CallBackResService extends BaseService{
	
	 public CallBackResService(String weixinURL) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
	        super(weixinURL);
	    }

	    /**
	     * 请求支付服务
	     * @param scanPayReqData 这个数据对象里面包含了API要求提交的各种数据字段
	     * @return API返回的数据
	     * @throws Exception
	     */
	    public String request(NativeOneCallBackRes nativeOneCallBackRes) throws Exception {

	        //--------------------------------------------------------------------
	        //发送HTTPS的Post请求到API地址
	        //--------------------------------------------------------------------
	        String responseString = sendPost(nativeOneCallBackRes);

	        return responseString;
	    }

}
