package kiosk.android.econ.mcrbooking;

/**
 * Created by nidhi on 27/5/18.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.support.v7.widget.RecyclerView;

//import com.bumptech.glide.Glide;

import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.MyViewHolder> {
    private Context context;
    private List<Item> eventList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView room, time, person;
        public RelativeLayout viewBackground;
        public LinearLayout viewForeground;


        public MyViewHolder(View view) {
            super(view);
            room = view.findViewById(R.id.roomText);
            time = view.findViewById(R.id.timeText);
            person = view.findViewById(R.id.personText);
            viewBackground = view.findViewById(R.id.view_background);
            viewForeground = view.findViewById(R.id.view_foreground);
        }
    }


    public EventListAdapter(Context context, List<Item> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Item item = eventList.get(position);
        holder.room.setText(item.getRoom());
        holder.time.setText(item.getTime());
        holder.person.setText(item.getPerson());

//        Glide.with(context)
//                .load(item.getThumbnail())
//                .into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void removeItem(int position) {
        eventList.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    public void restoreItem(Item item, int position) {
        eventList.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }
}
