package com.kpblog.tt.adapter;

import android.widget.ArrayAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kpblog.tt.R;
import com.kpblog.tt.model.CustomerPurchase;
import com.kpblog.tt.util.Constants;

public class CustomerPurchaseListViewAdapter extends ArrayAdapter<CustomerPurchase> {
    int groupid;
    CustomerPurchase[] item_list;
    ArrayList<String> desc;
    Context context;
    public CustomerPurchaseListViewAdapter(Context context, int vg, int id, CustomerPurchase[] item_list){
        super(context,vg, id, item_list);
        this.context=context;
        groupid=vg;
        this.item_list=item_list;

    }
    // Hold views of the ListView to improve its scrolling performance
    static class ViewHolder {
        public TextView purchaseDate;
        public TextView quantity;
        public TextView note, receiptNum;

    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        // Inflate the customerRowLayoutowLayout.xml file if convertView is null
        if(rowView==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView= inflater.inflate(groupid, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.purchaseDate = (TextView) rowView.findViewById(R.id.purchaseDate);
            viewHolder.quantity = (TextView) rowView.findViewById(R.id.quantity);
            viewHolder.receiptNum = (TextView) rowView.findViewById(R.id.recNumber);
            viewHolder.note = (TextView) rowView.findViewById(R.id.note);
            rowView.setTag(viewHolder);

        }
        // Set text to each TextView of ListView item
        CustomerPurchase customerPurchase = item_list[position];
        ViewHolder holder = (ViewHolder) rowView.getTag();

        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_YYYY_MM_DD_HH_MM);
        holder.purchaseDate.setText(sdf.format(customerPurchase.getPurchaseDate()));

        holder.quantity.setText(String.valueOf(customerPurchase.getQuantity()));
        holder.receiptNum.setText(String.valueOf(customerPurchase.getReceiptNum()));
        holder.note.setText(customerPurchase.getNotes() == null? "" : customerPurchase.getNotes());
        return rowView;
    }

}