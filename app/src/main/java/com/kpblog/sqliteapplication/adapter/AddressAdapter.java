package com.kpblog.sqliteapplication.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.kpblog.sqliteapplication.MainActivity;
import com.kpblog.sqliteapplication.R;
import com.kpblog.sqliteapplication.model.Customer;

/**
 * Created by Khushvinders on 05-Feb-17.
 */

public class AddressAdapter extends BaseAdapter {
    private Context context;

    public AddressAdapter (Context context){
        this.context = context;
    }

    @Override
    public int getCount() {
        return ((MainActivity)context).list.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        Holder holder;
        if(convertView==null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();

            convertView = inflater.inflate(R.layout.address_list_layout, viewGroup, false);
            holder = new Holder();
            holder.customer = (TextView) convertView.findViewById(R.id.customer);
            holder.delete = (Button) convertView.findViewById(R.id.delete);

            convertView.setTag(holder);
        }
        else{
            holder = (Holder) convertView.getTag();
            //holder.addressId.setText(addressList.get(i).getTotalCredit());
            //holder.name.setText(addressList.get(i).getCustomerId());

        }
        final Customer customer = ((MainActivity) context).list.get(i);
        /*int totalCredit = customer.getTotalCredit();
        String phoneNum = customer.getCustomerId();
        Date lastVisitedDate = customer.getLastVisitDate();
        boolean isOptIn = customer.isOptIn();*/

        holder.customer.setText(customer.toString());
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)context).deleteCustomer(customer);
                ((MainActivity)context).list.remove(customer);
                notifyDataSetChanged();
            }
        });
        return convertView;
    }
    class Holder{
        private TextView customer;
        private Button delete;

    }
}
