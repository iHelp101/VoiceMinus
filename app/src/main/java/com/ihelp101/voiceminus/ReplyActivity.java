package com.ihelp101.voiceminus;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;

public class ReplyActivity extends BroadcastReceiver {
    public static final String ACTION_EXAMPLE =
            "com.ihelp101.rewarditup.Voice.ACTION_EXAMPLE";

    private boolean mEnableMessages = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_EXAMPLE)) {
            if (mEnableMessages) {
                Bundle remoteInputResults = RemoteInput.getResultsFromIntent(intent);
                CharSequence replyMessage = null;
                if (remoteInputResults != null) {
                    replyMessage = remoteInputResults.getCharSequence(NotificationUtil.EXTRA_REPLY);
                }
                if (replyMessage != null) {
                    System.out.println(replyMessage);
                    String ns = Context.NOTIFICATION_SERVICE;
                    NotificationManager nMgr = (NotificationManager) context.getSystemService(ns);
                    nMgr.cancel(69);

                    Intent i = new Intent();
                    i.setAction("com.ihelp101.rewarditup.Voice.START");
                    i.putExtra("name", SMS.name);
                    i.putExtra("msg", replyMessage.toString());
                    context.sendBroadcast(i);
                }
            }
        }
    }
}
