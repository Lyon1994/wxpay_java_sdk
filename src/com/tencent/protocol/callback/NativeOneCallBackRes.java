/**
 * Tencent
 */
package com.tencent.protocol.callback;
import com.tencent.common.Configure;
import com.tencent.common.RandomStringGenerator;
import com.tencent.common.Signature;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


/**
 * @author:  JeffChen
 * @date:    2015年2月1日
 * @time:    下午2:40:16
 */
public class NativeOneCallBackRes {
	private String return_code = "";
	private String return_msg = "";
    private String appid = "";
    private String mch_id = "";
    private String nonce_str = "";
    private String prepay_id= "";
    private String result_code = "";
    private String err_code_des = "";
    private String sign = "";
    
    public NativeOneCallBackRes(String return_code,String return_msg,String prepay_id,String result_code,String err_code_des){
    	
    	setReturn_code(return_code);
    	
    	setReturn_msg(return_msg);
    	
    	setAppid(Configure.getAppid());
    	
    	setMch_id(Configure.getMchid());
    	
    	setNonce_str(RandomStringGenerator.getRandomStringByLength(32));
    	
    	setPrepay_id(prepay_id);
    	
    	setResult_code(result_code);
    	
    	setErr_code_des(err_code_des);
    	
    	String sign = Signature.getSign(toMap());
        setSign(sign);
    	
    }
	public String getReturn_code() {
		return return_code;
	}
	public void setReturn_code(String return_code) {
		this.return_code = return_code;
	}
	public String getReturn_msg() {
		return return_msg;
	}
	public void setReturn_msg(String return_msg) {
		this.return_msg = return_msg;
	}
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getMch_id() {
		return mch_id;
	}
	public void setMch_id(String mch_id) {
		this.mch_id = mch_id;
	}
	public String getNonce_str() {
		return nonce_str;
	}
	public void setNonce_str(String nonce_str) {
		this.nonce_str = nonce_str;
	}
	public String getPrepay_id() {
		return prepay_id;
	}
	public void setPrepay_id(String prepay_id) {
		this.prepay_id = prepay_id;
	}
	public String getResult_code() {
		return result_code;
	}
	public void setResult_code(String result_code) {
		this.result_code = result_code;
	}
	public String getErr_code_des() {
		return err_code_des;
	}
	public void setErr_code_des(String err_code_des) {
		this.err_code_des = err_code_des;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	
	 public Map<String,Object> toMap(){
	        Map<String,Object> map = new HashMap<String, Object>();
	        Field[] fields = this.getClass().getDeclaredFields();
	        for (Field field : fields) {
	            Object obj;
	            try {
	                obj = field.get(this);
	                if(obj!=null){
	                    map.put(field.getName(), obj);
	                }
	            } catch (IllegalArgumentException e) {
	                e.printStackTrace();
	            } catch (IllegalAccessException e) {
	                e.printStackTrace();
	            }
	        }
	        return map;
	    }

}
