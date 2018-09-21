package com.kpblog.tt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kpblog.tt.R;
import com.kpblog.tt.model.CustomerPurchase;
import com.kpblog.tt.util.Constants;
import com.kpblog.tt.util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class TransactionListViewAdapter extends ArrayAdapter<CustomerPurchase> {
    int groupid;
    CustomerPurchase[] item_list;
    ArrayList<String> desc;
    Context context;
    public TransactionListViewAdapter(Context context, int vg, int id, CustomerPurchase[] item_list){
        super(context,vg, id, item_list);
        this.context=context;
        groupid=vg;
        this.item_list=item_list;

    }
    // Hold views of the ListView to improve its scrolling performance
    static class ViewHolder {
        public TextView transactionTime;
        public TextView quantity;
        public TextView phone, receiptNum;

    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        // Inflate the customerRowLayoutowLayout.xml file if convertView is null
        if(rowView==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView= inflater.inflate(groupid, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.transactionTime = (TextView) rowView.findViewById(R.id.transactionTime);
            viewHolder.quantity = (TextView) rowView.findViewById(R.id.quantity);
            viewHolder.receiptNum = (TextView) rowView.findViewById(R.id.receiptNumber);
            viewHolder.phone = (TextView) rowView.findViewById(R.id.phone);
            rowView.setTag(viewHolder);

        }
        // Set text to each TextView of ListView item
        CustomerPurchase customerPurchase = item_list[position];
        ViewHolder holder = (ViewHolder) rowView.getTag();

        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_YYYY_MM_DD_HH_MM);
        holder.transactionTime.setText(sdf.format(customerPurchase.getPurchaseDate()));

        holder.quantity.setText(String.valueOf(customerPurchase.getQuantity()));
        holder.receiptNum.setText(String.valueOf(customerPurchase.getReceiptNum()));
        holder.phone.setText(Util.formatPhoneNumber(customerPurchase.getCustomerId()));
        return rowView;
    }

}