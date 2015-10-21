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
		               // ����ƴ�յ�URL�������ӣ�URL.openConnection()��������� URL�����ͣ����ز�ͬ��URLConnection����Ķ������������ǵ�URL��һ��http�������ʵ���Ϸ��ص���HttpURLConnection 
		               HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection(); 

		               // ����������������ӣ���δ�������� 
		               connection.connect(); 

		               // �������ݵ���������ʹ��Reader��ȡ���ص����� 
		               BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())); 

		               String lines; 
		               while ((lines = reader.readLine()) != null) { 
		                  System.out.println(lines); 
		               } 

		               reader.close(); 
		               // �Ͽ����� 
		               connection.disconnect(); 
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}).start();
	}
}
