package com.alirnp.piri;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import me.everything.providers.android.contacts.Contact;
import me.everything.providers.android.contacts.ContactsProvider;
import me.everything.providers.core.Data;

public class GetContactsTask extends AsyncTask<Void, Void, Data<Contact>> {

    WeakReference<Context> contextWeakReference;

    GetContactsTask(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    @Override
    protected Data<Contact> doInBackground(Void... params) {
        Context context = contextWeakReference.get();
        if (context == null) return null;
        ContactsProvider contactsProvider = new ContactsProvider(context);
        Data<Contact> contacts = contactsProvider.getContacts();
        return contacts;
    }

    @Override
    protected void onPostExecute(Data<Contact> data) {
        Context context = contextWeakReference.get();
        if (context == null) return;

        Toast.makeText(context, "contact :" + data.getList().size(), Toast.LENGTH_SHORT).show();
    }
}
