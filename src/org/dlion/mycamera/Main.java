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
		setContentView(R.layout.map_camera);   //����Ļϵ��ʾ
		btnImgBrowse = (Button) findViewById(R.id.arc_hf_img_btnGridShow); //map_camera�ĵ�һ��ť�������
		btnImgBrowse.setOnClickListener(new btnListener());

		Button btnCamera = (Button) findViewById(R.id.btnVideo);//map_camera�ĵڶ�����ť������/¼��
		btnCamera.setOnClickListener(new btnListener());

		btnVideoBrowse = (Button) findViewById(R.id.arc_hf_video_btnVideoBrowse);//map_camera�ĵ�������ť�����¼��
		btnVideoBrowse.setOnClickListener(new btnListener());//�������ʱ��
		showVideoCount();        
		showImgCount();

		
	}

	// ��������
	class btnListener implements OnClickListener {   //btnlisterner������

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.arc_hf_img_btnGridShow:  //������������ť---imgshow();����
				imgShow();
				break;
			case R.id.btnVideo:       //���������/¼�񡱰�ť---startrecoeder();����
				startRecorder();
				break;
			case R.id.arc_hf_video_btnVideoBrowse://�������� ¼�񡱰�ť---videoshow();����
				videoShow();
				break;
			default:
				break;
			}
		}

	}

	// ����ɹ���ʾ
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  //����¼��������� 
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 1:// ���ջ���
			showImgCount();    //ǰ�涨����
			break;
		case 2:// �鿴ͼƬ�����
			showImgCount();
			break;
		case 3:// ¼�����
			showImgCount();
			showVideoCount();    //ǰ���Ѿ�����
			break;
		case 4:// ���¼�����
			showVideoCount();
		default:
			break;
		}
	}

	/*
	 * ��Ϣ��ʾ
	 */
	private Toast toast;
	private String videoPath;  //��Ƶ�洢·��
	private String imgPath;    //��Ƭ�洢·�� 

	public void showMsg(String arg) {
		if (toast == null) {
			toast = Toast.makeText(this, arg, Toast.LENGTH_SHORT);
		} else {
			toast.cancel();
			toast.setText(arg);  //������ʾ���ļ�
		}
		toast.show();
	}

	// ���ͼƬ   imgshow()����----�������õ� path   id---2
	public void imgShow() {
		Intent intent = new Intent();
		intent.putExtra("path", imgPath);   //��һ�������Ǽ������ڶ������Ǽ�ֵ   ���ݲ����õ�
		intent.setClass(this, FileShow.class); //setclassҳ�����ת����һ���ǵ�ǰ��Activityҳ�棬�ڶ���Ҫ��ת��
		startActivityForResult(intent, 2);   //  ���ص�id=2
	}

	// ͼƬ����  showImgCount()�����������ᵽ
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
			btnImgBrowse.setEnabled(false); //���û��ͼƬ���ݣ����������ť������
		} else {
			btnImgBrowse.setEnabled(true);   //��֮����
		}
		btnImgBrowse.setText("���ͼƬ(" + fileCount + ")");  //�����ݵĻ�����ʾ���ǡ����ͼƬ��+�����ţ�
	}

	/**
	 * ¼��  startRecoder()���� parentID  id ---3
	 */
	public void startRecorder() {
		Intent intent = new Intent();
		intent.setClass(Main.this, MCamera.class);  //��ת����  ���������MCamera
		intent.putExtra("parentId", parentId);
		startActivityForResult(intent, 3);   //����id=3
	}

	/**
	 * ���¼��  videoShow()����----id=4
	 */
	public void videoShow() {
		Intent intent = new Intent();
		intent.putExtra("path", videoPath); 
		intent.setClass(Main.this, FileShow.class); //��ת��fileshow
		startActivityForResult(intent, 4);
	}

	/**
	 * ¼������   ----showVideoCount()����
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
		btnVideoBrowse.setText("���¼��(" + fileCount + ")");
	}
}