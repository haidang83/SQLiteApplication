package com.kpblog.tt;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.kpblog.tt.adapter.CustomerListViewAdapter;
import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.model.Customer;
import com.kpblog.tt.util.Constants;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_Dashboard.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Dashboard#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Dashboard extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    DatabaseHandler handler;
    EditText lastVisitMinDay, lastVisitMaxDay, lastTextMinDay;
    TextInputLayout lastVisitMinLayout, lastVisitMaxLayout, lastTextMinLayout;
    ListView listView;
    Button search;
    long searchBtnLastClicked = 0;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Fragment_Dashboard() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_Dashboard.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Dashboard newInstance(String param1, String param2) {
        Fragment_Dashboard fragment = new Fragment_Dashboard();
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
        return inflater.inflate(R.layout.fragment__dashboard, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        handler = new DatabaseHandler(getContext());

        lastVisitMinLayout = (TextInputLayout) getView().findViewById(R.id.minLayout);
        lastVisitMinDay = (EditText) getView().findViewById(R.id.minimumDay);

        lastVisitMaxLayout = (TextInputLayout) getView().findViewById(R.id.maxLayout);
        lastVisitMaxDay = (EditText) getView().findViewById(R.id.maximumDay);

        lastTextMinLayout = (TextInputLayout) getView().findViewById(R.id.lastTextMinLayout);
        lastTextMinDay = (EditText) getView().findViewById(R.id.lastTextMinimumDay);

        search = (Button) getView().findViewById(R.id.searchBtn);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - searchBtnLastClicked > Constants.BUTTON_CLICK_ELAPSE_THRESHOLD) {
                    searchBtnLastClicked = SystemClock.elapsedRealtime();

                    validateInputAndSearchCustomers();
                }
            }
        });

        listView = (ListView) getView().findViewById(R.id.listview);
        // Inflate customerHeader view
        ViewGroup headerView = (ViewGroup)getLayoutInflater().inflate(R.layout.dashboard_header, listView,false);
        // Add customerHeader view to the ListView
        listView.addHeaderView(headerView);
    }

    private void validateInputAndSearchCustomers() {
        boolean validInput = true;

        int lastVisitMinDayInt = 0, lastVisitMaxDayInt = 0, lastTextMinDayInt = 0;

        String lastVisitMinDayStr = lastVisitMinDay.getText().toString();
        if (!lastVisitMinDayStr.isEmpty()){
            if (lastVisitMinDayStr.matches(Constants.AT_LEAST_ONE_DIGIT_REGEXP)) {
                lastVisitMinLayout.setErrorEnabled(false);
                lastVisitMinDayInt = Integer.parseInt(lastVisitMinDayStr);
            }
            else {
                lastVisitMinLayout.setError("*");
                validInput = false;
            }
        }

        String lastVisitMaxDayStr = lastVisitMaxDay.getText().toString();
        if (!lastVisitMaxDayStr.isEmpty()){
            if (lastVisitMaxDayStr.matches(Constants.AT_LEAST_ONE_DIGIT_REGEXP)){
                lastVisitMaxLayout.setErrorEnabled(false);
                lastVisitMaxDayInt = Integer.parseInt(lastVisitMaxDayStr);
            }
            else {
                lastVisitMaxLayout.setError("*");
                validInput = false;
            }
        }

        String lastTextMinDayStr = lastTextMinDay.getText().toString();
        if (!lastTextMinDayStr.isEmpty()){
            if (lastTextMinDayStr.matches(Constants.AT_LEAST_ONE_DIGIT_REGEXP)){
                lastTextMinLayout.setErrorEnabled(false);
                lastTextMinDayInt = Integer.parseInt(lastTextMinDayStr);
            }
            else {
                lastTextMinLayout.setError("*");
                validInput = false;
            }
        }

        if (validInput){
            Customer[] customers = searchCustomers(lastVisitMinDayInt, lastVisitMaxDayInt, lastTextMinDayInt);

            // Create an adapter to bind data to the ListView
            CustomerListViewAdapter adapter = new CustomerListViewAdapter(getContext(), R.layout.dashboard_row_layout, R.id.phone, customers);

            // Bind data to the ListView
            listView.setAdapter(adapter);
        }
    }

    private Customer[] searchCustomers(int lastVisitMinDayInt, int lastVisitMaxDayInt, int lastTextMinDayInt) {
        List<Customer> customerList = handler.searchCustomerByLastVisitAndText(lastVisitMinDayInt, lastVisitMaxDayInt, lastTextMinDayInt);
        return customerList.toArray(new Customer[0]);
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
