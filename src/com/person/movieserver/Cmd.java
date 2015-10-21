package com.person.movieserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.net.Uri;

public class Cmd {
	public static final int CMD_MUTE 	= 1;

	public static final int CMD_OPEN 	= 2;
	
	public static final int CMD_PLAY 	= 3;
	
	public static final int CMD_PAUSE 	= 4;
	
	public static final int CMD_V_ADD 	= 5;
	
	public static final int CMD_V_SUB 	= 6;
	
	public static final int CMD_PREV 	= 7;
	
	public static final int CMD_NEXT 	= 8;
	
	public static final int CMD_STOP 	= 9;
	
	
	public static final void sendCmd(String url, int cmd, String param){
		if (param == null){
			param = "";
		}
		Uri dstfetch = Uri.parse(url);
		final String realUrl = dstfetch.buildUpon()
					.appendQueryParameter("cmd", "" + cmd)
					.appendQueryParameter("param", param)
					.build()
					.toString();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try{
					  URL getUrl = new URL(realUrl); 
		               // 根据拼凑的URL，打开连接，URL.openConnection()函数会根据 URL的类型，返回不同的URLConnection子类的对象，在这里我们的URL是一个http，因此它实际上返回的是HttpURLConnection 
		               HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection(); 

		               // 建立与服务器的连接，并未发送数据 
		               connection.connect(); 

		               // 发送数据到服务器并使用Reader读取返回的数据 
		               BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())); 

		               String lines; 
		               while ((lines = reader.readLine()) != null) { 
		                  System.out.println(lines); 
		               } 

		               reader.close(); 
		               // 断开连接 
		               connection.disconnect(); 
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}).start();
	}
}
