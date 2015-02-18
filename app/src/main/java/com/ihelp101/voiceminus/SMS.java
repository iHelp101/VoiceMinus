package com.ihelp101.voiceminus;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import java.io.IOException;

public class SMS extends BroadcastReceiver {

    Context c;
    public static String name;
    String number;
    String account;
    String authToken;
    String msg;
    String rnr;

    private class FooTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            AccountManager am = AccountManager.get(c);
            Bundle token = null;
            if (am != null) {
                try {
                    token = am.getAuthToken(new Account(account, "com.google"), "grandcentral", null, true, null, null)
                            .getResult();
                } catch (AuthenticatorException e) {

                } catch (OperationCanceledException e) {

                } catch (IOException e) {

                }

                authToken = token.getString(AccountManager.KEY_AUTHTOKEN);

                System.out.println("Got Token!");

                try {
                    JsonObject userInfo = Ion.with(c)
                            .load("https://www.google.com/voice/request/user")
                            .setHeader("Authorization", "GoogleLogin auth=" + authToken)
                            .asJsonObject()
                            .get();

                    String rnrse = userInfo.get("r").getAsString();

                    if (rnrse != null) {
                        rnr = rnrse;
                    }
                } catch (Exception e) {
                    System.out.println("Fetch Failed: " +e);
                }

                try {
                    sendRnrSe(c);
                } catch (Exception e){
                    System.out.println("Failed: " + e);
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(String token) {
            if (token != null){
                //use token here
            }
        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        c = context;
        account = preferences.getString("account","");

        name = intent.getStringExtra("name");
        name = name.substring(0,1).toUpperCase() + name.substring(1);

        msg = intent.getStringExtra("msg");

        if (msg != null) {
            number = getContactNumber(context, name);
            msg = msg.substring(0,1).toUpperCase() + msg.substring(1);
            try {
                new FooTask().execute();
            } catch (Exception e) {
                System.out.println("I broke!");
            }
        } else {
            number = getContactNumber(context, name);
            if (number == null) {
                String ns = Context.NOTIFICATION_SERVICE;
                NotificationManager nMgr = (NotificationManager) context.getSystemService(ns);
                nMgr.cancel(69);

                NotificationManager mNotificationManager;

                mNotificationManager = (NotificationManager) c
                        .getSystemService(Context.NOTIFICATION_SERVICE);

                PendingIntent contentIntent = PendingIntent.getActivity(c, 0,
                        new Intent(c, SMS.class), 0);

                long[] vibraPattern = {0, 250, 100, 250, 100, 250};

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                        c).setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Invalid Contact Name")
                        .setVibrate(vibraPattern)
                        .setLights(Color.parseColor("RED"), 5000, 5000);

                Notification note = mBuilder.build();
                note.defaults |= Notification.DEFAULT_VIBRATE;


                mBuilder.setContentIntent(contentIntent);
                mNotificationManager.notify(1, mBuilder.build());

            } else {
                RemoteInput remoteInput = new RemoteInput.Builder(NotificationUtil.EXTRA_REPLY)
                        .setLabel("To: " + name)
                        .build();

// Create the reply action and add the remote input
                NotificationCompat.Action action =
                        new NotificationCompat.Action.Builder(R.drawable.ic_launcher, "Text Message", NotificationUtil.getExamplePendingIntent(context,
                                R.string.example_reply_action_clicked))
                                .addRemoteInput(remoteInput)
                                .build();

// Build the notification and add the action via WearableExtender
                Notification notification =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("SMS To " + name)
                                .extend(new NotificationCompat.WearableExtender().addAction(action))
                                .build();

// Issue the notification
                NotificationManagerCompat notificationManager =
                        NotificationManagerCompat.from(context);
                notificationManager.notify(69, notification);
            }
        }
    }

    void sendRnrSe(Context context) throws Exception {
        JsonObject json = Ion.with(context)
                .load("https://www.google.com/voice/sms/send/")
                .setHeader("Authorization", "GoogleLogin auth=" + authToken)
                .setBodyParameter("phoneNumber", number)
                .setBodyParameter("sendErrorSms", "0")
                .setBodyParameter("text", msg)
                .setBodyParameter("_rnr_se", rnr)
                .asJsonObject()
                .get();

        if (!json.get("ok").getAsBoolean()) {
            System.out.println("JSON:" +json);
            throw new Exception(json.toString());
        }
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
