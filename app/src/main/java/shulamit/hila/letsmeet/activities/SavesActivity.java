package shulamit.hila.letsmeet.activities;

import android.app.NotificationManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import shulamit.hila.letsmeet.R;
import shulamit.hila.letsmeet.moduls.Point;

public class SavesActivity extends AppCompatActivity {
    private static final String SAVES_DB = "saves_DB";
    private RecyclerView savesRecycleView;
    private ArrayList<Point> savesList;
    private SQLiteDatabase savesDB = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saves);
        savesRecycleView = findViewById(R.id.saves_recycler_view_id);
        savesList = new ArrayList<Point>();
        this.setTitle(getResources().getString(R.string.saves_activity));
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();

        boolean shouldAddNewLocation = intent.getBooleanExtra("shouldAddNewLocation",false);
        // shouldAddNewLocation is to true from 'myFireBaseMessagingService'.
        // made to know if the user has got a new saved location from a notification
        if(shouldAddNewLocation){
            addNewLocationToDB(intent);
        }
        //load all the 'saved positions' from the local DB
        loadSaves();
    }

    private void addNewLocationToDB(Intent intent) {
        String name = intent.getStringExtra("saveMTitle");
        String body = intent.getStringExtra("saveMLatLng");
        int notificationId= intent.getIntExtra("notificationId",0);
        String [] temp = new String[2];
        temp = body.split(",");
        String lat,lng;
        lat = temp[0];
        lng = temp[1];
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();

        name = name + "\ndate: " + dateFormat.format(date);
        //save in the database
        saveInDB(name,lat,lng);

        NotificationManager notificationManager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    private void saveInDB(String name, String lat, String lng) {
        // Get the contact name and email entered
        SQLiteDatabase savesDB = openOrCreateDatabase(SAVES_DB, MODE_PRIVATE, null);
        // Execute SQL statement to insert new data
        String sql = "INSERT INTO savesPoints (name, lat,lng) VALUES ('" + name + "', '" + lat +"', '" + lng + "');";
        savesDB.execSQL(sql);
        // Execute SQL statement to insert new data
        Toast.makeText(this,  " was insert!", Toast.LENGTH_SHORT).show();
    }

    /**
     *  load all saved positions from the device DB
     */
    private void loadSaves() {
        try {
            //get from database firebase or sql i think sql
            savesList.clear();
            savesDB = openOrCreateDatabase(SAVES_DB, MODE_PRIVATE, null);
            String sql = "SELECT * FROM savesPoints";
            Cursor cursor = savesDB.rawQuery(sql, null);

            // Get the index for the column name provided
            int nameColumn = cursor.getColumnIndex("name");
            int latColumn = cursor.getColumnIndex("lat");
            int lonColumn = cursor.getColumnIndex("lng");

            // Move to the first row of results & Verify that we have results
            if (cursor.moveToFirst()) {
                do {
                    // Get the results and store them in a String
                    String name = cursor.getString(nameColumn);
                    float lat = Float.parseFloat(cursor.getString(latColumn));
                    float lon = Float.parseFloat(cursor.getString(lonColumn));
                    Point point = new Point(lat, lon, name);

                    savesList.add(point);

                    // Keep getting results as long as they exist
                } while (cursor.moveToNext());
            } else {
                Toast.makeText(this, "No saves to Show", Toast.LENGTH_SHORT).show();
            }
            SavesAdapter adapter = new SavesAdapter(savesList, this);
            savesRecycleView.setAdapter(adapter);
            savesRecycleView.setLayoutManager(new LinearLayoutManager(this));
        }
        catch (Exception e){ }
    }
}
