package com.picselect.utils;

import android.content.Context;


public class Uitls {

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int limit;
	static String savePathString;

	static {
		limit = 9;
		savePathString = "/temp";
	}

	public static void setLimit(int limit) {
		Uitls.limit = limit;
	}

	public static int getLimit() {
		return limit;
	}

	public static void setSavePath(String path) {
		Uitls.savePathString = path;
	}

	public static String getSavePath() {
		return savePathString;
	}
}
