package com.ihelp101.voiceminus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

public class SMS extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String account = preferences.getString("account","");
        String authToken = preferences.getString("authToken","");
        String name = intent.getStringExtra("name");

        String msg = intent.getStringExtra("message");
        msg = msg.substring(0,1).toUpperCase() + msg.substring(1);

        String number = getContactNumber(context, name);

        try {
            String rnr = preferences.getString("rnr","");
            if (number != null|rnr != null) {
                sendRnrSe(authToken, rnr, number, msg, context);
            } else {
                NotificationManager mNotificationManager;

                mNotificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);

                PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                        new Intent(context, SMS.class), 0);

                long[] vibraPattern = {0, 250, 100, 250, 100, 250};

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                        context).setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("SMS Name Lookup Failed")
                        .setVibrate(vibraPattern)
                        .setLights(Color.parseColor("RED"), 5000, 5000)
                        .setContentText("SMS Name Lookup Failed");

                Notification note = mBuilder.build();
                note.defaults |= Notification.DEFAULT_VIBRATE;


                mBuilder.setContentIntent(contentIntent);
                mNotificationManager.notify(1, mBuilder.build());
            }
        } catch (Exception e){
            System.out.println("Failed: " + e);
        }
    }

    void sendRnrSe(final String authToken, String rnrse, String number, String text, Context context) throws Exception {
        JsonObject json = Ion.with(context)
                .load("https://www.google.com/voice/sms/send/")
                .setHeader("Authorization", "GoogleLogin auth=" + authToken)
                .setBodyParameter("phoneNumber", number)
                .setBodyParameter("sendErrorSms", "0")
                .setBodyParameter("text", text)
                .setBodyParameter("_rnr_se", rnrse)
                .asJsonObject()
                .get();

        if (!json.get("ok").getAsBoolean())
            throw new Exception(json.toString());
    }

    public static String getContactNumber(Context context, String phoneName) {
        String contactNumber = null;
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext()) {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            if (name.toLowerCase().equals(phoneName.toLowerCase())) {
                contactNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
        }
        phones.close();
        return contactNumber;
    }
}
