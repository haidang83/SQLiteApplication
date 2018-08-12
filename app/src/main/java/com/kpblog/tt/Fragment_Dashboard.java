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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.kpblog.tt.adapter.CustomerListViewAdapter;
import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.model.Customer;
import com.kpblog.tt.util.Constants;

import org.w3c.dom.Text;

import java.util.ArrayList;
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
    EditText lastVisitOrDrinkCreditMin, lastVisitOrDrinkCreditMax, lastTextMinDay, lastTextMaxDay;
    TextInputLayout lastVisitOrDrinkCreditMinLayout, lastVisitOrDrinkCreditMaxLayout, lastTextMinLayout, lastTextMaxLayout;
    ListView listView;
    Button search;
    long searchBtnLastClicked = 0;
    Spinner lastVisitOrTotalCreditDropdown, orderByDropdown, ascDescDropdown;

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

        lastVisitOrTotalCreditDropdown = (Spinner) getView().findViewById(R.id.lastVisitOrTotalCreditDropdown);
        lastVisitOrTotalCreditDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateOrderByDropdownValues();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        lastVisitOrDrinkCreditMinLayout = (TextInputLayout) getView().findViewById(R.id.lastVisitOrDrinkCreditMinLayout);
        lastVisitOrDrinkCreditMin = (EditText) getView().findViewById(R.id.lastVisitOrDrinkCreditMin);

        lastVisitOrDrinkCreditMaxLayout = (TextInputLayout) getView().findViewById(R.id.lastVisitOrDrinkCreditMaxLayout);
        lastVisitOrDrinkCreditMax = (EditText) getView().findViewById(R.id.lastVisitOrDrinkCreditMax);

        lastTextMinLayout = (TextInputLayout) getView().findViewById(R.id.lastTextMinLayout);
        lastTextMinDay = (EditText) getView().findViewById(R.id.lastTextMinimumDay);

        lastTextMaxLayout = (TextInputLayout) getView().findViewById(R.id.lastTextMaxLayout);
        lastTextMaxDay = (EditText) getView().findViewById(R.id.lastTextMaximumDay);

        orderByDropdown = (Spinner) getView().findViewById(R.id.orderByDropdown);
        setOrderByDropdownAdapter();

        ascDescDropdown = (Spinner) getView().findViewById(R.id.ascDescDropdown);

        search = (Button) getView().findViewById(R.id.searchBtn);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - searchBtnLastClicked > Constants.BUTTON_CLICK_ELAPSE_THRESHOLD) {
                    //hide soft keyboard
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

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

    private void updateOrderByDropdownValues() {
        orderByDropdown = (Spinner) getView().findViewById(R.id.orderByDropdown);
        String selectedItem = lastVisitOrTotalCreditDropdown.getSelectedItem().toString();
        if (selectedItem.equals(getString(R.string.totalDrinkCreditText))){
            ArrayAdapter<String> orderByAdapter = (ArrayAdapter<String>) orderByDropdown.getAdapter();
            orderByAdapter.add(getString(R.string.totalDrinkCreditText));
            orderByAdapter.remove(getString(R.string.daysNotVisitedText));
            orderByAdapter.notifyDataSetChanged();
        }
        else if (selectedItem.equals(getString(R.string.daysNotVisitedText))){
            ArrayAdapter<String> orderByAdapter = (ArrayAdapter<String>) orderByDropdown.getAdapter();
            orderByAdapter.add(getString(R.string.daysNotVisitedText));
            orderByAdapter.remove(getString(R.string.totalDrinkCreditText));
            orderByAdapter.notifyDataSetChanged();
        }
    }

    private void setOrderByDropdownAdapter() {
        ArrayList<String> orderByValues = new ArrayList<String>();
        orderByValues.add(getString(R.string.daysSinceLastText));
        ArrayAdapter<String> orderByDropdownAdapter = new ArrayAdapter<String>(getContext(), R.layout.orderby_spinner_item, orderByValues);
        orderByDropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderByDropdown.setAdapter(orderByDropdownAdapter);
    }

    private String[] getOrderByItems() {
        return new String[] {lastVisitOrTotalCreditDropdown.getSelectedItem().toString(), getString(R.string.daysSinceLastText)};
    }

    private void validateInputAndSearchCustomers() {
        boolean validInput = true;

        int lastVisitOrDrinkCreditMinInt = 0, lastVisitOrDrinkCreditMaxInt = 0, lastTextMinDayInt = 0, lastTextMaxDayInt = 0;

        String selection = lastVisitOrTotalCreditDropdown.getSelectedItem().toString();
        boolean isDaysNotVisitedSelection = false;
        if (getString(R.string.daysNotVisitedText).equals(selection)){
            isDaysNotVisitedSelection = true;
        }

        String lastVisitOrDrinkCreditMinStr = lastVisitOrDrinkCreditMin.getText().toString();
        if (!lastVisitOrDrinkCreditMinStr.isEmpty()){
            if (lastVisitOrDrinkCreditMinStr.matches(Constants.AT_LEAST_ONE_DIGIT_REGEXP)) {
                lastVisitOrDrinkCreditMinLayout.setErrorEnabled(false);
                lastVisitOrDrinkCreditMinInt = Integer.parseInt(lastVisitOrDrinkCreditMinStr);
            }
            else {
                lastVisitOrDrinkCreditMinLayout.setError("*");
                validInput = false;
            }
        }

        String lastVisitOrDrinkCreditMaxStr = lastVisitOrDrinkCreditMax.getText().toString();
        if (!lastVisitOrDrinkCreditMaxStr.isEmpty()){
            if (lastVisitOrDrinkCreditMaxStr.matches(Constants.AT_LEAST_ONE_DIGIT_REGEXP)){
                lastVisitOrDrinkCreditMaxLayout.setErrorEnabled(false);
                lastVisitOrDrinkCreditMaxInt = Integer.parseInt(lastVisitOrDrinkCreditMaxStr);
            }
            else {
                lastVisitOrDrinkCreditMaxLayout.setError("*");
                validInput = false;
            }
        }

        if (lastVisitOrDrinkCreditMinInt != 0 && lastVisitOrDrinkCreditMaxInt != 0){
            if (lastVisitOrDrinkCreditMinInt > lastVisitOrDrinkCreditMaxInt) {
                //both min and max are specified, and min > max
                lastVisitOrDrinkCreditMinLayout.setError("*");
                validInput = false;
            }
            else {
                lastVisitOrDrinkCreditMinLayout.setErrorEnabled(false);
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

        String lastTextMaxDayStr = lastTextMaxDay.getText().toString();
        if (!lastTextMaxDayStr.isEmpty()){
            if (lastTextMaxDayStr.matches(Constants.AT_LEAST_ONE_DIGIT_REGEXP)){
                lastTextMaxLayout.setErrorEnabled(false);
                lastTextMaxDayInt = Integer.parseInt(lastTextMaxDayStr);
            }
            else {
                lastTextMaxLayout.setError("*");
                validInput = false;
            }
        }

        if (lastTextMinDayInt != 0 && lastTextMaxDayInt != 0){
            if (lastTextMinDayInt > lastTextMaxDayInt){
                //both specified, and min >= max
                lastTextMinLayout.setError("*");
                validInput = false;
            }
            else {
                lastTextMinLayout.setErrorEnabled(false);
            }
        }

        if (validInput){
            String sortByColumnUI = orderByDropdown.getSelectedItem().toString();
            String sortByDbColumn = DatabaseHandler.KEY_LAST_VISIT_DATE;
            if (sortByColumnUI.equals(getString(R.string.totalDrinkCreditText))){
                sortByDbColumn = DatabaseHandler.KEY_TOTALCREDIT;
            }
            else if (sortByColumnUI.equals(getString(R.string.daysSinceLastText))){
                sortByDbColumn = DatabaseHandler.KEY_LAST_CONTACTED_DATE;
            }

            String sortOrder = ascDescDropdown.getSelectedItem().toString();
            final String asc = getString(R.string.asc);
            final String desc = getString(R.string.desc);

            if (sortByDbColumn.equals(DatabaseHandler.KEY_LAST_CONTACTED_DATE) ||
                    sortByDbColumn.equals(DatabaseHandler.KEY_LAST_VISIT_DATE)){
                //flip the sort order
                sortOrder = sortOrder.equals(asc)? desc : asc;
            }

            Customer[] customers = searchCustomers(isDaysNotVisitedSelection, lastVisitOrDrinkCreditMinInt,
                    lastVisitOrDrinkCreditMaxInt, lastTextMinDayInt, lastTextMaxDayInt, sortByDbColumn, sortOrder);

            // Create an adapter to bind data to the ListView
            CustomerListViewAdapter adapter = new CustomerListViewAdapter(getContext(), R.layout.dashboard_row_layout, R.id.phone, customers);

            // Bind data to the ListView
            listView.setAdapter(adapter);
        }
    }

    private Customer[] searchCustomers(boolean isDaysNotVisitedSelection, int lastVisitMinDayInt, int lastVisitMaxDayInt,
                                       int lastTextMinDayInt, int lastTextMaxDayInt,
                                       String sortByDbColumn, String sortOrder) {

        List<Customer> customerList = handler.searchCustomerByLastVisitAndText(isDaysNotVisitedSelection, lastVisitMinDayInt,
                                                lastVisitMaxDayInt, lastTextMinDayInt, lastTextMaxDayInt, sortByDbColumn, sortOrder);
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
