package com.tencent.business;

import org.slf4j.LoggerFactory;


import com.tencent.common.Log;
import com.tencent.common.Signature;
import com.tencent.common.Util;
import com.tencent.protocol.unify_pay_protocol.UnifyPayResData;
import com.tencent.protocol.unify_pay_protocol.UnifyPayReqData;
import com.tencent.protocol.callback.NativeOneCallBack;
import com.tencent.protocol.callback.NativeOneCallBackRes;
import com.tencent.service.CallBackResService;
import com.tencent.service.UnifyPayService;

public class NativeOnePayBusiness {

	   public NativeOnePayBusiness() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
		   unifyPayService = new UnifyPayService();      
	    }
	   
	   private UnifyPayService unifyPayService;
	   
	   private static Log log = new Log(LoggerFactory.getLogger(NativeOnePayBusiness.class));
	   
	   public interface ResultListener {
		   
		   //微信的回调，签名错误，数据可能被篡改
		   void onFailBySignInvalid(NativeOneCallBack nativeOneCallBack);
		   
		    //收到微信发过来的回调请求，这里商户需要检查product_id是否是合法
		   void onReceiveCallBackUrl(NativeOneCallBack nativeOneCallBack);

		   //根据收到的回调函数，生成统一下单的所有参数
		   UnifyPayReqData onPrepareForUnifyPay(NativeOneCallBack nativeOneCallBack);
		   
	        //统一支付支付API系统返回失败，请检测Post给API的数据是否规范合法
	        void onFailByReturnCodeFail(UnifyPayResData unifyPayResData);

	        //支付请求API返回的数据签名验证失败，有可能数据被篡改了
	        void onFailBySignInvalid(UnifyPayResData unifyPayResData);

	        //统一下单逻辑错误的回调，这里集成到一个回调函数，商户应该详细实现，SDK不做过多假设
	        void onUnifyPayFail(UnifyPayResData scanPayResData);
	        	        
	        //支付成功
	        void onSuccess(UnifyPayResData unifyPayResData,String prepay_id);

	    }
	   
	    //每次调用订单查询API时的等待时间，因为当出现支付失败的时候，如果马上发起查询不一定就能查到结果，所以这里建议先等待一定时间再发起查询
	    private int waitingTimeBeforePayQueryServiceInvoked = 5000;

	    //循环调用订单查询API的次数
	    private int payQueryLoopInvokedCount = 6;
	    
	    
	    /**
	     * 直接执行Native模式1业务逻辑,这里run是指商户后台收到了微信支付的回调URL
	     * @param urlCallBack 这是微信post过来的回调里带的数据,XML格式
	     * @param weiXinURL 商户服务器收到的微信URL
	     * @param resultListener 商户根据微信支付的文档，实现监听所有的事件
	     * @throws Exception
	     */
	    public void run(String urlCallBack,String weiXinURL,ResultListener resultListener) throws Exception {

	    	//回复回调消息的所有字段，需要设置
	    	String return_code= "";
	    	String return_msg= "";
	    	String prepay_id= "";
	    	String result_code= "";
	    	String err_code_des= "";	    	
	    	
	    	//解析收到的来自微信服务器的回调数据
	    	log.i("收到的来自微信的回调数据:");
	    	log.i(urlCallBack);
	    	NativeOneCallBack nativeOneCallBack = (NativeOneCallBack)Util.getObjectFromXML(urlCallBack, NativeOneCallBack.class);
	    	
	        //检查数据的签名
	    	 if(!Signature.checkIsSignValidFromResponseString(urlCallBack)) {
                 log.e("【支付失败】支付请求API返回的数据签名验证失败，有可能数据被篡改了");
                 resultListener.onFailBySignInvalid(nativeOneCallBack);
             }
	    	 
	    	 //检查product_id是否合法，通常商家需要和自己的数据库相比较
	    	 //这里没有只传product_id，因为商家可能要处理其他数据，比如没有关注公众号的openid可以存储下来，后续做推广
	    	 //String product_id = nativeOneCallBack.getProduct_id();
	    	 resultListener.onReceiveCallBackUrl(nativeOneCallBack);
	    	 
	    	 //统一支付下单
	    	 //商户需要通过收到product_id，获取统一下单的信息，这里使用一个监听器，让商家来实现
	    	 //商家可能需要根据product_id去数据库里找到相应数据
	    	UnifyPayReqData unifyPayReqData = resultListener.onPrepareForUnifyPay(nativeOneCallBack); 
	        
	        //接受API返回
	        String payServiceResponseString;

	        long costTimeStart = System.currentTimeMillis();
	        log.i("开始请求统一下单服务：");
	        payServiceResponseString = unifyPayService.request(unifyPayReqData);
	        long costTimeEnd = System.currentTimeMillis();
	        long totalTimeCost = costTimeEnd - costTimeStart;
	        log.i("被扫支付服务总耗时：" + totalTimeCost + "ms");
	        
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
	            return_code = "FAIL";
	            return_msg  = "Wrong";
	        } else {
	            log.i("支付API系统成功返回数据");
	            //获取错误码
	            String errorCode = unifyPayResData.getErr_code();
	            //获取错误描述
	            String errorCodeDes = unifyPayResData.getErr_code_des();

	            if (unifyPayResData.getResult_code().equals("SUCCESS")) {//统一下单成功

	                //--------------------------------------------------------------------
	                //1)统一下单成功
	                //--------------------------------------------------------------------

	                log.i("【统一下单成功】");

	                if(!unifyPayResData.getPrepay_id().equals("")){
	                	prepay_id = unifyPayResData.getPrepay_id();
	    	            return_code = "SUCCESS";
	    	            return_msg  = "OK";
	    	            result_code = "SUCCESS";
	                }
	                else//prepay_id是空
	                {
	                	return_code = "SUCCESS";
	    	            return_msg  = "OK";
	    	            result_code = "FAIL";
	    	            err_code_des = "unify failed";
	                }
	                          	                
	            }
	            else//统一下单失败
	            {
	            	return_code = "SUCCESS";
    	            return_msg  = "OK";
    	            result_code = "FAIL";
    	            err_code_des = "unify failed";
	            }
	        }
	            
	            
	        //这里开始回调的返回，让用户完成支付
            NativeOneCallBackRes nativeOneCallBackRes = new NativeOneCallBackRes(return_code,return_msg,prepay_id,result_code,err_code_des);
            CallBackResService   callBackResService =  new CallBackResService(weiXinURL);
            //微信开始处理支付，不会有回复
            callBackResService.request(nativeOneCallBackRes);	       
	    }
	   
}
