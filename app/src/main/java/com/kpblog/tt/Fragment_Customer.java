package com.kpblog.tt;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.kpblog.tt.adapter.CustomerPurchaseListViewAdapter;
import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.model.CustomerPurchase;
import com.kpblog.tt.util.Constants;
import com.kpblog.tt.util.Util;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_Customer.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Customer#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Customer extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText phone;
    private Button optOutBtn, searchBtn;
    TextInputLayout phoneLayout;
    ListView listview;

    private long optOutBtnLastClicked = 0, searchBtnLastClicked = 0;
    DatabaseHandler handler;

    private OnFragmentInteractionListener mListener;

    public Fragment_Customer() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_Customer.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Customer newInstance(String param1, String param2) {
        Fragment_Customer fragment = new Fragment_Customer();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment__customer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        handler = new DatabaseHandler(getContext());
        
        phoneLayout = (TextInputLayout) getView().findViewById(R.id.phoneLayout);
        phone = (EditText) (getView().findViewById(R.id.phone));
        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        optOutBtn = (Button) (getView().findViewById(R.id.optOutBtn));
        optOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - optOutBtnLastClicked > Constants.BUTTON_CLICK_ELAPSE_THRESHOLD){
                    optOutBtnLastClicked = SystemClock.elapsedRealtime();
                    final String unformattedPhoneNumber = Util.getUnformattedPhoneNumber(phone.getText().toString());

                    if (Util.isPhoneNumberValid(phoneLayout, getString(R.string.phone_err_msg), unformattedPhoneNumber)){
                        unsubscribe(unformattedPhoneNumber);
                    }
                }
            }
        });

        searchBtn = (Button) (getView().findViewById(R.id.searchBtn));
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - searchBtnLastClicked > Constants.BUTTON_CLICK_ELAPSE_THRESHOLD){
                    searchBtnLastClicked = SystemClock.elapsedRealtime();
                    final String unformattedPhoneNumber = Util.getUnformattedPhoneNumber(phone.getText().toString());
                    
                    if (Util.isPhoneNumberValid(phoneLayout, getString(R.string.phone_err_msg), unformattedPhoneNumber)){
                        CustomerPurchase[] purchases = searchCustomer(unformattedPhoneNumber);

                        // Create an adapter to bind data to the ListView
                        CustomerPurchaseListViewAdapter adapter = new CustomerPurchaseListViewAdapter(getContext(), R.layout.customer_row_layout, R.id.purchaseDate, purchases);

                        // Bind data to the ListView
                        listview.setAdapter(adapter);
                    }
                }
            }
        });


        listview = (ListView) getView().findViewById(R.id.listview);
        // Inflate customerHeader view
        ViewGroup headerView = (ViewGroup)getLayoutInflater().inflate(R.layout.customer_header, listview,false);
        // Add customerHeader view to the ListView
        listview.addHeaderView(headerView);
    }

    private CustomerPurchase[] searchCustomer(String customerId) {
        CustomerPurchase[] cpList = handler.getAllCustomerPurchaseById(customerId);

        return cpList;
    }

    private void unsubscribe(String customerId) {
        try {
            handler.unsubscribe(customerId);
            displayToast(getString(R.string.optOutSuccess));
        } catch (Exception e){
            displayToast("unsubscribe FAILED: " + e.getMessage());
        }
    }

    private void displayToast(String msg) {
        Toast.makeText(getContext().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
