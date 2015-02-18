package com.ihelp101.voiceminus;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotificationUtil {
    public static final String EXTRA_MESSAGE =
            "com.ihelp101.voiceminus.Voice.MESSAGE";
    public static final String EXTRA_REPLY =
            "com.ihelp101.voiceminus.Voice.REPLY";

    public static PendingIntent getExamplePendingIntent(Context context, int messageResId) {
        Intent intent = new Intent(ReplyActivity.ACTION_EXAMPLE)
                .setClass(context, ReplyActivity.class);
        intent.putExtra(EXTRA_MESSAGE, context.getString(messageResId));
        return PendingIntent.getBroadcast(context, messageResId /* requestCode */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}