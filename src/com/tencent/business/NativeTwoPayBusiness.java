/**
 * Tencent
 */
package com.tencent.business;

import org.slf4j.LoggerFactory;

import com.tencent.common.Log;
import com.tencent.common.Signature;
import com.tencent.common.Util;
import com.tencent.protocol.unify_pay_protocol.UnifyPayReqData;
import com.tencent.protocol.unify_pay_protocol.UnifyPayResData;
import com.tencent.service.UnifyPayService;

/**
 * @author:  JeffChen
 * @date:    2015年2月2日
 * @time:    上午11:13:43
 */
public class NativeTwoPayBusiness {
	
	public NativeTwoPayBusiness() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
		   unifyPayService = new UnifyPayService();      
	    }
	   
	   private UnifyPayService unifyPayService;
	   
	   private static Log log = new Log(LoggerFactory.getLogger(NativeTwoPayBusiness.class));
	   
       public interface ResultListener {
		   
		   
		   
	        //统一支付支付API系统返回失败，请检测Post给API的数据是否规范合法
	        void onFailByReturnCodeFail(UnifyPayResData unifyPayResData);

	        //支付请求API返回的数据签名验证失败，有可能数据被篡改了
	        void onFailBySignInvalid(UnifyPayResData unifyPayResData);

	        //统一下单逻辑错误的回调，这里集成到一个回调函数，商户应该详细实现，SDK不做过多假设
	        void onUnifyPayFail(UnifyPayResData scanPayResData);
	        	        
	        //支付成功
	        void onSuccess(UnifyPayResData unifyPayResData,String prepay_id);

	    }
       
       /**
	     * 直接执行Native模式1业务逻辑,这里run是指商户后台收到了微信支付的回调URL
	     * @param urlCallBack 这是微信post过来的回调里带的数据,XML格式
	     * @param weiXinURL 商户服务器收到的微信URL
	     * @param resultListener 商户根据微信支付的文档，实现监听所有的事件
	     * @throws Exception
	     */
	    public void run(UnifyPayReqData unifyPayReqData,ResultListener resultListener) throws Exception {

	    	//回复回调消息的所有字段，需要设置   	
	    		        
	        //接受API返回
	        String payServiceResponseString;

	        payServiceResponseString = unifyPayService.request(unifyPayReqData);
	        
	        //打印回包数据
	        log.i("统一下单服务回包:");
	        log.i(payServiceResponseString);

	        //将从API返回的XML数据映射到Java对象
	        UnifyPayResData unifyPayResData = (UnifyPayResData) Util.getObjectFromXML(payServiceResponseString, UnifyPayResData.class);

	        if (unifyPayResData == null || unifyPayResData.getReturn_code() == null) {
	            log.e("【支付失败】支付请求逻辑错误，请仔细检测传过去的每一个参数是否合法，或是看API能否被正常访问");
	            resultListener.onFailByReturnCodeFail(unifyPayResData);
	            return;
	        }
	        
	        //验证签名，无论业务返回成功与否
	        if (!Signature.checkIsSignValidFromResponseString(payServiceResponseString)) {
               log.e("【支付失败】支付请求API返回的数据签名验证失败，有可能数据被篡改了");
               resultListener.onFailBySignInvalid(unifyPayResData);
               return;
           }

	        if (unifyPayResData.getReturn_code().equals("FAIL")) {
	            //注意：一般这里返回FAIL是出现系统级参数错误，请检测Post给API的数据是否规范合法
	            log.e("【支付失败】支付API系统返回失败，请检测Post给API的数据是否规范合法");
	            log.e("return_msg: "+unifyPayResData.getReturn_msg());
	            resultListener.onFailByReturnCodeFail(unifyPayResData);
	            return;
	        } else {
	            log.i("支付API系统成功返回数据");
	            //获取错误码
	            String errorCode = unifyPayResData.getErr_code();
	            //获取错误描述
	            String errorCodeDes = unifyPayResData.getErr_code_des();

	            if (unifyPayResData.getResult_code().equals("SUCCESS")) {
	                //--------------------------------------------------------------------
	                //1)统一下单成功
	                //--------------------------------------------------------------------

	                log.i("【统一下单成功】");

	                if(!unifyPayResData.getCode_url().equals("")){
	                	String code_url = unifyPayResData.getCode_url();
	                	//下面开始调用第三方库生成二维码
	                	//.....
	                }
	                else//code_url是空
	                {
	                	resultListener.onUnifyPayFail(unifyPayResData);
	                	return;
	                }
	                          	                
	            }
	            else//统一下单失败
	            {
	            	resultListener.onUnifyPayFail(unifyPayResData);
                	return;
	            }
	        }
	                   
	    }

}
