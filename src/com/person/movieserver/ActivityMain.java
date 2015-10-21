package com.person.movieserver;
import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.person.movieserver.R;
import com.person.thirdparty.image.FailReason;
import com.person.thirdparty.image.ImageLoader;
import com.person.thirdparty.image.ImageLoaderConfiguration;
import com.person.thirdparty.image.ImageLoadingListener;
import com.person.tools.NetUtils;
import com.person.tools.WifiApManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.VideoView;
import android.widget.ViewSwitcher.ViewFactory;


public class ActivityMain extends Activity {
	private VideoView mVideoView;
	private ImageSwitcher mImageSwitcher;
	
	//音量
	private boolean mbMute = false;
	private int mCurVolumn = 0;
	
	//控制台
	MediaController mMediaController;
	
	//http服务
	private NanoHTTPD mNanoHttpd;
	
	//当前是不是在显示屏保
	private boolean mbIsInScreenSaver = false;
	
	private List<String> mPictures = new ArrayList<String>();
	
    private int mCurImg = 0;
    
    private Handler mHandler = new Handler();
    
    private static final int CONTROLLER_SHOWING_TIME = 5000;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		Log.d("duhuanbiao", "onCreate");
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
//		openWifi();
		
		initView();
		
		ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
		
		processDefaultConfig();
		
		startServer();
		
		AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		mCurVolumn = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	
		mVideoView.postDelayed(mWifiAPDelay, 3000);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		mVideoView.stopPlayback();
		mVideoView.removeCallbacks(mWifiAPDelay);
		mVideoView = null;
		mHandler.removeCallbacks(imageSwitchRunable);
		stopServer();
	}
	
	//不容许退出
	@Override
	public void onBackPressed() {
	}
	
	//延迟打开wifi ap
	private Runnable mWifiAPDelay = new Runnable() {
		@Override
		public void run() {
			if (NetUtils.isNetConnected(getApplicationContext())){
				Log.d("duhuanbiao", "网络连接完成，打开ap");
				openWifi();
			}else{
				mVideoView.postDelayed(mWifiAPDelay, 2000);
			}
		}
	};
	
	private void processDefaultConfig(){
		if (!FileUtils.isSdcardExist()){
			showRetryDialog("SD卡不存在！");
			return;
		}
		
		String sdDir = FileUtils.getSdcardPath();
		if (!FileUtils.isFileExist(sdDir + "coffeemovie/startup.conf")){
			showRetryDialog("启动配置不存在");
			return;
		}
		
		if (!FileUtils.isFileExist(sdDir + "coffeemovie/screen.conf")){
			showRetryDialog("屏保配置不存在");
			return;
		}
		
		//read screen saver
		List<String> pictures = FileUtils.readFileToList(sdDir + "coffeemovie/screen.conf");
		if (pictures != null){
			//填充完整路径
			for (String path:pictures){
				if (path.startsWith("http://") 
					|| path.startsWith("HTTP://") 
					|| path.startsWith("file://")
					|| path.startsWith("FILE://")){
				}else{
					path = "file://" + sdDir + "coffeemovie" + File.separator + path;
				}
				mPictures.add(path);
			}			
		}
		
		//read startup config
		List<String> videos = FileUtils.readFileToList(sdDir + "coffeemovie/startup.conf");
		if (videos != null && videos.size() > 0){
			String path = videos.get(0);
			if (path.startsWith("http://") 
					|| path.startsWith("HTTP://") 
					|| path.startsWith("file://")
					|| path.startsWith("FILE://")){
			}else{
				path = sdDir + "coffeemovie" + File.separator + path;
			}
			
			processCmd(Cmd.CMD_OPEN, path);
		}else{
			startScreenSaver();
		}
	}
	
	//错误重试
	private void showRetryDialog(String msg){
		new AlertDialog.Builder(this)
			.setTitle("发生错误")
			.setMessage(msg)
			.setPositiveButton("重试", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					processDefaultConfig();
				}
			}).create().show();
	}
	
	private void startServer() {
		mNanoHttpd = new MyNanoHTTPD(8080);
		try{
			mNanoHttpd.start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void stopServer() {
		try{
			mNanoHttpd.stop();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private boolean openWifi(){
		WifiApManager manage = new WifiApManager(getApplicationContext());
		
		WifiConfiguration config = manage.getWifiApConfiguration();
		
		Log.d("duhuanbiao", "ssid=" + config.SSID + ";pwd=" + config.preSharedKey);
		
		manage.setWifiApEnabled(config, true);
		return true;
	}

	private void initView() {
		mVideoView = (VideoView) findViewById(R.id.videoView);
		mMediaController = new MediaController(this);
		mVideoView.setMediaController(mMediaController);
		mMediaController.setMediaPlayer(mVideoView);
		
		mVideoView.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				postMessage("播放出错，现在推出播放器！");
				mVideoView.stopPlayback();
				startScreenSaver();
				return false;
			}
		});
		
		mVideoView.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				mVideoView.stopPlayback();
				startScreenSaver();
			}
		});
		
		mVideoView.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();
			}
		});
		
		
		mImageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitch);
		
		  // 实现并设置工厂内部接口的makeView方法，用来显示视图。  
		mImageSwitcher.setFactory(new ViewFactory() {  
  
            public View makeView() {  
            	ImageView ivImage = new ImageView(ActivityMain.this);
            	ivImage.setScaleType(ScaleType.FIT_XY);
            	ivImage.setLayoutParams(new ImageSwitcher.LayoutParams(ImageSwitcher.LayoutParams.MATCH_PARENT, 
            			ImageSwitcher.LayoutParams.MATCH_PARENT));
                return ivImage;  
            }  
        }); 
		
		// 设置切入动画  
		mImageSwitcher.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(),  
                R.anim.fade_in));  
        // 设置切出动画  
		mImageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(  
                getApplicationContext(), R.anim.fade_out));
	}
	
	private Runnable imageSwitchRunable = new Runnable() {
		
		@Override
		public void run() {
			mCurImg++;
			if (mCurImg >= mPictures.size()){
				mCurImg = 0;
			}
			
			ImageLoader.getInstance().loadImage(mPictures.get(mCurImg), new ImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
				}
				
				@Override
				public void onLoadingFailed(String imageUri, View view,
						FailReason failReason) {
					mHandler.post(imageSwitchRunable);
				}
				
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					Drawable drawable = new BitmapDrawable(loadedImage);  
					mImageSwitcher.setImageDrawable(drawable);
					mHandler.postDelayed(imageSwitchRunable, 3000);
				}
				
				@Override
				public void onLoadingCancelled(String imageUri, View view) {
					mHandler.post(imageSwitchRunable);
				}
			});
		}
	};
	
	private void startScreenSaver(){
		if (mPictures.size() == 0){
			return;
		}
		
		Log.d("duhuanbiao", "startScreenSaver");
		
		mbIsInScreenSaver = true;
		
		mVideoView.setVisibility(View.INVISIBLE);
		
		mImageSwitcher.setVisibility(View.VISIBLE);
		mHandler.post(imageSwitchRunable);
	}
	
	private void stopScreenSaver(){
		Log.d("duhuanbiao", "stopScreenSaver");
		
		mbIsInScreenSaver = false;
		mVideoView.setVisibility(View.VISIBLE);
		mImageSwitcher.setVisibility(View.GONE);
		mHandler.removeCallbacks(imageSwitchRunable);
	}
	
	private void postMessage(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	
	private void parseParam(Map<String, String> paramMap){
		String tmp = paramMap.get("cmd");
		final int cmd;
		if (TextUtils.isDigitsOnly(tmp)){
			cmd = Integer.parseInt(tmp);
		}else{
			postMessage("无效的命令");
			return;
		}
		
		tmp = paramMap.get("param");
		if (TextUtils.isEmpty(tmp)){
			tmp = "";
		}else{
			tmp = URLDecoder.decode(tmp);
		}
		
		final String param = tmp;
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				processCmd(cmd, param);					
			}
		});
	}
	
	private void processCmd(int cmd, String param){
		Log.d("duhuanbiao", "cmd=" + cmd + ";param=" + param);
		
		if (isFinishing()){
			Log.d("duhuanbiao", "没有界面，直接忽略");
			return;
		}
		
		if (cmd != Cmd.CMD_OPEN && mbIsInScreenSaver){
			Log.d("duhuanbiao", "没有在播放，直接退出");
			return;
		}
		
		int paramInt = 0;
		if (!TextUtils.isEmpty(param.trim()) && TextUtils.isDigitsOnly(param.trim())){
			paramInt = Integer.parseInt(param.trim());
		}
		
		switch(cmd){
		case Cmd.CMD_MUTE:
		{
			mMediaController.show(CONTROLLER_SHOWING_TIME);
			mbMute = !mbMute;
			AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
			audioManager.setStreamMute(AudioManager.STREAM_MUSIC, mbMute);
		}
			break;
		case Cmd.CMD_OPEN:
			mMediaController.show(CONTROLLER_SHOWING_TIME);
			if (!TextUtils.isEmpty(param)){
				stopScreenSaver();
				mVideoView.setVideoPath(param);
				mVideoView.start();
			}else{
				postMessage("无效的播放串");
			}
			
			
			break;
		case Cmd.CMD_PLAY:
			if (mVideoView.isPlaying()){
				mVideoView.pause();
			}else{
				mVideoView.start();
			}
			break;
		case Cmd.CMD_PAUSE:
			mMediaController.show(CONTROLLER_SHOWING_TIME);
			mVideoView.pause();
			break;
		case Cmd.CMD_V_ADD:
		{
			mMediaController.show(CONTROLLER_SHOWING_TIME);
			if (paramInt <= 0){
				paramInt = 1;
			}
			
			mCurVolumn += paramInt;
			if (mCurVolumn > 100){
				mCurVolumn = 100;
			}
			AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
			mbMute = false;
			audioManager.setStreamMute(AudioManager.STREAM_MUSIC, mbMute);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 
					mCurVolumn * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100, AudioManager.FLAG_SHOW_UI);
		}
			break;
		case Cmd.CMD_V_SUB:
		{
			mMediaController.show(CONTROLLER_SHOWING_TIME);
			if (paramInt <= 0){
				paramInt = 1;
			}
			
			mCurVolumn -= paramInt;
			if (mCurVolumn < 0){
				mCurVolumn = 0;
			}
			AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
			mbMute = false;
			audioManager.setStreamMute(AudioManager.STREAM_MUSIC, mbMute);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 
					mCurVolumn * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 100, AudioManager.FLAG_SHOW_UI);
		}
			break;
		case Cmd.CMD_PREV:
		{
			mMediaController.show(CONTROLLER_SHOWING_TIME);
			if (paramInt <= 0){
				paramInt = 10;
			}
			
			int cur = mVideoView.getCurrentPosition() / 1000;
			cur -= paramInt;
			if (cur < 0){
				cur = 0;
			}
			
			mVideoView.seekTo(cur * 1000);			
		}
			break;
		case Cmd.CMD_NEXT:
		{
			mMediaController.show(CONTROLLER_SHOWING_TIME);
			if (paramInt <= 0){
				paramInt = 10;
			}
			
			int cur = mVideoView.getCurrentPosition() / 1000;
			cur += paramInt;
			if (cur >= mVideoView.getDuration() / 1000){
				cur = mVideoView.getDuration() / 1000 - 1;
			}
			
			mVideoView.seekTo(cur * 1000);			
		}
			break;
		case Cmd.CMD_STOP:
			mVideoView.stopPlayback();
			startScreenSaver();
			break;
		}
	}
	
	class MyNanoHTTPD extends NanoHTTPD{

		public MyNanoHTTPD(int port) {
			super(port);
		}
		
		public MyNanoHTTPD(String hostName,int port){
			super(hostName,port);
		}
		
		 public Response serve(IHTTPSession session) { 
			 Method method = session.getMethod();
			 if(NanoHTTPD.Method.GET.equals(method)){
				 parseParam(session.getParms());
				 return new Response("ok");
			 }else if(NanoHTTPD.Method.POST.equals(method)){
				 //post方式
			 }
			 return super.serve(session);
		 }
	}
}
