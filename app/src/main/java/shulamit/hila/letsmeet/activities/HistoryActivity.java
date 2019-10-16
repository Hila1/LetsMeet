package shulamit.hila.letsmeet.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.util.ArrayList;

import shulamit.hila.letsmeet.R;
import shulamit.hila.letsmeet.moduls.Point;

public class HistoryActivity extends AppCompatActivity {
    public static final String HISTORY_DB = "history_DB";
    private RecyclerView historyRecycleView;
    private ArrayList<Point> historyList;
    private SQLiteDatabase historyDB = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        historyRecycleView = findViewById(R.id.history_recycle_view_id);
        historyList = new ArrayList<Point>();
        this.setTitle(getResources().getString(R.string.history));
    }


    @Override
    protected void onStart() {
        super.onStart();
        loadHistory();
    }


    private void loadHistory() {
        try {
            //get from database firebase or sql i think sql
            historyList.clear();
            historyDB = openOrCreateDatabase(HISTORY_DB, MODE_PRIVATE, null);
            String sql = "SELECT * FROM historyPoints";
            Cursor cursor = historyDB.rawQuery(sql, null);

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

                    historyList.add(point);

                    // Keep getting results as long as they exist
                } while (cursor.moveToNext());


            } else {

                Toast.makeText(this, "No history to Show", Toast.LENGTH_SHORT).show();

            }

            HistoryAdapter adapter = new HistoryAdapter(historyList, this);
            historyRecycleView.setAdapter(adapter);
            historyRecycleView.setLayoutManager(new LinearLayoutManager(this));
        }
        catch (Exception e){
            String error = e.getMessage();
        }
    }

}
