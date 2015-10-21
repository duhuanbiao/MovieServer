package com.person.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * 缃戠粶搴旂敤绫�
 */
public class NetUtils {
	private static final String tag = NetUtils.class.getName();

	/**
	 * 褰撳墠缃戠粶鏄惁宸茶繛鎺�
	 * 
	 * @return true鏍囩ず宸茶繛鎺ワ紝false鏍囩ず娌℃湁
	 */
	public static boolean isNetConnected(Context context) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				// 鑾峰彇缃戠粶杩炴帴绠＄悊鐨勫璞�
				NetworkInfo[] infos = connectivity.getAllNetworkInfo();
				if (infos != null) {
					for (NetworkInfo info : infos) {
						if (info != null && info.isConnected()) {
							// 鍒ゆ柇褰撳墠缃戠粶鏄惁宸茬粡杩炴帴
							if (info.getState() == NetworkInfo.State.CONNECTED) {
								return true;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 褰撳墠3鏄惁杩炴帴
	 * 
	 * @return true鏍囩ず宸茶繛鎺ワ紝false鏍囩ず娌℃湁
	 */
	public static boolean isIn3GConnect(Context context) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				// 鑾峰彇缃戠粶杩炴帴绠＄悊鐨勫璞�
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null
						&& info.isConnected() // 鏄惁杩炴帴
						&& info.getType() != ConnectivityManager.TYPE_WIFI
						&& info.getType() != 9 /* TYPE_ETHERNET */) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}
	
	public static String getMacAddress(Context context) {
		String result = "";     
	     String Mac = "";
	     result = callCmd("busybox ifconfig","HWaddr");
	      
	     if(result!=null){
	    	 //瀵硅琛屾暟鎹繘琛岃В鏋�
		     //渚嬪锛歟th0      Link encap:Ethernet  HWaddr 00:16:E8:3E:DF:67
		     if(result.length()>0 && result.contains("HWaddr")==true){
		         Mac = result.substring(result.indexOf("HWaddr")+6, result.length()-1);
		         Log.i("test","Mac:"+Mac+" Mac.length: "+Mac.length());
		          
		         if(Mac.length()>1){
		             Mac = Mac.replaceAll(" ", "");
		             result = "";
		             String[] tmp = Mac.split(":");
		             for(int i = 0;i<tmp.length;++i){
		                 result +=tmp[i];
		             }
		         }
		         return result;
		     }
	     } else {
	    	 WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	    	 
	    	 WifiInfo info = wifi.getConnectionInfo();
	    	  
	    	 return info.getMacAddress();
	     }
	     
	     return "";
	}
	
	private static String callCmd(String cmd,String filter) {   
	     String result = "";   
	     String line = "";   
	     try {
	         Process proc = Runtime.getRuntime().exec(cmd);
	         InputStreamReader is = new InputStreamReader(proc.getInputStream());   
	         BufferedReader br = new BufferedReader (is);   
	          
	         //鎵ц鍛戒护cmd锛屽彧鍙栫粨鏋滀腑鍚湁filter鐨勮繖涓�琛�
	         while ((line = br.readLine ()) != null && line.contains(filter)== false) {   
	             //result += line;
	             Log.i("test","line: "+line);
	         }
	          
	         result = line;
	         Log.i("test","result: "+result);
	     }   
	     catch(Exception e) {   
	         e.printStackTrace();   
	     }   
	     return result;   
	 }
}
