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
	private boolean bool;    //默认值为false
	private int parentId;
	protected Camera camera;
	protected boolean isPreview;   //是否在预览  默认false
	private Drawable iconStart;
	private Drawable iconStop;
	private boolean isRecording = true; // true表示没有录像，点击开始；false表示正在录像，点击暂停

	@Override
	protected void onCreate(Bundle savedInstanceState) {   //到296行，即拍照结束
		super.onCreate(savedInstanceState);
		/*
		 * 设置全屏显示
		 */
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); //没有抬头的标题
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);  //使窗口支持透明度
		setContentView(R.layout.map_video);         //显示map_video窗口
		iconStart = getResources().getDrawable(
				R.drawable.arc_hf_btn_video_start);  //设置录制视屏的按钮图标
		iconStop = getResources().getDrawable(R.drawable.arc_hf_btn_video_stop); //社朱视屏录制的停止按钮

		parentId = getIntent().getIntExtra("parentId", 0);  //和Intent.putExtra相联系的，用来启动activity
															//（第一个是传递的参数名字，第二个是activity能取到的值）
		timer = (TextView) findViewById(R.id.arc_hf_video_timer); //timer和map_video中的textview相对应
		mVideoStartBtn = (Button) findViewById(R.id.arc_hf_video_start);//和map_video中的第一个button
		mSurfaceview = (SurfaceView) this.findViewById(R.id.arc_hf_video_view);//和map_video中的surfaceview

		// 设置计时器不可见
		timer.setVisibility(View.GONE);

		// 设置缓存路径-----在/hfdatabase/video/temp/
		mRecVedioPath = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/hfdatabase/video/temp/");
		if (!mRecVedioPath.exists()) {
			mRecVedioPath.mkdirs();
		}

		// 绑定预览视图----surfaceview的视频实时显示    
		SurfaceHolder holder = mSurfaceview.getHolder();
		holder.addCallback(new Callback() {    //到127行结束

			@Override//在切换到其他activity时，sufaceview destory
			public void surfaceDestroyed(SurfaceHolder holder) {  //sufaceview destory
				if (camera != null) {
					if (isPreview) {
						camera.stopPreview();
						isPreview = false;
					}
					camera.release();
					camera = null; // 记得释放相机 ----release
				}
				mSurfaceview = null;
				mSurfaceHolder = null;    //将surfaceview、surfaceholder、mMediaRecord 都释放
				mMediaRecorder = null;
			}

			//surfaceCreated
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				try {
					camera = Camera.open();
					Camera.Parameters parameters = camera.getParameters();  //设置参数
					parameters.setPreviewFrameRate(25); // 每秒5帧---可以做修改，增加帧数
					parameters.setPictureFormat(PixelFormat.JPEG);// 设置照片的输出格式
					parameters.set("jpeg-quality", 90);// 照片质量---可以调高
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

		mVideoStartBtn.setOnClickListener(new Button.OnClickListener() {  //到220行结束videostart监听事件

			@Override
			public void onClick(View v) {
				if (isRecording) {
					/*
					 * 点击开始录像
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
							.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); //输出格式，可以mp4(MPEG4)
					mMediaRecorder
							.setVideoEncoder(MediaRecorder.VideoEncoder.H264);  //视频编码H264
					mMediaRecorder
							.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);//音频编码ARM——NB
					mMediaRecorder.setVideoSize(480, 800);   //设置视频大小
					mMediaRecorder.setVideoFrameRate(25);   //设置帧率15----和之前的preview的不同
					try {
						mRecAudioFile = File.createTempFile("Vedio", ".mp4",
								mRecVedioPath);                                //临时视频文件
					} catch (IOException e) {
						e.printStackTrace();
					}
					mMediaRecorder.setOutputFile(mRecAudioFile        //输出视频文件
							.getAbsolutePath());
					try {
						mMediaRecorder.prepare();
						timer.setVisibility(View.VISIBLE);
						handler.postDelayed(task, 1000);     //定时器延时，1000ms=1s
						mMediaRecorder.start();
					} catch (Exception e) {
						e.printStackTrace();
					}
					showMsg("开始录制");         //显示，开始录制
					mVideoStartBtn.setBackgroundDrawable(iconStop);   //视频录制图标变为停止的图标
					isRecording = !isRecording;                 //把开始录制变为  不录制
				} else {
					/*
					 * 点击停止
					 */
					try {
						bool = false;
						mMediaRecorder.stop();                 //录制停止
						timer.setText(format(hour) + ":" + format(minute) + ":"
								+ format(second));           //显示时间
						mMediaRecorder.release();            //释放视频录制
						mMediaRecorder = null;
						videoRename();                     //video 重命名
					} catch (Exception e) {
						e.printStackTrace();
					}
					isRecording = !isRecording;           
					mVideoStartBtn.setBackgroundDrawable(iconStart);  //录像图标还原为原来图标
					showMsg("录制完成，已保存");
					timer.setVisibility(View.GONE);
					//拍完视频之后再进行preview
					try {
						camera = Camera.open();
						Camera.Parameters parameters = camera.getParameters();
						parameters.setPreviewFrameRate(25); // 每秒5帧-----可以更改为25
						parameters.setPictureFormat(PixelFormat.JPEG);// 设置照片的输出格式
						parameters.set("jpeg-quality", 90);// 照片质量
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
		
		Button btnImgStart = (Button) findViewById(R.id.arc_hf_img_start); //拍照按钮
		btnImgStart.setOnClickListener(new OnClickListener() {   //到300行

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
//					showMsg("录制完成，已保存");

					try {
						camera = Camera.open();
						Camera.Parameters parameters = camera.getParameters();
						parameters.setPreviewFrameRate(25); // 每秒5帧
						parameters.setPictureFormat(PixelFormat.JPEG);// 设置照片的输出格式
						parameters.set("jpeg-quality", 90);// 照片质量
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
									0, data.length);                       //图片解码
							Matrix matrix = new Matrix();
							// 设置缩放----什么用？
							matrix.postScale(5f, 4f);  //宽度5倍，高度4倍
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
								FileOutputStream outStream = new FileOutputStream(  //图片的保存
										out);
								bitmap.compress(CompressFormat.JPEG, 100,
										outStream);    //图片压缩，100表示压缩
								outStream.close();
								camera.startPreview();
							} catch (Exception e) {
								e.printStackTrace();
							}
//							showMsg("拍照成功");
						}
					}); // 拍照
				}
				showMsg("拍照成功");	
			}
		});
	}

	/*
	 * 消息提示  showMsg（）函数
	 */
	private Toast toast;

	public void showMsg(String arg) {                //showMsg（）函数
		if (toast == null) {
			toast = Toast.makeText(this, arg, Toast.LENGTH_SHORT);  //显示三秒时间
		} else {
			toast.cancel();
			toast.setText(arg);
		}
		toast.show();
	}

	/*
	 * 生成video文件名字   video重命名
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
	 *  定时器设置，实现计时   定时器的实现---秒数在走的
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
	 * 格式化时间
	 */
	public String format(int i) {
		String s = i + "";
		if (s.length() == 1) {
			s = "0" + s;
		}
		return s;
	}

	/*
	 * 覆写返回键监听    手机自带的返回键使用
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

	//用于当有电话打入，保存呢视频并退出
	
	@Override
	protected void onPause() {
		super.onPause();
		onBackPressed();
	}
}