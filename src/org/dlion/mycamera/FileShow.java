package org.dlion.mycamera;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Video;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author dlion
 * 
 */
public class FileShow extends Activity {

	private static final int MENU_DELETE = Menu.FIRST;
	private static final int MENU_RENAME = Menu.FIRST + 1;
	private File[] files;
	private String[] names;
	private String[] paths;
	private GridView fileGrid;
	private BaseAdapter adapter = null;
	private String path;
	private EditText etRename;
	private File file;

	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.map_file_show);   //和map_file_show绑定
		path = getIntent().getStringExtra("path"); //从“path”取数据
		File file = new File(path);
		files = file.listFiles();
		fileGrid = (GridView) findViewById(R.id.arc_hf_file_show);  //fileGid和xml中的绑定
		adapter = new fileAdapter(this);
		fileGrid.setAdapter(adapter);
		showFileItems();                    //一个函数 showfileitems
		fileGrid.setOnItemClickListener(new OnItemClickListener() {  //fileGrid监听事件

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {                   //arg0--所有文件 arg1--点击的文件 arg2--点击文件位置arg3--文件id
				File f = new File(paths[arg2]);
				Intent intent = new Intent();
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setAction(android.content.Intent.ACTION_VIEW);
				String type = thisFileType(names[arg2]);
				intent.setDataAndType(Uri.fromFile(f), type);
				startActivity(intent);
			}
		});

		// 注册上下文菜单
		registerForContextMenu(fileGrid);
	}

	/*
	 * 覆写上下文菜单    //设置长按跳出删除，重命名菜单
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterView.AdapterContextMenuInfo info = null;

		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			return;
		}
		menu.setHeaderTitle(names[info.position]);
		menu.add(0, MENU_DELETE, 1, "删除");
		menu.add(0, MENU_RENAME, 2, "重命名");
	}

	/*
	 * 上下文菜单监听   “删除”、“重命名”监听事件
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		File file = new File(paths[info.position]);
		switch (item.getItemId()) {
		case MENU_DELETE:
			file.delete();
			showFileItems();
			return true;
		case MENU_RENAME:
			fileRename(file);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	/*
	 * 获取文件    showfileitems()函数
	 */
	private void showFileItems() {
		File file = new File(path);
		files = file.listFiles();
		int count = files.length;
		names = new String[count];
		paths = new String[count];
		for (int i = 0; i < count; i++) {
			File f = files[i];
			names[i] = f.getName();
			paths[i] = f.getPath();
		}
		adapter.notifyDataSetChanged();
	}

	/*
	 * 获取文件类型
	 */
	public static String thisFileType(String name) {
		String type = "";
		String end = name.substring(name.lastIndexOf(".") + 1, name.length())
				.toLowerCase();
		if (end.equals("jpg")) {
			type = "image";
		} else if (end.equals("mp4")) {
			type = "video";
		} else {
			type = "*";
		}
		type += "/*";
		return type;
	}

	/**
	 * 重命名
	 */
	private void fileRename(File file) {
		this.file = file;
		View view = getLayoutInflater().inflate(R.layout.map_file_rename, null);  //和map_file_rename.xml绑定
		etRename = (EditText) view.findViewById(R.id.arc_hf_file_rename);
		new AlertDialog.Builder(this).setView(view)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String newName = etRename.getText().toString().trim();
						File newFile = new File(path, newName);
						if (newFile.exists()) {
							showMsg(newName + "已经存在，请重新输入");
						} else
							FileShow.this.file.renameTo(newFile);
						showFileItems();
					}
				}).setNegativeButton("取消", null).show();
	}

	/*
	 * 消息提示    showMsg()函数
	 */
	private Toast toast;

	public void showMsg(String arg) {
		if (toast == null) {
			toast = Toast.makeText(this, arg, Toast.LENGTH_SHORT);
		} else {
			toast.cancel();
			toast.setText(arg);
		}
		toast.show();
	}

	/*
	 * File adapter设置
	 */
	@SuppressLint("NewApi") class fileAdapter extends BaseAdapter {
		Context context;

		public fileAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return files.length;
		}

		@Override
		public Object getItem(int arg0) {
			// return files[arg0];
			return names[arg0];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String type = thisFileType(names[position]);
			convertView = getLayoutInflater().inflate(R.layout.map_file_item,
					null);
			ImageView icon = (ImageView) convertView
					.findViewById(R.id.arc_hf_file_icon);
			TextView name = (TextView) convertView
					.findViewById(R.id.arc_hf_file_name);
			if (type.equals("video/*")) {
				Bitmap videoIcon = ThumbnailUtils.createVideoThumbnail(
						paths[position], Video.Thumbnails.MINI_KIND);
				icon.setImageBitmap(videoIcon);
			} else if (type.equals("image/*")) {
				Bitmap bitmap = BitmapFactory.decodeFile(paths[position]);
				Bitmap imgIcon = ThumbnailUtils.extractThumbnail(bitmap, 150,
						120);
				icon.setImageBitmap(imgIcon);
			}
			name.setText(names[position]);
			return convertView;
		}
	}
}
