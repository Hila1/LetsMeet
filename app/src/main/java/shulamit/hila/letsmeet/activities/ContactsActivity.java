package shulamit.hila.letsmeet.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import shulamit.hila.letsmeet.moduls.Person;
import shulamit.hila.letsmeet.R;

import static android.support.constraint.Constraints.TAG;

public class ContactsActivity extends AppCompatActivity {
    private RecyclerView contactsRecycleView;
    private ArrayList<Person> contactsList;
    private ArrayList<String> users;
    private FirebaseFirestore db;
    private static final String USERS ="users" ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init the listView and get the recyclerView
        setContentView(R.layout.activity_contents);
        contactsRecycleView = findViewById(R.id.contacts_list_view_id);
        contactsList = new ArrayList<>();
        this.setTitle(getResources().getString(R.string.contacts));
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadContacts();//load the Contact from the content provider
    }

    // Read all contacts from Content Provider in Contacts App.
    public void loadContacts()
    {
        if(isPermissionToReadContactsOK())//check permission
            getContacts();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
            getContacts();//when the result came if the permission accepted get the contacts
        else
            showCenteredToast("NO Permission to Read Contacts!");
    }

    private void getContacts() {
        {
            contactsList.clear();

            String name, phone;

            ContentResolver resolver = getContentResolver();
            Uri contactsTableUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            Cursor cursor = resolver.query(contactsTableUri, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");

            if(cursor != null)
            {
                if(cursor.moveToNext())
                {
                    // there is at least ONE contact
                    do
                    {
                        name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Person person = new Person(name,phone);//create a person and add it to the list
                        contactsList.add(person);
                    }
                    while(cursor.moveToNext());
                    importUsersList();
                    cursor.close();
                }
                else
                    // Empty - No contacts
                    showCenteredToast("No Contacts!");
            }
            else
                // problem with resolver query
                showCenteredToast("Resolver Query Error!");
        }
    }
//check if user register to the app
    private void importUsersList() {
        this.users = new ArrayList<>();
        this.db = FirebaseFirestore.getInstance();
        db.collection(USERS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        if (document.exists()) {
                            String userId = (String)document.getData().get("number");
                            if (userId != null)
                                users.add(userId);//add him the users list
                        }
                    }
                    openContactsAdapter();//connect to the adapter
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    /**
     * connect thee list to the adapter
     */
    private void openContactsAdapter() {
        ContactsAdapter adapter = new ContactsAdapter(contactsList,this, users);
        contactsRecycleView.setAdapter(adapter);
        contactsRecycleView.setLayoutManager(new LinearLayoutManager(this));

    }

    // Check Runtime Permission for READ_CONTACTS
    public boolean isPermissionToReadContactsOK()
    {
        // check if permission for READ_CONTACTS is granted ?
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
            return true;
        else
        {
            // show requestPermissions dialog
            ActivityCompat.requestPermissions(ContactsActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 111);
            return false;
        }
    }

    // Jump to this app settings (to change the permissions if needed)
    public void showAppSettings(View view)
    {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    // show centered toast
    public void showCenteredToast(String msg)
    {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
