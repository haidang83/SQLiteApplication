package com.kpblog.tt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kpblog.tt.R;
import com.kpblog.tt.model.Customer;
import com.kpblog.tt.util.Constants;
import com.kpblog.tt.util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CustomerListViewAdapter extends ArrayAdapter<Customer> {

    int groupid;
    Customer[] item_list;
    Context context;
    public CustomerListViewAdapter(Context context, int vg, int id, Customer[] item_list){
        super(context,vg, id, item_list);
        this.context=context;
        groupid=vg;
        this.item_list=item_list;

    }

    // Hold views of the ListView to improve its scrolling performance
    static class ViewHolder {
        public TextView customerId;
        public TextView drinkCredit;
        public TextView lastVisitDate, lastTextedDate;

    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        // Inflate the customerRowLayoutowLayout.xml file if convertView is null
        if(rowView==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView= inflater.inflate(groupid, parent, false);
            CustomerListViewAdapter.ViewHolder viewHolder = new CustomerListViewAdapter.ViewHolder();
            viewHolder.customerId = (TextView) rowView.findViewById(R.id.phone);
            viewHolder.drinkCredit = (TextView) rowView.findViewById(R.id.drinkCredit);
            viewHolder.lastVisitDate = (TextView) rowView.findViewById(R.id.lastVisit);
            viewHolder.lastTextedDate = (TextView) rowView.findViewById(R.id.lastTexted);
            rowView.setTag(viewHolder);

        }
        // Set text to each TextView of ListView item
        Customer customer = item_list[position];
        CustomerListViewAdapter.ViewHolder holder = (CustomerListViewAdapter.ViewHolder) rowView.getTag();

        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_YYYY_MM_DD);
        holder.lastVisitDate.setText(sdf.format(customer.getLastVisitDate()));

        holder.drinkCredit.setText(String.valueOf(customer.getTotalCredit()));
        holder.customerId.setText(Util.formatPhoneNumber(customer.getCustomerId()));
        holder.lastTextedDate.setText(customer.getLastContactedDate().getTime() == 0? "" : sdf.format(customer.getLastContactedDate()));
        return rowView;
    }


}
