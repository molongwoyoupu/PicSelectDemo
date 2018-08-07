package com.picselect.activity;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.picselect.R;
import com.picselect.pojo.ImageBean;
import com.picselect.utils.AnimationUtil;
import com.picselect.utils.ImmerseHelper;
import com.picselect.view.MyGridView;
import com.picselect.view.MyImageView;

import com.squareup.picasso.Picasso;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements OnClickListener {

	MyGridView image_selector;
	MyAdapter adapter;
	List<ImageBean> images = new ArrayList<ImageBean>();
	
	List<ImageBean> preimages = new ArrayList<ImageBean>();
	private TextView oldimage_priview;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			ImmerseHelper.setSystemBarTransparent(this);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
	}

	// 选择图片
	public void test_select() {
		startActivityForResult((new Intent(this, PicSelectActivity.class)), 0x123);
	}

	public void initView() {

		image_selector = (MyGridView) findViewById(R.id.image_selector);// 图片选择
		oldimage_priview = (TextView) findViewById(R.id.oldimage_priview);// 图片预览
		oldimage_priview.setOnClickListener(this);
		ImageBean bmp = new ImageBean(R.drawable.menu_add + "");
		adapter = new MyAdapter(this);
		images.add(bmp);
		adapter.setData(images);
		image_selector.setAdapter(adapter);
		image_selector.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// 按下抬起事件
				AnimationUtil.AnimationAlgorithm(v, event, MainActivity.this);
				// true拦截事件，不会走onclick
				// 返回false即可响应click事件
				return false;
			}
		});

		/*
		 * 监听GridView点击事件 报错:该函数必须抽象方法 故需要手动导入import android.view.View;
		 */
		image_selector.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				if (position == 0) { // 点击图片位置为+ 0对应0张图片
					if (images.size() >= 10) { // 第一张为默认图片
						Toast.makeText(MainActivity.this, "图片数9张已满", Toast.LENGTH_SHORT).show();
					} else {
						if (ActivityCompat.checkSelfPermission(MainActivity.this,
								Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
								|| ActivityCompat.checkSelfPermission(MainActivity.this,
										Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
							ActivityCompat.requestPermissions(MainActivity.this, new String[] {
									Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA }, 1234);
						} else {
							Toast.makeText(MainActivity.this, "添加图片", Toast.LENGTH_SHORT).show();
							// 选择图片
							test_select();
						}
					}
				} else {
					dialog(position);
				}
			}
		});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == 1234) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // 用户同意写文件
				Toast.makeText(MainActivity.this, "添加图片", Toast.LENGTH_SHORT).show();
				// 选择图片
				test_select();
			} else { // 用户不同意,自行处理即可
				Toast.makeText(MainActivity.this, "没有权限!", Toast.LENGTH_SHORT).show();

			}
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0x123 && resultCode == RESULT_OK) {
			Intent intent = data;
			List<ImageBean> selectimages = (List<ImageBean>) intent.getSerializableExtra("images");
			for (ImageBean b : selectimages) {
				System.out.println("<><<><> ???" + b.toString());
				if (images.size() < 10) {
					images.add(b);
				}
			}
			adapter.notifyDataSetChanged();
		} else if (requestCode == 0x456 && resultCode == RESULT_OK) {
			Intent intent = data;
			List<ImageBean> selectimages = (List<ImageBean>) intent.getSerializableExtra("M_LIST");
			System.out.println("返回的数据量:" + images.size());
			for (ImageBean m : selectimages) {
				System.out.println(m.path);
				if (images.size() < 10) {
					images.add(m);
				}
			}
			adapter.notifyDataSetChanged();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	/*
	 * Dialog对话框提示用户删除操作 position为删除图片位置
	 */
	protected void dialog(final int position) {

		AlertDialog.Builder builder = new Builder(MainActivity.this);
		builder.setMessage("确认移除已添加图片吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				images.remove(position);
				adapter.notifyDataSetChanged();
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	class MyAdapter extends BaseAdapter {
		Context context;
		List<ImageBean> beans;

		public MyAdapter(Context context) {
			this.context = context;
		}

		public void setData(List<ImageBean> beans) {
			this.beans = beans;
		}

		@Override
		public int getCount() {
			return beans == null || beans.size() == 0 ? 0 : beans.size();
		}

		@Override
		public Object getItem(int position) {
			return beans == null || beans.size() == 0 ? null : beans.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(context, R.layout.item_publish, null);
			}
			MyImageView image = (MyImageView) convertView.findViewById(R.id.image);
			String path = beans.get(position).path;
			if (path.startsWith("http://") || path.startsWith("https://")) {
				// 这里进行图片的缓存操作
				Picasso.with(context).load(path).resize(150, 150).into(image);
			} else if (path.matches("[0-9]+")) {
				// 这里进行图片的缓存操作
				Picasso.with(context).load(Integer.parseInt(path)).resize(150, 150).into(image);
			} else {
				Picasso.with(context).load(new File(path)).resize(150, 150).into(image);
			}
			return convertView;
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent;
		switch (v.getId()) {
		case R.id.oldimage_priview:
			if(images.size()==1) {
				Toast.makeText(this, "没有图片", Toast.LENGTH_SHORT).show();
			}else {
				preimages.clear();
				preimages.addAll(images);
				preimages.remove(0);
				intent = new Intent(this, ImageBrowserActivity.class);
				intent.putExtra("images", (Serializable) preimages);
				intent.putExtra("position", 0);
				startActivity(intent);
			}
			break;

		default:
			break;
		}
	}
}
