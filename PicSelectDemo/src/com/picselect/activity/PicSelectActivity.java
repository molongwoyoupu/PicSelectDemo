package com.picselect.activity;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import com.picselect.activity.ImageBrowserActivity;

import com.picselect.R;
import com.picselect.utils.AlbumHelper;
import com.picselect.utils.Uitls;
import com.picselect.utils.NativeImageLoader;
import com.picselect.utils.NativeImageLoader.NativeImageCallBack;
import com.picselect.adapter.PicSelectAdapter;
import com.picselect.pojo.AlbumBean;
import com.picselect.pojo.ImageBean;
import com.picselect.utils.ImmerseHelper;
import com.picselect.view.MyImageView;
import com.picselect.view.MyImageView.OnMeasureListener;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 图片选择activity
 * 
 */
public class PicSelectActivity extends FragmentActivity implements
		OnItemClickListener {
	private static final int PHOTO_GRAPH = 1;// ����

	GridView gridView;
	PicSelectAdapter adapter;
	TextView album;
	Button complete;
	TextView preView;

	Button back;

	String fileName;// 文件名，路径
	String dirPath;//
	static final int SCAN_OK = 0x1001;

	static boolean isOpened = false;
	PopupWindow popWindow;

	int selected = 0;//选择图片数量

	int height = 0;
	List<AlbumBean> mAlbumBean;
	public static final String IMAGES = "images";

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			ImmerseHelper.setSystemBarTransparent(this);
		}
	}
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_pic_select);
		back = (Button) this.findViewById(R.id.back);
		album = (TextView) this.findViewById(R.id.album);
		complete = (Button) this.findViewById(R.id.complete);
		preView = (TextView) this.findViewById(R.id.preview);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		//预览
		preView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				List<ImageBean> selecteds = getSelectedItem();
				Intent intent = new Intent(PicSelectActivity.this, ImageBrowserActivity.class);
				intent.putExtra(IMAGES, (Serializable) selecteds);
				startActivity(intent);
			}
		});

		//完成
		complete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				List<ImageBean> selecteds = getSelectedItem();
				Intent intent = new Intent();
				intent.putExtra(IMAGES, (Serializable) selecteds);
				setResult(RESULT_OK, intent);
				finish();
			}
		});

		//相册
		album.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isOpened && popWindow != null) {
					height = getWindow().getDecorView().getHeight();
					WindowManager.LayoutParams ll = getWindow().getAttributes();
					ll.alpha = 0.3f;
					getWindow().setAttributes(ll);
					popWindow.showAtLocation(
							findViewById(android.R.id.content),
							Gravity.NO_GRAVITY, 0,
							height - Uitls.dip2px(PicSelectActivity.this, 448));
				} else {
					if (popWindow != null) {
						popWindow.dismiss();
					}
				}
			}
		});
		gridView = (GridView) this.findViewById(R.id.child_grid);
		adapter = new PicSelectAdapter(PicSelectActivity.this, gridView,
				onImageSelectedCountListener);
		gridView.setAdapter(adapter);
		adapter.setOnImageSelectedListener(onImageSelectedListener);
		showPic();
		gridView.setOnItemClickListener(this);
	}

	/** 
	 * 拍照
	 */
	private void takePhoto() {
		//获取系統版本  
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		// 判断存储卡是否可以用，可用进行存储  
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			fileName = getFileName();
			dirPath = Environment.getExternalStorageDirectory().getPath();
			File tempFile = new File(dirPath);
			if (!tempFile.exists()) {
				tempFile.mkdirs();
			}
			File saveFile = new File(tempFile, fileName + ".jpg");
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			if (currentapiVersion < 24) {  
                // 从文件中创建uri  
                Uri uri = Uri.fromFile(saveFile);  
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);  
            } else {  
                //兼容android7.0 使用共享文件的形式  
                ContentValues contentValues = new ContentValues(1);  
                contentValues.put(MediaStore.Images.Media.DATA, saveFile.getAbsolutePath());  
                Uri uri = getApplication().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);  
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);  
            }  
			startActivityForResult(intent, PHOTO_GRAPH);
		} else {
			Toast.makeText(PicSelectActivity.this, "未检测到CDcard，拍照不可用!",
					Toast.LENGTH_SHORT).show();
		}		
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		System.out.println("..." + requestCode + ".." + resultCode + "..."
				+ data);
		if (requestCode == PHOTO_GRAPH && resultCode == RESULT_OK) {
			List<ImageBean> selecteds = new ArrayList<ImageBean>();
			selecteds.add(new ImageBean(null, 0l, null, dirPath + "/"
					+ fileName + ".jpg", false));
			Intent intent = new Intent();
			intent.putExtra(IMAGES, (Serializable) selecteds);
			setResult(RESULT_OK, intent);
			finish();
		}
	}
	
	

	/**
	 * 生成文件名
	 */
	private String getFileName() {
		StringBuffer sb = new StringBuffer();
		Calendar calendar = Calendar.getInstance();
		long millis = calendar.getTimeInMillis();
		String[] dictionaries = { "A", "B", "C", "D", "E", "F", "G", "H", "I",
				"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
				"V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g",
				"h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
				"t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4",
				"5", "6", "7", "8", "9" };
		sb.append("dzc");
		sb.append(millis);
		Random random = new Random();
		for (int i = 0; i < 5; i++) {
			sb.append(dictionaries[random.nextInt(dictionaries.length - 1)]);
		}
		return sb.toString();
	};

	OnImageSelectedCountListener onImageSelectedCountListener = new OnImageSelectedCountListener() {

		@Override
		public int getImageSelectedCount() {
			return selected;
		}
	};

	OnImageSelectedListener onImageSelectedListener = new OnImageSelectedListener() {

		@Override
		public void notifyChecked() {
			selected = getSelectedCount();
			complete.setText("完成(" + selected + "/" + Uitls.limit + ")");
			preView.setText("预览(" + selected + "/" + Uitls.limit + ")");
		}
	};

	//显示图片
	private void showPic() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = handler.obtainMessage();
				msg.what = SCAN_OK;
				msg.obj = AlbumHelper.newInstance(PicSelectActivity.this)
						.getFolders();
				msg.sendToTarget();
			}
		}).start();
	}

	Handler handler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			if (SCAN_OK == msg.what) {
				mAlbumBean = (List<AlbumBean>) msg.obj;
				if (mAlbumBean != null && mAlbumBean.size() != 0) {
					AlbumBean b = mAlbumBean.get(0);
					adapter.taggle(b);
					popWindow = showPopWindow();
				} else {
					List<ImageBean> sets = new ArrayList<ImageBean>();
					sets.add(new ImageBean());
					AlbumBean b = new AlbumBean("", 1, sets, "");
					adapter.taggle(b);
				}
			}
		};
	};

	/**
	 * 获取选择数量ֵ
	 * 
	 * @return
	 */
	private int getSelectedCount() {
		int count = 0;
		for (AlbumBean albumBean : mAlbumBean) {
			for (ImageBean b : albumBean.sets) {
				if (b.isChecked == true) {
					count++;
				}
			}
		}
		return count;
	}

	private List<ImageBean> getSelectedItem() {
		int count = 0;
		List<ImageBean> beans = new ArrayList<ImageBean>();
		OK: for (AlbumBean albumBean : mAlbumBean) {
			for (ImageBean b : albumBean.sets) {
				if (b.isChecked == true) {
					beans.add(b);
					count++;
				}
				if (count == Uitls.limit) {
					break OK;
				}
			}
		}
		return beans;
	}

	@SuppressWarnings("deprecation")
	private PopupWindow showPopWindow() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.picture_album, null);
		final PopupWindow mPopupWindow = new PopupWindow(view,
				LayoutParams.MATCH_PARENT, Uitls.dip2px(PicSelectActivity.this,
						400), true);
		//要setOutsideTouchable（true）有效果，还需要给PopupWindow设置背景
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		//相册列表
		ListView listView = (ListView) view.findViewById(R.id.list);
		AlbumAdapter albumAdapter = new AlbumAdapter(PicSelectActivity.this,
				listView);
		
		listView.setAdapter(albumAdapter);
		//设置相册列表显示数据
		albumAdapter.setData(mAlbumBean);
		mPopupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				WindowManager.LayoutParams ll = getWindow().getAttributes();
				ll.alpha = 1f;
				getWindow().setAttributes(ll);
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				AlbumBean b = (AlbumBean) parent.getItemAtPosition(position);
				//设置显示相册图片数据
				adapter.taggle(b);
				//设置底部显示相册名
				album.setText(b.folderName);
				mPopupWindow.dismiss();
			}
		});
		return mPopupWindow;
	}

	//相册adapter
	class AlbumAdapter extends BaseAdapter {
		ViewHolder viewHolder;
		LayoutInflater inflater;
		List<AlbumBean> albums;
		private Point mPoint = new Point(0, 0);
		ListView mListView;

		public AlbumAdapter(Context context, ListView mListView) {
			this.inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.mListView = mListView;
		}

		public void setData(List<AlbumBean> albums) {
			this.albums = albums;
		}

		@Override
		public int getCount() {
			return albums == null || albums.size() == 0 ? 0 : albums.size();
		}

		@Override
		public Object getItem(int position) {
			return albums.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = inflater.inflate(
						R.layout.picture_album_item, null);
				viewHolder.album_count = (TextView) convertView
						.findViewById(R.id.album_count);
				viewHolder.album_name = (TextView) convertView
						.findViewById(R.id.album_name);
				viewHolder.mImageView = (MyImageView) convertView
						.findViewById(R.id.album_image);
				viewHolder.mImageView
						.setOnMeasureListener(new OnMeasureListener() {

							@Override
							public void onMeasureSize(int width, int height) {
								mPoint.set(150, 150);
							}
						});
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
				viewHolder.mImageView
						.setImageResource(R.drawable.friends_sends_pictures_no);
			}
			final AlbumBean b = (AlbumBean) getItem(position);
			viewHolder.mImageView.setTag(b.thumbnail);

			viewHolder.album_name.setText(b.folderName);
			viewHolder.album_count.setText(b.count-1 + "张");

			Bitmap bitmap = NativeImageLoader.getInstance().loadNativeImage(
					b.thumbnail, mPoint, new NativeImageCallBack() {

						@Override
						public void onImageLoader(Bitmap bitmap, String path) {
							ImageView mImageView = (ImageView) mListView
									.findViewWithTag(b.thumbnail);
							if (mImageView != null && bitmap != null) {
								mImageView.setImageBitmap(bitmap);
							}
						}
					});
			if (bitmap != null) {
				viewHolder.mImageView.setImageBitmap(bitmap);
			} else {
				viewHolder.mImageView
						.setImageResource(R.drawable.friends_sends_pictures_no);
			}
			return convertView;
		}
	}

	public interface OnImageSelectedListener {
		void notifyChecked();
	}

	public interface OnImageSelectedCountListener {
		int getImageSelectedCount();
	}

	public static class ViewHolder {
		public MyImageView mImageView;
		public TextView album_name;
		public TextView album_count;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position == 0) {
			takePhoto();
		}else {
			PicSelectAdapter.ViewHolder viewHolder=(PicSelectAdapter.ViewHolder) view.getTag();
			if(viewHolder.mCheckBox.isChecked()) {
				viewHolder.mCheckBox.setChecked(false);
			}else {
				viewHolder.mCheckBox.setChecked(true);
			}
			
		}
	}

}
