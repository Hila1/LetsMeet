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

import shulamit.hila.letsmeet.moduls.Point;
import shulamit.hila.letsmeet.R;

/**
 * this adapter it attached to view holder
 */
public class SavesAdapter extends RecyclerView.Adapter<SavesAdapter.ViewHolder>{
    private ArrayList<Point> savesPoints;
    private Context context;

    SavesAdapter(ArrayList<Point> savesPoints, Context context) {
        this.savesPoints = savesPoints;
        this.context = context;
    }

    @NonNull
    @Override
    public SavesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.saves_item, parent , false) ;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.name.setText(savesPoints.get(position).getName());
        holder.lat = savesPoints.get(position).getLat();
        holder.lon = savesPoints.get(position).getLng();
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        RelativeLayout xmlContactsItem;
        Button btnGo;
        double lat;
        double lon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.txv_contact_details);
            xmlContactsItem=itemView.findViewById(R.id.item_saves);
            btnGo=itemView.findViewById(R.id.btn_go);
        }
    }

    @Override
    public int getItemCount() {
        return savesPoints.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final SavesAdapter.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
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
