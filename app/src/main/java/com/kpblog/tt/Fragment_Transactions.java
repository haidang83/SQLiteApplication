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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.kpblog.tt.adapter.CustomerPurchaseListViewAdapter;
import com.kpblog.tt.adapter.TransactionListViewAdapter;
import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.model.CustomerPurchase;
import com.kpblog.tt.util.Constants;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_Transactions.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Transactions#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Transactions extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    long searchBtnLastClicked = 0;
    ListView listview;

    private OnFragmentInteractionListener mListener;

    public Fragment_Transactions() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_Transactions.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Transactions newInstance(String param1, String param2) {
        Fragment_Transactions fragment = new Fragment_Transactions();
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
        return inflater.inflate(R.layout.fragment__transactions, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ((EditText) getView().findViewById(R.id.result)).setText("0");

        Button search = (Button) getView().findViewById(R.id.searchBtn);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - searchBtnLastClicked > Constants.BUTTON_CLICK_ELAPSE_THRESHOLD) {
                    //hide soft keyboard
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                    searchBtnLastClicked = SystemClock.elapsedRealtime();

                    validateInputAndSearchTransactions();

                }
            }
        });

        listview = (ListView) getView().findViewById(R.id.listview);
        // Inflate customerHeader view
        ViewGroup headerView = (ViewGroup)getLayoutInflater().inflate(R.layout.transaction_header, listview,false);
        // Add customerHeader view to the ListView
        listview.addHeaderView(headerView);
    }

    private void validateInputAndSearchTransactions() {
        Spinner transactionTypeDropdown = (Spinner) getView().findViewById(R.id.transactionTypeDropdown);

        String transactionType = transactionTypeDropdown.getSelectedItem().toString();

        TextInputLayout daysAgoLayout = (TextInputLayout) getView().findViewById(R.id.daysAgoLayout);
        EditText daysAgo = (EditText) getView().findViewById(R.id.daysAgo);
        String daysAgoStr = daysAgo.getText().toString();
        int daysAgoInt = 0;
        boolean isValidInput = true, allTime = false;
        if (!daysAgoStr.isEmpty()){
            if (!daysAgoStr.matches(Constants.AT_LEAST_ONE_DIGIT_REGEXP)) {
                daysAgoLayout.setError(getString(R.string.numericOnlyInput));
                isValidInput = false;
            }
            else {
                daysAgoLayout.setErrorEnabled(false);
                daysAgoInt = Integer.parseInt(daysAgoStr);
            }
        }
        else {
            allTime = true;
        }

        if (isValidInput){
            String note = "";
            boolean drinkClaimTransaction = false;
            if (transactionType.equals(getString(R.string.drinkClaimType))){
                note = getString(R.string.freeDrink_claim);
                drinkClaimTransaction = true;
            }

            String orderByUiColumn = ((Spinner) getView().findViewById(R.id.orderByDropdown)).getSelectedItem().toString();

            //set purchase date to be default sort
            String orderByDbColumn = DatabaseHandler.KEY_PURCHASE_DATE;
            if (orderByUiColumn.equals(getString(R.string.orderByQuantity))){
                orderByDbColumn = DatabaseHandler.KEY_QUANTITY;
            }

            String ascDescSort = ((Spinner) getView().findViewById(R.id.ascDescDropdown)).getSelectedItem().toString();

            DatabaseHandler handler = new DatabaseHandler(getContext());
            CustomerPurchase[] cp = handler.getAllCustomerPurchaseByTypeAndTime(note, daysAgoInt, allTime, orderByDbColumn, ascDescSort, drinkClaimTransaction);

            // Create an adapter to bind data to the ListView
            TransactionListViewAdapter adapter = new TransactionListViewAdapter(getContext(), R.layout.transaction_row_layout, R.id.transactionTime, cp);

            // Bind data to the ListView
            listview.setAdapter(adapter);

            ((EditText) getView().findViewById(R.id.result)).setText(String.valueOf(cp.length));
        }

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
