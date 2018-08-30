package com.kpblog.tt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kpblog.tt.R;
import com.kpblog.tt.model.CustomerBroadcast;
import com.kpblog.tt.util.Constants;
import com.kpblog.tt.util.Util;

import java.text.SimpleDateFormat;

public class BroadcastListViewAdapter extends ArrayAdapter<CustomerBroadcast> {

    int groupid;
    CustomerBroadcast[] item_list;
    Context context;
    public BroadcastListViewAdapter(Context context, int vg, int id, CustomerBroadcast[] item_list){
        super(context,vg, id, item_list);
        this.context=context;
        groupid=vg;
        this.item_list=item_list;

    }

    // Hold views of the ListView to improve its scrolling performance
    static class ViewHolder {
        public TextView type;
        public TextView status;
        public TextView broadcastTimestamp, numTexted;

    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        // Inflate the broadcastRowLayout.xml file if convertView is null
        if(rowView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView= inflater.inflate(groupid, parent, false);
            BroadcastListViewAdapter.ViewHolder viewHolder = new BroadcastListViewAdapter.ViewHolder();
            viewHolder.type = (TextView) rowView.findViewById(R.id.type);
            viewHolder.status = (TextView) rowView.findViewById(R.id.status);
            viewHolder.broadcastTimestamp = (TextView) rowView.findViewById(R.id.timestamp);
            viewHolder.numTexted = (TextView) rowView.findViewById(R.id.numTexted);
            rowView.setTag(viewHolder);

        }
        // Set text to each TextView of ListView item
        CustomerBroadcast customerBroadcast = item_list[position];
        BroadcastListViewAdapter.ViewHolder holder = (BroadcastListViewAdapter.ViewHolder) rowView.getTag();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        holder.broadcastTimestamp.setText(sdf.format(customerBroadcast.getTimestamp()));

        holder.type.setText(customerBroadcast.getType().replace("SCHEDULED_", ""));
        holder.status.setText((customerBroadcast.getStatus()));

        if (Constants.BROADCAST_TYPE_SCHEDULED_FREE_FORM.equals(customerBroadcast.getType()) ||
                customerBroadcast.getStatus().equals(Constants.STATUS_SENT)){
            holder.numTexted.setText(String.valueOf(customerBroadcast.getRecipientPhoneNumbers().size()));
        }
        else {
            holder.numTexted.setText("TBD");
        }
        return rowView;
    }


}
