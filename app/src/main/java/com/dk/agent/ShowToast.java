package com.dk.agent;

import android.content.Context;
import android.widget.Toast;

public class ShowToast {

	static void show(Context ctx, String text) {
		CharSequence t = text;
		Toast toast = Toast.makeText(ctx, t, Toast.LENGTH_LONG);
		toast.show();
	}
}
