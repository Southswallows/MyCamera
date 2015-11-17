package org.dlion.mycamera;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Main extends Activity {
	private int parentId = 3;
	private Button btnVideoBrowse;
	private Button btnImgBrowse;
	private Button btnVpn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_camera);   //主屏幕系显示
		btnImgBrowse = (Button) findViewById(R.id.arc_hf_img_btnGridShow); //map_camera的第一按钮“浏览”
		btnImgBrowse.setOnClickListener(new btnListener());

		Button btnCamera = (Button) findViewById(R.id.btnVideo);//map_camera的第二个按钮“拍照/录像”
		btnCamera.setOnClickListener(new btnListener());

		btnVideoBrowse = (Button) findViewById(R.id.arc_hf_video_btnVideoBrowse);//map_camera的第三个按钮“浏览录像”
		btnVideoBrowse.setOnClickListener(new btnListener());//点击监听时间
		showVideoCount();        
		showImgCount();

		
	}

	// 按键监听
	class btnListener implements OnClickListener {   //btnlisterner监听类

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.arc_hf_img_btnGridShow:  //点击“浏览”按钮---imgshow();函数
				imgShow();
				break;
			case R.id.btnVideo:       //点击“拍照/录像”按钮---startrecoeder();函数
				startRecorder();
				break;
			case R.id.arc_hf_video_btnVideoBrowse://点击“浏览 录像”按钮---videoshow();函数
				videoShow();
				break;
			default:
				break;
			}
		}

	}

	// 拍摄成功提示
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  //点击事件返回数据 
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 1:// 拍照回来
			showImgCount();    //前面定义了
			break;
		case 2:// 查看图片后回来
			showImgCount();
			break;
		case 3:// 录像回来
			showImgCount();
			showVideoCount();    //前面已经定义
			break;
		case 4:// 浏览录像回来
			showVideoCount();
		default:
			break;
		}
	}

	/*
	 * 消息提示
	 */
	private Toast toast;
	private String videoPath;  //视频存储路径
	private String imgPath;    //照片存储路径 

	public void showMsg(String arg) {
		if (toast == null) {
			toast = Toast.makeText(this, arg, Toast.LENGTH_SHORT);
		} else {
			toast.cancel();
			toast.setText(arg);  //设置显示的文件
		}
		toast.show();
	}

	// 浏览图片   imgshow()函数----上面引用到 path   id---2
	public void imgShow() {
		Intent intent = new Intent();
		intent.putExtra("path", imgPath);   //第一个参数是键名，第二参数是键值   传递参数用的
		intent.setClass(this, FileShow.class); //setclass页面的跳转，第一个是当前的Activity页面，第二是要跳转的
		startActivityForResult(intent, 2);   //  返回的id=2
	}

	// 图片数量  showImgCount()函数，上面提到
	private void showImgCount() {
		imgPath = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/hfdatabase/img/" + String.valueOf(parentId) + "/";
		File file = new File(imgPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		File[] files = file.listFiles();
		int fileCount = files.length;
		if (fileCount == 0) {
			btnImgBrowse.setEnabled(false); //如果没有图片内容，“浏览”按钮不能用
		} else {
			btnImgBrowse.setEnabled(true);   //反之能用
		}
		btnImgBrowse.setText("浏览图片(" + fileCount + ")");  //有内容的话，显示的是“浏览图片”+（几张）
	}

	/**
	 * 录像  startRecoder()函数 parentID  id ---3
	 */
	public void startRecorder() {
		Intent intent = new Intent();
		intent.setClass(Main.this, MCamera.class);  //跳转界面  从这个跳刀MCamera
		intent.putExtra("parentId", parentId);
		startActivityForResult(intent, 3);   //返回id=3
	}

	/**
	 * 浏览录像  videoShow()函数----id=4
	 */
	public void videoShow() {
		Intent intent = new Intent();
		intent.putExtra("path", videoPath); 
		intent.setClass(Main.this, FileShow.class); //跳转到fileshow
		startActivityForResult(intent, 4);
	}

	/**
	 * 录像数量   ----showVideoCount()函数
	 */
	public void showVideoCount() {
		videoPath = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/hfdatabase/video/" + String.valueOf(parentId) + "/";
		File file = new File(videoPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		File[] files = file.listFiles();
		int fileCount = files.length;
		if (fileCount == 0) {
			btnVideoBrowse.setEnabled(false);
		} else {
			btnVideoBrowse.setEnabled(true);
		}
		btnVideoBrowse.setText("浏览录像(" + fileCount + ")");
	}
}