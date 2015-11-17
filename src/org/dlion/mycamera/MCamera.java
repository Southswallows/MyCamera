package org.dlion.mycamera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MCamera extends Activity {
	private Button mVideoStartBtn;
	private SurfaceView mSurfaceview;
	private MediaRecorder mMediaRecorder;
	private SurfaceHolder mSurfaceHolder;
	private File mRecVedioPath;
	private File mRecAudioFile;
	private TextView timer;
	private int hour = 0;
	private int minute = 0;
	private int second = 0;
	private boolean bool;    //Ĭ��ֵΪfalse
	private int parentId;
	protected Camera camera;
	protected boolean isPreview;   //�Ƿ���Ԥ��  Ĭ��false
	private Drawable iconStart;
	private Drawable iconStop;
	private boolean isRecording = true; // true��ʾû��¼�񣬵����ʼ��false��ʾ����¼�񣬵����ͣ

	@Override
	protected void onCreate(Bundle savedInstanceState) {   //��296�У������ս���
		super.onCreate(savedInstanceState);
		/*
		 * ����ȫ����ʾ
		 */
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); //û��̧ͷ�ı���
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);  //ʹ����֧��͸����
		setContentView(R.layout.map_video);         //��ʾmap_video����
		iconStart = getResources().getDrawable(
				R.drawable.arc_hf_btn_video_start);  //����¼�������İ�ťͼ��
		iconStop = getResources().getDrawable(R.drawable.arc_hf_btn_video_stop); //��������¼�Ƶ�ֹͣ��ť

		parentId = getIntent().getIntExtra("parentId", 0);  //��Intent.putExtra����ϵ�ģ���������activity
															//����һ���Ǵ��ݵĲ������֣��ڶ�����activity��ȡ����ֵ��
		timer = (TextView) findViewById(R.id.arc_hf_video_timer); //timer��map_video�е�textview���Ӧ
		mVideoStartBtn = (Button) findViewById(R.id.arc_hf_video_start);//��map_video�еĵ�һ��button
		mSurfaceview = (SurfaceView) this.findViewById(R.id.arc_hf_video_view);//��map_video�е�surfaceview

		// ���ü�ʱ�����ɼ�
		timer.setVisibility(View.GONE);

		// ���û���·��-----��/hfdatabase/video/temp/
		mRecVedioPath = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/hfdatabase/video/temp/");
		if (!mRecVedioPath.exists()) {
			mRecVedioPath.mkdirs();
		}

		// ��Ԥ����ͼ----surfaceview����Ƶʵʱ��ʾ    
		SurfaceHolder holder = mSurfaceview.getHolder();
		holder.addCallback(new Callback() {    //��127�н���

			@Override//���л�������activityʱ��sufaceview destory
			public void surfaceDestroyed(SurfaceHolder holder) {  //sufaceview destory
				if (camera != null) {
					if (isPreview) {
						camera.stopPreview();
						isPreview = false;
					}
					camera.release();
					camera = null; // �ǵ��ͷ���� ----release
				}
				mSurfaceview = null;
				mSurfaceHolder = null;    //��surfaceview��surfaceholder��mMediaRecord ���ͷ�
				mMediaRecorder = null;
			}

			//surfaceCreated
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				try {
					camera = Camera.open();
					Camera.Parameters parameters = camera.getParameters();  //���ò���
					parameters.setPreviewFrameRate(25); // ÿ��5֡---�������޸ģ�����֡��
					parameters.setPictureFormat(PixelFormat.JPEG);// ������Ƭ�������ʽ
					parameters.set("jpeg-quality", 90);// ��Ƭ����---���Ե���
					camera.setParameters(parameters);
					camera.setPreviewDisplay(holder);
					camera.startPreview();
					isPreview = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
				mSurfaceHolder = holder;
			}

			//surfacechanged
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				mSurfaceHolder = holder;
			}
		});
		
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		mVideoStartBtn.setOnClickListener(new Button.OnClickListener() {  //��220�н���videostart�����¼�

			@Override
			public void onClick(View v) {
				if (isRecording) {
					/*
					 * �����ʼ¼��
					 */
					if (isPreview) {
						camera.stopPreview();
						camera.release();
						camera = null;
					}
					second = 0;
					minute = 0;
					hour = 0;
					bool = true;
					if (mMediaRecorder == null){
						mMediaRecorder = new MediaRecorder();
					}else{
						mMediaRecorder.reset();
					}
					mMediaRecorder.setPreviewDisplay(mSurfaceHolder
							.getSurface());
					mMediaRecorder
							.setVideoSource(MediaRecorder.VideoSource.CAMERA); //video
					mMediaRecorder
							.setAudioSource(MediaRecorder.AudioSource.MIC);  //Audio
					mMediaRecorder
							.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); //�����ʽ������mp4(MPEG4)
					mMediaRecorder
							.setVideoEncoder(MediaRecorder.VideoEncoder.H264);  //��Ƶ����H264
					mMediaRecorder
							.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);//��Ƶ����ARM����NB
					mMediaRecorder.setVideoSize(480, 800);   //������Ƶ��С
					mMediaRecorder.setVideoFrameRate(25);   //����֡��15----��֮ǰ��preview�Ĳ�ͬ
					try {
						mRecAudioFile = File.createTempFile("Vedio", ".mp4",
								mRecVedioPath);                                //��ʱ��Ƶ�ļ�
					} catch (IOException e) {
						e.printStackTrace();
					}
					mMediaRecorder.setOutputFile(mRecAudioFile        //�����Ƶ�ļ�
							.getAbsolutePath());
					try {
						mMediaRecorder.prepare();
						timer.setVisibility(View.VISIBLE);
						handler.postDelayed(task, 1000);     //��ʱ����ʱ��1000ms=1s
						mMediaRecorder.start();
					} catch (Exception e) {
						e.printStackTrace();
					}
					showMsg("��ʼ¼��");         //��ʾ����ʼ¼��
					mVideoStartBtn.setBackgroundDrawable(iconStop);   //��Ƶ¼��ͼ���Ϊֹͣ��ͼ��
					isRecording = !isRecording;                 //�ѿ�ʼ¼�Ʊ�Ϊ  ��¼��
				} else {
					/*
					 * ���ֹͣ
					 */
					try {
						bool = false;
						mMediaRecorder.stop();                 //¼��ֹͣ
						timer.setText(format(hour) + ":" + format(minute) + ":"
								+ format(second));           //��ʾʱ��
						mMediaRecorder.release();            //�ͷ���Ƶ¼��
						mMediaRecorder = null;
						videoRename();                     //video ������
					} catch (Exception e) {
						e.printStackTrace();
					}
					isRecording = !isRecording;           
					mVideoStartBtn.setBackgroundDrawable(iconStart);  //¼��ͼ�껹ԭΪԭ��ͼ��
					showMsg("¼����ɣ��ѱ���");
					timer.setVisibility(View.GONE);
					//������Ƶ֮���ٽ���preview
					try {
						camera = Camera.open();
						Camera.Parameters parameters = camera.getParameters();
						parameters.setPreviewFrameRate(25); // ÿ��5֡-----���Ը���Ϊ25
						parameters.setPictureFormat(PixelFormat.JPEG);// ������Ƭ�������ʽ
						parameters.set("jpeg-quality", 90);// ��Ƭ����
						camera.setParameters(parameters);
						camera.setPreviewDisplay(mSurfaceHolder);
						camera.startPreview();
						isPreview = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		Button btnImgStart = (Button) findViewById(R.id.arc_hf_img_start); //���հ�ť
		btnImgStart.setOnClickListener(new OnClickListener() {   //��300��

			@Override
			public void onClick(View v) {
//				if (mMediaRecorder != null) {
//					try {
//						bool = false;
//						mMediaRecorder.stop();
//						timer.setText(format(hour) + ":" + format(minute) + ":"
//								+ format(second));
//						mMediaRecorder.release();
//						mMediaRecorder = null;
//						videoRename();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					isRecording = !isRecording;
//					mVideoStartBtn.setBackgroundDrawable(iconStart);
//					showMsg("¼����ɣ��ѱ���");

					try {
						camera = Camera.open();
						Camera.Parameters parameters = camera.getParameters();
						parameters.setPreviewFrameRate(25); // ÿ��5֡
						parameters.setPictureFormat(PixelFormat.JPEG);// ������Ƭ�������ʽ
						parameters.set("jpeg-quality", 90);// ��Ƭ����
						camera.setParameters(parameters);
						camera.setPreviewDisplay(mSurfaceHolder);
						camera.startPreview();
						isPreview = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
//				}
				if (camera != null) {
					camera.autoFocus(null);
					camera.takePicture(null, null, new PictureCallback() {
						@Override
						public void onPictureTaken(byte[] data, Camera camera) {
							Bitmap bitmap = BitmapFactory.decodeByteArray(data,
									0, data.length);                       //ͼƬ����
							Matrix matrix = new Matrix();
							// ��������----ʲô�ã�
							matrix.postScale(5f, 4f);  //���5�����߶�4��
							bitmap = Bitmap.createBitmap(bitmap, 0, 0,
									bitmap.getWidth(), bitmap.getHeight(),
									matrix, true);

							String path = Environment
									.getExternalStorageDirectory()
									.getAbsolutePath()
									+ "/hfdatabase/img/"
									+ String.valueOf(parentId) + "/";
							String fileName = new SimpleDateFormat(
									"yyyyMMddHHmmss").format(new Date())
									+ ".jpg";
							File out = new File(path);
							if (!out.exists()) {
								out.mkdirs();
							}
							out = new File(path, fileName);
							try {
								FileOutputStream outStream = new FileOutputStream(  //ͼƬ�ı���
										out);
								bitmap.compress(CompressFormat.JPEG, 100,
										outStream);    //ͼƬѹ����100��ʾѹ��
								outStream.close();
								camera.startPreview();
							} catch (Exception e) {
								e.printStackTrace();
							}
//							showMsg("���ճɹ�");
						}
					}); // ����
				}
				showMsg("���ճɹ�");	
			}
		});
	}

	/*
	 * ��Ϣ��ʾ  showMsg��������
	 */
	private Toast toast;

	public void showMsg(String arg) {                //showMsg��������
		if (toast == null) {
			toast = Toast.makeText(this, arg, Toast.LENGTH_SHORT);  //��ʾ����ʱ��
		} else {
			toast.cancel();
			toast.setText(arg);
		}
		toast.show();
	}

	/*
	 * ����video�ļ�����   video������
	 */
	protected void videoRename() {
		String path = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ "/hfdatabase/video/"
				+ String.valueOf(parentId) + "/";
		String fileName = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(new Date()) + ".mp4";
		File out = new File(path);
		if (!out.exists()) {
			out.mkdirs();
		}
		out = new File(path, fileName);
		if (mRecAudioFile.exists())
			mRecAudioFile.renameTo(out);
	}

	/*
	 *  ��ʱ�����ã�ʵ�ּ�ʱ   ��ʱ����ʵ��---�������ߵ�
	 */
	private Handler handler = new Handler();
	private Runnable task = new Runnable() {
		public void run() {
			if (bool) {
				handler.postDelayed(this, 1000);
				second++;
				if (second >= 60) {
					minute++;
					second = second % 60;
				}
				if (minute >= 60) {
					hour++;
					minute = minute % 60;
				}
				timer.setText(format(hour) + ":" + format(minute) + ":"
						+ format(second));
			}
		}
	};

	/*
	 * ��ʽ��ʱ��
	 */
	public String format(int i) {
		String s = i + "";
		if (s.length() == 1) {
			s = "0" + s;
		}
		return s;
	}

	/*
	 * ��д���ؼ�����    �ֻ��Դ��ķ��ؼ�ʹ��
	 */
	@Override
	public void onBackPressed() {
		if (mMediaRecorder != null) {
			mMediaRecorder.stop();
			mMediaRecorder.release();
			mMediaRecorder = null;
			videoRename();
		}
		finish();
	}

	//���ڵ��е绰���룬��������Ƶ���˳�
	
	@Override
	protected void onPause() {
		super.onPause();
		onBackPressed();
	}
}