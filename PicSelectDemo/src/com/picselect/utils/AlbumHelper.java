package com.picselect.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.picselect.pojo.AlbumBean;
import com.picselect.pojo.ImageBean;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * 
 * 获取相册
 */
public class AlbumHelper {

	Context context;
	
	//内容解析,使用ContentResolver调用ContentProvider提供的接口，操作数据
	ContentResolver contentResolver;

	private static AlbumHelper instance;

	private AlbumHelper(Context context) {
		this.context = context;
		contentResolver = context.getContentResolver();
	}

	public static AlbumHelper newInstance(Context context) {
		if (instance == null) {
			instance = new AlbumHelper(context);
		}
		return instance;
	};

	//返回相册集合
	public List<AlbumBean> getFolders() {
		//MediaStore这个类是android系统提供的一个多媒体数据库
		Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

		//用于从ContentProvider中获取数据。
		Cursor mCursor = contentResolver.query(mImageUri, null,
				MediaStore.Images.Media.MIME_TYPE + "=? or "
						+ MediaStore.Images.Media.MIME_TYPE + "=?",
				new String[] { "image/jpeg", "image/png" },
				MediaStore.Images.Media.DATE_MODIFIED);
		if(mCursor == null){  
            return null;  
        } 
		HashMap<String, List<ImageBean>> map = capacity(mCursor);

		List<AlbumBean> mAlbumBeans = new ArrayList<AlbumBean>();

		Set<Entry<String, List<ImageBean>>> set = map.entrySet();
		for (Iterator<Map.Entry<String, List<ImageBean>>> iterator = set
				.iterator(); iterator.hasNext();) {
			Map.Entry<String, List<ImageBean>> entry = iterator.next();
			String parentName = entry.getKey();
			ImageBean b = entry.getValue().get(0);
			AlbumBean tempAlbumBean = new AlbumBean(parentName, entry
					.getValue().size()+1, entry.getValue(), b.path);
			// 在第0个位置加入了拍照图片（即第一张图片为拍照，后面图片显示相册内图片）
			tempAlbumBean.sets.add(0, new ImageBean());
			mAlbumBeans.add(tempAlbumBean);
		}
		return mAlbumBeans;
	}

	private HashMap<String, List<ImageBean>> capacity(Cursor mCursor) {

		HashMap<String, List<ImageBean>> beans = new HashMap<String, List<ImageBean>>();
		while (mCursor.moveToNext()) {
			String path = mCursor.getString(mCursor
					.getColumnIndex(MediaStore.Images.Media.DATA));

			long size = mCursor.getLong(mCursor
					.getColumnIndex(MediaStore.Images.Media.SIZE));

			String display_name = mCursor.getString(mCursor
					.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));

			String parentName = new File(path).getParentFile().getName();
			List<ImageBean> sb;
			
			//如果这个beans集合中有这个key就返回true
			if (beans.containsKey(parentName)) {
				sb = beans.get(parentName);
				sb.add(new ImageBean(parentName, size, display_name, path,
						false));
			} else {
				sb = new ArrayList<ImageBean>();
				sb.add(new ImageBean(parentName, size, display_name, path,
						false));
			}
			beans.put(parentName, sb);
		}
		return beans;
	}
}
