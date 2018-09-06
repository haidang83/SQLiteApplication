package com.kpblog.tt;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.TabLayout;
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

import com.kpblog.tt.adapter.CustomerListViewAdapter;
import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.model.Customer;
import com.kpblog.tt.util.Constants;

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
    EditText lastVisitMin, lastVisitMax, lastTextMinDay, lastTextMaxDay, drinkCreditMin, drinkCreditMax;
    TextInputLayout lastVisitMinLayout, lastVisitMaxLayout, lastTextMinLayout,
                    lastTextMaxLayout;
    ListView listView;
    Button search;
    long searchBtnLastClicked = 0, sendTextBtnLastClicked = 0;
    Spinner orderByDropdown, ascDescDropdown, templateQuery;

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

        templateQuery = (Spinner) getView().findViewById(R.id.templateQueryType);
        templateQuery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                handleTemplateQuerySelection();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        lastVisitMinLayout = (TextInputLayout) getView().findViewById(R.id.lastVisitMinLayout);
        lastVisitMin = (EditText) getView().findViewById(R.id.lastVisitMin);

        lastVisitMaxLayout = (TextInputLayout) getView().findViewById(R.id.lastVisitMaxLayout);
        lastVisitMax = (EditText) getView().findViewById(R.id.lastVisitMax);

        lastTextMinLayout = (TextInputLayout) getView().findViewById(R.id.lastTextMinLayout);
        lastTextMinDay = (EditText) getView().findViewById(R.id.lastTextMinimumDay);

        lastTextMaxLayout = (TextInputLayout) getView().findViewById(R.id.lastTextMaxLayout);
        lastTextMaxDay = (EditText) getView().findViewById(R.id.lastTextMaximumDay);

        drinkCreditMin = (EditText) getView().findViewById(R.id.drinkCreditMin);
        drinkCreditMax = (EditText) getView().findViewById(R.id.drinkCreditMax);

        orderByDropdown = (Spinner) getView().findViewById(R.id.orderByDropdown);

        ascDescDropdown = (Spinner) getView().findViewById(R.id.ascDescDropdown);

        search = (Button) getView().findViewById(R.id.searchBtn);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //hide soft keyboard
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                if (SystemClock.elapsedRealtime() - searchBtnLastClicked > Constants.BUTTON_CLICK_ELAPSE_THRESHOLD) {

                    searchBtnLastClicked = SystemClock.elapsedRealtime();
                    handleSearchClick();
                }
            }
        });

        Button sendTextBtn = (Button) getView().findViewById(R.id.sendTextBtn);
        sendTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - sendTextBtnLastClicked > Constants.BUTTON_CLICK_ELAPSE_THRESHOLD){
                    sendTextBtnLastClicked = SystemClock.elapsedRealtime();
                    Customer[] customers = getOptInCustomerMeetingCriteria();
                    Fragment_Text text = Fragment_Text.newInstance(customers, templateQuery.getSelectedItem().toString());
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.textFragment,text).commit();
                    TabLayout tabs = (TabLayout)((MainActivity)getActivity()).findViewById(R.id.tabs);
                    tabs.getTabAt(5).select();
                }
            }
        });

        Button clearBtn = (Button) getView().findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearInputs();
            }
        });



        listView = (ListView) getView().findViewById(R.id.listview);
        // Inflate customerHeader view
        ViewGroup headerView = (ViewGroup)getLayoutInflater().inflate(R.layout.dashboard_header, listView,false);
        // Add customerHeader view to the ListView
        listView.addHeaderView(headerView);
    }

    private void handleTemplateQuerySelection() {
        String queryType = templateQuery.getSelectedItem().toString();

        if (getString(R.string.queryType_inactiveOldPromo).equals(queryType) ||
            getString(R.string.queryType_inactiveNewPromo).equals(queryType)) {

            //on the UI side, both types of inactive have the same criteria
            //but on the backend, we'll separate users already with promo and user without
            lastVisitMin.setText(String.valueOf(Constants.INACTIVE_LAST_VISIT_MIN));
            lastVisitMax.setText(String.valueOf(Constants.INACTIVE_LAST_VISIT_MAX));
            lastTextMinDay.setText(String.valueOf(Constants.INACTIVE_LAST_TEXTED_MIN));
            lastTextMaxDay.setText(String.valueOf(Constants.INACTIVE_LAST_TEXTED_MAX));

            //the same customer might fall under both scenario, so for inactive, set credit from 1-6
            //since the reminder is for 7-10
            drinkCreditMin.setText(String.valueOf(Constants.INACTIVE_CREDIT_MIN));
            drinkCreditMax.setText(String.valueOf(Constants.INACTIVE_CREDIT_MAX));
        }
        else if (getString(R.string.queryType_drinkCreditReminder).equals(queryType)){
            lastVisitMin.setText(String.valueOf(Constants.DRINK_REMINDER_LAST_VISIT_MIN));
            lastVisitMax.setText(String.valueOf(Constants.DRINK_REMINDER_LAST_VISIT_MAX));
            lastTextMinDay.setText(String.valueOf(Constants.DRINK_REMINDER_LAST_TEXTED_MIN));
            lastTextMaxDay.setText(String.valueOf(Constants.DRINK_REMINDER_LAST_TEXTED_MAX));
            drinkCreditMin.setText(String.valueOf(Constants.DRINK_REMINDER_CREDIT_MIN));
            drinkCreditMax.setText(String.valueOf(Constants.DRINK_REMINDER_CREDIT_MAX));

            orderByDropdown.setSelection(0);//days not visit
            ascDescDropdown.setSelection(1);//desc
        }
        else {
            clearInputs();
        }

        handleSearchClick();
    }

    private void handleSearchClick() {

        Customer[] customers = validateInputAndSearchCustomers();
        if (customers != null){
            // Create an adapter to bind data to the ListView
            CustomerListViewAdapter adapter = new CustomerListViewAdapter(getContext(), R.layout.dashboard_row_layout, R.id.phone, customers);
            // Bind data to the ListView
            listView.setAdapter(adapter);
            ((EditText) getView().findViewById(R.id.result)).setText(String.valueOf(customers.length));
        }
    }

    private Customer[] getOptInCustomerMeetingCriteria() {
        Customer[] customers = validateInputAndSearchCustomers();
        List<Customer> optInCustomers = new ArrayList<Customer>();

        for (Customer c : customers){
            if (c.isOptIn()){
                optInCustomers.add(c);
            }
        }

        return optInCustomers.toArray(new Customer[0]);
    }

    private void clearInputs() {
        lastVisitMin.setText("");
        lastVisitMax.setText("");
        lastTextMinDay.setText("");
        lastTextMaxDay.setText("");
        drinkCreditMin.setText("");
        drinkCreditMax.setText("");
        ((EditText) getView().findViewById(R.id.result)).setText("");
    }

    /**
     * not needed anymore, but kept as an example of setting the spinner style in xml and instantiate the adapter
     */
    private void setOrderByDropdownAdapter() {
        ArrayList<String> orderByValues = new ArrayList<String>();
        orderByValues.add(getString(R.string.daysSinceLastText));
        ArrayAdapter<String> orderByDropdownAdapter = new ArrayAdapter<String>(getContext(), R.layout.orderby_spinner_item, orderByValues);
        orderByDropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderByDropdown.setAdapter(orderByDropdownAdapter);
    }

    private Customer[] validateInputAndSearchCustomers() {
        boolean validInput = true;
        Customer[] customers = null;

        int lastVisitMinInt = 0, lastVisitMaxInt = 0, lastTextMinDayInt = 0, lastTextMaxDayInt = 0;


        String lastVisitMinStr = lastVisitMin.getText().toString();
        if (!lastVisitMinStr.isEmpty()){
            if (lastVisitMinStr.matches(Constants.AT_LEAST_ONE_DIGIT_REGEXP)) {
                lastVisitMinLayout.setErrorEnabled(false);
                lastVisitMinInt = Integer.parseInt(lastVisitMinStr);
            }
            else {
                lastVisitMinLayout.setError("*");
                validInput = false;
            }
        }

        String lastVisitMaxStr = lastVisitMax.getText().toString();
        if (!lastVisitMaxStr.isEmpty()){
            if (lastVisitMaxStr.matches(Constants.AT_LEAST_ONE_DIGIT_REGEXP)){
                lastVisitMaxLayout.setErrorEnabled(false);
                lastVisitMaxInt = Integer.parseInt(lastVisitMaxStr);
            }
            else {
                lastVisitMaxLayout.setError("*");
                validInput = false;
            }
        }

        if (lastVisitMinInt != 0 && lastVisitMaxInt != 0){
            if (lastVisitMinInt > lastVisitMaxInt) {
                //both min and max are specified, and min > max
                lastVisitMinLayout.setError("*");
                validInput = false;
            }
            else {
                lastVisitMinLayout.setErrorEnabled(false);
            }
        }

        String drinkCreditMinStr = drinkCreditMin.getText().toString();
        String drinkCreditMaxStr = drinkCreditMax.getText().toString();
        TextInputLayout drinkCreditMinLayout = (TextInputLayout) getView().findViewById(R.id.drinkCreditMinLayout);
        TextInputLayout drinkCreditMaxLayout = (TextInputLayout) getView().findViewById(R.id.drinkCreditMaxLayout);
        double drinkCreditMinDouble = 0, drinkCreditMaxDouble = 0;
        if (!drinkCreditMinStr.isEmpty()){
            if (drinkCreditMinStr.matches(Constants.NUMBER_OR_DECIMAL_REGEXP)){
                drinkCreditMinLayout.setErrorEnabled(false);
                drinkCreditMinDouble = Double.parseDouble(drinkCreditMinStr);
            }
            else {
                drinkCreditMinLayout.setError("*");
                validInput = false;
            }
        }

        if (!drinkCreditMaxStr.isEmpty()){
            if (drinkCreditMaxStr.matches(Constants.NUMBER_OR_DECIMAL_REGEXP)){
                drinkCreditMaxLayout.setErrorEnabled(false);
                drinkCreditMaxDouble = Double.parseDouble(drinkCreditMaxStr);
            }
            else {
                drinkCreditMaxLayout.setError("*");
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
                sortByDbColumn = DatabaseHandler.KEY_TOTAL_CREDIT;
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

            customers = searchCustomers(lastVisitMinInt, lastVisitMaxInt,
                                lastTextMinDayInt, lastTextMaxDayInt,
                                drinkCreditMinDouble, drinkCreditMaxDouble,
                                sortByDbColumn, sortOrder, getExistingPromoRequirement());
        }

        return customers;
    }

    private Constants.EXISTING_PROMO_REQUIREMENT getExistingPromoRequirement() {
        String queryType = templateQuery.getSelectedItem().toString();
        Constants.EXISTING_PROMO_REQUIREMENT promo_requirement = Constants.EXISTING_PROMO_REQUIREMENT.IGNORE;
        if (getString(R.string.queryType_inactiveNewPromo).equals(queryType)){
            promo_requirement = Constants.EXISTING_PROMO_REQUIREMENT.NEITHER_EXISTING_PROMO_NOR_FREE_DRINK;
        }
        else if (getString(R.string.queryType_inactiveOldPromo).equals(queryType)){
            promo_requirement = Constants.EXISTING_PROMO_REQUIREMENT.HAS_EXISTING_PROMO_ONLY;
        }

        return promo_requirement;
    }

    private Customer[] searchCustomers(int lastVisitMinDayInt, int lastVisitMaxDayInt,
                                       int lastTextMinDayInt, int lastTextMaxDayInt,
                                       double drinkCreditMinDouble, double drinkCreditMaxDouble,
                                       String sortByDbColumn, String sortOrder, Constants.EXISTING_PROMO_REQUIREMENT existing_promo_requirement) {

        List<Customer> customerList = handler.searchCustomerByLastVisitAndText(lastVisitMinDayInt,
                                                lastVisitMaxDayInt, lastTextMinDayInt, lastTextMaxDayInt,
                                                drinkCreditMinDouble, drinkCreditMaxDouble, sortByDbColumn, sortOrder, existing_promo_requirement);
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
