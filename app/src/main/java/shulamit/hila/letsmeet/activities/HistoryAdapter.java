package shulamit.hila.letsmeet.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import shulamit.hila.letsmeet.R;
import shulamit.hila.letsmeet.moduls.Point;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder>{
    private ArrayList<Point> historyPoints;
    private Context context;

    public HistoryAdapter(ArrayList<Point> savesPoints, Context context) {
        this.historyPoints = savesPoints;
        this.context = context;
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent , false) ;
        HistoryAdapter.ViewHolder holder= new HistoryAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final HistoryAdapter.ViewHolder holder, int position) {
        holder.name.setText(historyPoints.get(position).getName());
        holder.lat = historyPoints.get(position).getLat();
        holder.lon = historyPoints.get(position).getLng();
    }



    public class  ViewHolder extends RecyclerView.ViewHolder{
        //        ImageView image;
        TextView name;
        RelativeLayout xmlContactsItem;
        Button btnGo;
        double lat;
        double lon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.txv_contact_details_history);
            xmlContactsItem=itemView.findViewById(R.id.item_history);
            btnGo=itemView.findViewById(R.id.btn_go_history);
        }

    }

    @Override
    public int getItemCount() {
        return historyPoints.size();    }
    @Override
    public void onBindViewHolder(@NonNull final HistoryAdapter.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
/**
 * on click go in history list its navigate to the place in the history
 */
        (holder).btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,MainActivity.class);
                intent.putExtra("otherUserLat",holder.lat);
                intent.putExtra("otherUserLng",holder.lon);
                intent.putExtra("shouldNavigateFromSaves",true);
                context.startActivity(intent);
            }
        });
    }
}
