package com.kpblog.tt;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kpblog.tt.adapter.AddressAdapter;
import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.model.Customer;
import com.kpblog.tt.model.CustomerPurchase;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_RegisterOrUpdate.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_RegisterOrUpdate#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_RegisterOrUpdate extends Fragment implements TextView.OnEditorActionListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final int FREE_DRINK_THRESHOLD = 10;
    public static final int TODAY_CREDIT_LIMIT = 10; //number of drinks that can be purchased at 1 time (to avoid typo)
    public static final String AT_LEAST_ONE_DIGIT_REGEXP = "[0-9]+";
    private EditText phone, todayCredit, previousCredit, missingCredit, receiptNum;
    private Button confirmBtn, cancelBtn;
    private CheckBox optIn;
    private DatabaseHandler handler;
    private ListView listView;
    private AddressAdapter addressAdapter;
    public List<Customer> list;
    private Customer customer;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private View view;

    public Fragment_RegisterOrUpdate() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_RegisterOrUpdate.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_RegisterOrUpdate newInstance(String param1, String param2) {
        Fragment_RegisterOrUpdate fragment = new Fragment_RegisterOrUpdate();
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
        View view = inflater.inflate(R.layout.fragment_register_or_update, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        phone = (EditText)(getView().findViewById(R.id.phone));
        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus){
                    if (getCustomerInfoFromDatabaseAndUpdateScreen()){
                        //requestFocusOnTodayCredit();
                        //don't request focus here because if the user presses a different input field, then there'd be 2 fields with focus
                    }
                }
            }
        });
        phone.setOnEditorActionListener(this);

        previousCredit = (EditText) getView().findViewById(R.id.previousCredit);
        previousCredit.setText(String.valueOf(0));

        todayCredit = (EditText) getView().findViewById(R.id.todayCredit);
        todayCredit.setText(String.valueOf(1));
        todayCredit.setOnEditorActionListener(this);
        todayCredit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus){
                    if(isTodayCreditValid()){
                        updateMissingCredit();
                        //((EditText) getView().findViewById(R.id.receiptNumber)).requestFocus();
                        //don't request focus here because if the user press a different input field, then there'll be 2 fields with focus
                    }
                }
            }
        });

        missingCredit = (EditText) getView().findViewById(R.id.missingCredit);
        missingCredit.setText(String.valueOf(FREE_DRINK_THRESHOLD - getTodayCredit()));

        receiptNum = (EditText) getView().findViewById(R.id.receiptNumber);
        receiptNum.setOnEditorActionListener(this);

        optIn = (CheckBox) getView().findViewById(R.id.checkbox_optIn);
        optIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //there's an issue while on todayCredit, user clicks back,
                //after exiting the keyboard, the focus is still on the todayCredit
                todayCredit.clearFocus();
                phone.clearFocus();
            }
        });

        confirmBtn = (Button) getView().findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerOrUpdateCustomer();
            }
        });

        cancelBtn = (Button) getView().findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                gotoHomeScreen();
            }
        });

        handler = new DatabaseHandler(getContext());
        List<CustomerPurchase> cpList = handler.getAllCustomerPurchase();
        Log.d("CustomerPurchase: ", cpList.toString());

        //uncomment to see the db entries on screen
        /*listView = (ListView) getView().findViewById(R.id.addressListView);
        list = handler.getAllAddress();
        addressAdapter = new AddressAdapter(this);
        listView.setAdapter(addressAdapter);*/
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

    private boolean isTodayCreditValid() {
        boolean isValid = false;
        EditText todayCreditEditText = (EditText)(getView().findViewById(R.id.todayCredit));
        final String todayCreditStr = todayCreditEditText.getText().toString();
        if (todayCreditStr != null && todayCreditStr.matches(AT_LEAST_ONE_DIGIT_REGEXP)){
            int todayCredit = Integer.parseInt(todayCreditStr);
            isValid = (todayCredit < TODAY_CREDIT_LIMIT);
        }

        TextInputLayout todayCreditLayout = (TextInputLayout) getView().findViewById(R.id.todayCreditlayout);
        if (isValid){
            todayCreditLayout.setErrorEnabled(false);
        }
        else {
            todayCreditLayout.setError(getString(R.string.todayCredit_err_msg));
            //((EditText) getView().findViewById(R.id.todayCredit)).requestFocus();
        }
        return isValid;
    }


    String confirmationMsg, targetPhoneNum;
    private void sendConfirmText(String phoneNum) {
        try {
            confirmationMsg = getString(R.string.welcomeText);
            targetPhoneNum = phoneNum;
            requestSmsPermission();
            //Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getContext().getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }


    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 123;

    private void requestSmsPermission() {

        // check permission is given
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // request permission (see result in onRequestPermissionsResult() method)
            requestPermissions(new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        } else {
            // permission already granted run sms send
            sendSms(targetPhoneNum, confirmationMsg);
            Toast.makeText(getContext().getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    sendSms(targetPhoneNum, confirmationMsg);
                    Toast.makeText(getContext().getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
                } else {
                    // permission denied
                    Toast.makeText(getContext().getApplicationContext(), "permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void sendSms(String phoneNumber, String message){
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
        gotoHomeScreen();
    }

    private void gotoHomeScreen() {
        //clear all fields
        phone.setText("");
        TextInputLayout phoneLayout = (TextInputLayout) getView().findViewById(R.id.phoneLayout);
        phoneLayout.setErrorEnabled(false);

        previousCredit.setText("0");

        TextInputLayout todayCreditLayout = (TextInputLayout) getView().findViewById(R.id.todayCreditlayout);
        todayCreditLayout.setErrorEnabled(false);
        todayCredit.setText(String.valueOf(1));

        receiptNum.setText("");
        TextInputLayout receiptLayout = (TextInputLayout) getView().findViewById(R.id.receiptLayout);
        receiptLayout.setErrorEnabled(false);

        missingCredit.setText(String.valueOf(FREE_DRINK_THRESHOLD - getTodayCredit()));
        optIn.setVisibility(View.VISIBLE);

        phone.requestFocus();

        /*android.support.v4.app.FragmentTransaction ftr = getFragmentManager().beginTransaction();
        ftr.detach(Fragment_RegisterOrUpdate.this).attach(Fragment_RegisterOrUpdate.this).commit();*/

        /*Fragment_RegisterOrUpdate fragment = (Fragment_RegisterOrUpdate)
                getFragmentManager().findFragmentById(R.id.your_fragment_container_id);

        getFragmentManager().beginTransaction()
                .detach(fragment)
                .attach(fragment)
                .commit();*/

        /*go back to home screen
        Intent myIntent = new Intent(MainActivity.this, MainActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        MainActivity.this.startActivity(myIntent);*/
    }

    private void updateMissingCredit() {
        //done with editing
        EditText missingCreditView = (EditText) getView().findViewById(R.id.missingCredit);
        int previousCreditValue = getPreviousCredit();

        int todayCredit = getTodayCredit();
        int totalCredit = previousCreditValue + todayCredit;
        missingCreditView = (EditText) getView().findViewById(R.id.missingCredit);
        final int missingCredit = FREE_DRINK_THRESHOLD - totalCredit;

        if (missingCredit > 0){
            ((TextInputLayout) getView().findViewById(R.id.missingCreditlayout)).setHintEnabled(true);
            missingCreditView.setText(String.valueOf(missingCredit));
        }
        else {
            //qualifies for free drink
            missingCreditView.setText(R.string.freeDrinkAchieved);
            ((TextInputLayout) getView().findViewById(R.id.missingCreditlayout)).setHintEnabled(false);
            //TODO: need to reset the credit
        }
    }

    private int getPreviousCredit() {
        int previousCreditValue = 0;
        final String previousCreditStr = previousCredit.getText().toString();
        if (!previousCreditStr.isEmpty()){
            previousCreditValue = Integer.parseInt(previousCreditStr);
        }
        return previousCreditValue;
    }

    private int getTodayCredit() {
        final String todayCreditStr = todayCredit.getText().toString();
        int todayCredit = 0;
        if (todayCreditStr != null && !todayCreditStr.isEmpty()){
            todayCredit = Integer.parseInt(todayCreditStr);
        }
        return todayCredit;
    }

    @Override
    //This is when the user press done on the keypad after entering phone number
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;
        String id = v.getResources().getResourceEntryName(v.getId());
        String phoneId = phone.getResources().getResourceEntryName(phone.getId());
        String todayCreditId = todayCredit.getResources().getResourceEntryName(todayCredit.getId());
        String receiptNumId = receiptNum.getResources().getResourceEntryName(receiptNum.getId());

        if (id.equals(phoneId)) {
            //when user is done entering phone number
            if (getCustomerInfoFromDatabaseAndUpdateScreen()){
                //don't request the focus if the phone number entry isnt valid
                requestFocusOnTodayCredit();
            }
            handled = true;
        }
        else if(id.equals(todayCreditId)){
            //when user is done entering today's drink
            if (isTodayCreditValid()){
                updateMissingCredit();
                ((EditText) getView().findViewById(R.id.receiptNumber)).requestFocus();
            }
        }
        else if (id.equals(receiptNumId)){
            isReceiptNumberValid();
        }

        return handled;
    }

    private void requestFocusOnTodayCredit() {
        //clearCurrentFocus();
        todayCredit = (EditText) getView().findViewById(R.id.todayCredit);
        todayCredit.requestFocus();
        todayCredit.setSelection(todayCredit.getText().length());
    }

    private boolean isReceiptNumberValid(){
        boolean isValid = false;
        receiptNum = (EditText) getView().findViewById(R.id.receiptNumber);
        String receiptNumStr = receiptNum.getText().toString();

        TextInputLayout receiptNumLayout = (TextInputLayout) getView().findViewById(R.id.receiptLayout);
        if (receiptNumStr != null && receiptNumStr.matches(AT_LEAST_ONE_DIGIT_REGEXP)){
            receiptNumLayout.setErrorEnabled(false);
            isValid = true;
        }
        else {
            receiptNumLayout.setError(getString(R.string.receipt_err_msg));
            //receiptNum.requestFocus();
        }

        return isValid;
    }

    private String getUnformattedPhoneNumber(){
        return PhoneNumberUtils.normalizeNumber(this.phone.getText().toString());
    }

    private boolean getCustomerInfoFromDatabaseAndUpdateScreen() {
        if (!isPhoneNumberValid()){
            return false;
        }

        //get customer by phone, if not exist, add
        final String phone = getUnformattedPhoneNumber();
        customer = handler.getCustomerByPhone(phone);

        if (customer != null){
            //existing customer, update the screen with customer info
            int todayCredit = getTodayCredit();
            int previousCreditValue = customer.getTotalCredit();
            if (customer.isOptIn()){
                //hide the checkbox if customer already opted in
                ((CheckBox) getView().findViewById(R.id.checkbox_optIn)).setVisibility(View.INVISIBLE);
            }
            else {
                //handles when 1 number was entered, then another one was enter
                ((CheckBox) getView().findViewById(R.id.checkbox_optIn)).setVisibility(View.VISIBLE);
            }

            previousCredit = (EditText) getView().findViewById(R.id.previousCredit);
            previousCredit.setText(String.valueOf(previousCreditValue));

            updateMissingCredit();
        }
        else {
            //new customer
            //handles the case where customer looked up 1 number, and then enter a different number, so need to reload
            int todayCredit = getTodayCredit();
            ((CheckBox) getView().findViewById(R.id.checkbox_optIn)).setVisibility(View.VISIBLE);

            int previousCreditValue = 0;
            previousCredit = (EditText) getView().findViewById(R.id.previousCredit);
            previousCredit.setText(String.valueOf(previousCreditValue));

            updateMissingCredit();
        }


        /*list = handler.getAllAddress();
        addressAdapter.notifyDataSetChanged();*/

        return true;
    }

    private boolean isPhoneNumberValid() {
        boolean isValid = false;
        String inputPhoneNum = null;
        TextInputLayout phoneLayout = (TextInputLayout) getView().findViewById(R.id.phoneLayout);
        try {
            inputPhoneNum = getUnformattedPhoneNumber();
            if (inputPhoneNum != null && inputPhoneNum.matches("[0-9]{10}")){
                phoneLayout.setErrorEnabled(false);
                isValid = true;
            }
            else {
                phoneLayout.setError(getString(R.string.phone_err_msg));
                //setFocusOnPhone();
            }
        } catch (Exception e){
            phoneLayout.setError(getString(R.string.phone_err_msg));
            //setFocusOnPhone();
        }


        return isValid;
    }

    private void setFocusOnPhone() {
        //clearCurrentFocus();
        phone.requestFocus();
    }


    public void deleteCustomer(Customer customer){
        handler.deleteCustomer(customer);
    }

    private boolean isAllInputValid(){
        return isPhoneNumberValid() && isTodayCreditValid() && isReceiptNumberValid();
    }

    /**
     * register new customer, and update existing customer.
     * using SQLiteDatabase.insertWithOnConflict() using CONFLICT_IGNORE to avoid overwriting existing data
     * https://stackoverflow.com/questions/13311727/android-sqlite-insert-or-update
     */
    private boolean registerOrUpdateCustomer() {
        if (!isAllInputValid()){
            return false;
        }

        boolean success = false;
        boolean sendConfirmationText = false;
        try {
            final Date today = new Date(Calendar.getInstance().getTime().getTime());

            if (customer == null){
                customer = new Customer();
                customer.setCustomerId(getUnformattedPhoneNumber());
            }

            //only set the opt-in date if it's checked.
            //if customer already opted-in, we'd hide the checkbox. They can opt-out from the admin screen
            if (optIn.isChecked()){
                customer.setOptIn(optIn.isChecked());
                customer.setOptInDate(today);

                //clear out opt-out date
                customer.setOptOutDate(null);

                sendConfirmationText = true;
            }
            customer.setTotalCredit(getPreviousCredit() + getTodayCredit());
            customer.setLastVisitDate(today);

            handler.registerOrUpdateCustomer(customer);

            insertCustomerPurchase(customer.getCustomerId(), getTodayCredit());

            if (sendConfirmationText){
                sendConfirmText(customer.getCustomerId());
            }
            else {
                gotoHomeScreen();
            }

            success = true;
        }
        catch (Exception e){

        }

        return success;
    }

    private void insertCustomerPurchase(String customerId, int todayCredit) {
        CustomerPurchase cp = new CustomerPurchase();

        cp.setCustomerId(customerId);
        cp.setQuantity(todayCredit);
        cp.setPurchaseDate(new java.util.Date());
        cp.setReceiptNum(Integer.parseInt(receiptNum.getText().toString()));

        handler.insertCustomerPurchase(cp);
    }
}
