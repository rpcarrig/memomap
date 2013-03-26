package com.rpcarrig.memomapa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GpsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
        Intent service = new Intent(context, MemoMapService.class);
        context.startService(service);
	}

}
