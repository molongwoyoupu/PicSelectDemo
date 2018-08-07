package com.picselect.utils;

import com.picselect.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

public class ImmerseHelper {
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static void setSystemBarTransparent(Activity paramActivity) {
		Window window = paramActivity.getWindow();
		WindowManager.LayoutParams layoutParams = window.getAttributes();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			// api 21 解决方案
			View systemdecor = window.getDecorView();
			systemdecor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			layoutParams.flags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
			window.setStatusBarColor(0x00000000);
		} else {
			// api 19 解决方案
			layoutParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		}
		window.setAttributes(layoutParams);

		hackStatusBarTransparent(paramActivity);
		setContentPadding(paramActivity);

	}

	public static void hackStatusBarTransparent(Activity paramActivity) {
		ViewGroup localViewGroup = (ViewGroup) paramActivity.getWindow().getDecorView()
				.findViewById(android.R.id.content);
		View colorview = new View(paramActivity);
		colorview.setBackgroundResource(R.drawable.actionbar_shape);//设置状态栏的背景
		localViewGroup.addView(colorview, ViewGroup.LayoutParams.MATCH_PARENT,
				ImmerseHelper.getStatusBarHeight(paramActivity));
	}

	public static void setContentPadding(Activity activity) {
		((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0).setPadding(0,
				ImmerseHelper.getStatusBarHeight(activity), 0, 0);
	}

	/**
	 * 获取状态栏高度, 单位px
	 * 
	 * @param context
	 * @return
	 */
	public static int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	/**
	 * 获取actionbar高度, 单位px
	 * 
	 * @param context
	 * @return
	 */
	public static int getActionBarHeight(Context context) {
		TypedValue localTypedValue = new TypedValue();
		if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, localTypedValue, true)) {
			return TypedValue.complexToDimensionPixelSize(localTypedValue.data,
					context.getResources().getDisplayMetrics());
		}
		return 0;
	}
}