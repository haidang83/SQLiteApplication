package com.kpblog.tt;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.model.Customer;
import com.kpblog.tt.model.CustomerClaimCode;
import com.kpblog.tt.model.CustomerPurchase;
import com.kpblog.tt.util.AsteriskPasswordTransformationMethod;
import com.kpblog.tt.util.Constants;
import com.kpblog.tt.util.MyEditText;
import com.kpblog.tt.util.Util;

import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_Claim.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Claim#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Claim extends Fragment implements TextView.OnEditorActionListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String CUSTOMER_ID_PARAM = "CUSTOMER_ID";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String customerId = "";
    private String mParam2;

    private EditText phone, claimCode, freeDrink, receiptNum, cashierCode, freeDrinkClaimToday;
    private Button claimBtn, clearBtn, getCodeBtn;
    private DatabaseHandler handler;

    private OnFragmentInteractionListener mListener;

    public Fragment_Claim() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param customerId Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_Claim.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Claim newInstance(String customerId, String param2) {
        Fragment_Claim fragment = new Fragment_Claim();
        Bundle args = new Bundle();
        args.putString(CUSTOMER_ID_PARAM, customerId);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            customerId = getArguments().getString(CUSTOMER_ID_PARAM);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment__claim, container, false);
    }


    private long getCodeBtnLastClicked = 0;
    private long claimBtnLastClicked = 0;
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        handler = new DatabaseHandler(getContext());
        phone = (EditText)(getView().findViewById(R.id.phone));
        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus){
                    if (updateCustomerFreeDrink()){
                        //requestFocusOnTodayCredit();
                        //don't request focus here because if the user presses a different input field, then there'd be 2 fields with focus
                    }
                }
            }
        });
        phone.setOnEditorActionListener(this);

        freeDrink = (EditText) (getView().findViewById(R.id.freeDrink));
        freeDrink.setText(String.valueOf(0));

        freeDrinkClaimToday = (EditText) getView().findViewById(R.id.freeDrinkClaimToday);
        freeDrinkClaimToday.setText(String.valueOf(0));
        freeDrinkClaimToday.setTransformationMethod(null);
        freeDrinkClaimToday.setOnEditorActionListener(this);

        cashierCode = (EditText) getView().findViewById(R.id.cashierCode);
        cashierCode.setTransformationMethod(new AsteriskPasswordTransformationMethod());
        cashierCode.setOnEditorActionListener(this);

        getCodeBtn = (Button) (getView().findViewById(R.id.getCodeBtn));
        getCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //to prevent double click
                if (SystemClock.elapsedRealtime() - getCodeBtnLastClicked > Constants.BUTTON_CLICK_ELAPSE_THRESHOLD){
                    requestClaimCode();
                    getCodeBtnLastClicked = SystemClock.elapsedRealtime();
                }
            }
        });

        claimBtn = (Button) (getView().findViewById(R.id.claimBtn));
        claimBtn.setEnabled(false);
        claimBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //to prevent double click
                if (SystemClock.elapsedRealtime() - claimBtnLastClicked > Constants.BUTTON_CLICK_ELAPSE_THRESHOLD){
                    claimBtnLastClicked = SystemClock.elapsedRealtime();
                    if (validateClaimCode() && validateAmountClaimToday() && validateReceiptNum() && validateCashierCode()){
                        updateSuccessfulClaim();
                        clearScreen();
                    }
                }
            }
        });

        clearBtn = (Button) (getView().findViewById(R.id.clearBtn));
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearScreen();
            }
        });

        claimCode = (MyEditText) (getView().findViewById(R.id.claimCode));
        claimCode.setTransformationMethod(null);
        claimCode.setOnEditorActionListener(this);

        receiptNum = (EditText) getView().findViewById(R.id.receiptNumber);
        receiptNum.setTransformationMethod(null);
        receiptNum.setOnEditorActionListener(this);

        if (Util.getUnformattedPhoneNumber(customerId).length() == 10){
            //this is to handle the case where this fragment's data is passed from the other fragment
            phone.setText(customerId);
            updateCustomerFreeDrink();
        }
    }

    private void updateSuccessfulClaim() {
        String customerId = Util.getUnformattedPhoneNumber(phone.getText().toString());

        //update the total credit
        Customer c = handler.getCustomerById(customerId);
        double totalCredit = c.getTotalCredit();
        int claimAmt = Integer.parseInt(freeDrinkClaimToday.getText().toString());
        double remainingCredit = totalCredit - (claimAmt * Constants.FREE_DRINK_THRESHOLD);
        handler.updateTotalCreditForCustomerId(customerId, remainingCredit);

        String receiptNumStr = receiptNum.getText().toString();
        recordClaimIntoCustomerPurchase(customerId, claimAmt, receiptNumStr);
        handler.deleteClaimCodeForCustomerId(customerId);
        claimBtn.setEnabled(false);

        if (c.isOptIn()){
            //send claim confirmation text
            String msg = String.format(getString(R.string.successfulClaim_text), remainingCredit);
            sendText(customerId, msg, null);
        }
        else {
            Toast.makeText(getContext().getApplicationContext(), getString(R.string.claimSuccess_msg), Toast.LENGTH_LONG).show();
        }

        updateInfoOnSignInTab();
    }

    private void updateInfoOnSignInTab() {
        String phoneNum = Util.getUnformattedPhoneNumber(this.phone.getText().toString());
        Fragment_RegisterOrUpdate frag = Fragment_RegisterOrUpdate.newInstance(phoneNum, null);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.registerFragment,frag).commit();
    }

    private void clearScreen() {
        phone.setText("");
        TextInputLayout phoneLayout = (TextInputLayout) (getView().findViewById(R.id.phoneLayout));
        phoneLayout.setErrorEnabled(false);

        claimCode.setText("");
        claimCode.setError(null, null);

        freeDrink.setText("0");
        freeDrinkClaimToday.setText("0");
        freeDrinkClaimToday.setEnabled(false);

        receiptNum.setText("");
        cashierCode.setText("");

        claimBtn.setEnabled(false);
        getCodeBtn.setEnabled(false);

    }

    private void recordClaimIntoCustomerPurchase(String customerId, int claimAmt, String receiptNum) {
        CustomerPurchase cp = new CustomerPurchase();

        cp.setCustomerId(customerId);
        cp.setQuantity(claimAmt);
        cp.setReceiptNum(Integer.parseInt(receiptNum));
        cp.setNotes(getString(R.string.freeDrink_claim));
        cp.setPurchaseDate(new java.util.Date());

        handler.insertCustomerPurchase(cp);
    }

    private boolean validateClaimCode() {
        boolean isSuccess = false;
        String code = claimCode.getText().toString();
        String unformattedPhoneNum = Util.getUnformattedPhoneNumber(this.phone.getText().toString());

        if (code != null && code.matches(Constants.FOUR_DIGIT_REGEXP)){
            String dbCode = handler.getClaimCodeByCustomerId(unformattedPhoneNum);
            isSuccess = code.equals(dbCode);
        }

        if (isSuccess){
            Drawable myIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_done_green_24dp);
            myIcon.setBounds(0, 0, myIcon.getIntrinsicWidth(), myIcon.getIntrinsicHeight());
            claimCode.setError(null, myIcon);
            getCodeBtn.setEnabled(false);
        }
        else {
            Drawable errorIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_error_red_24dp);
            errorIcon.setBounds(new Rect(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight()));
            claimCode.setError(null, errorIcon);
            getCodeBtn.setEnabled(true);
            //claimCodeLayout.setError(getString(R.string.claimCode_err_msg));
        }
        return isSuccess;
    }

    private boolean requestClaimCode(){
        boolean isSuccess = false;
        String unformattedPhoneNum = Util.getUnformattedPhoneNumber(this.phone.getText().toString());
        Customer c = handler.getCustomerById(unformattedPhoneNum);
        if (c != null && c.getTotalCredit() >= Constants.FREE_DRINK_THRESHOLD){
            //generate code and send sms
            String code = Util.generateRandom4DigitCode();
            String msg = String.format(getString(R.string.getCodeMsg), code);
            sendText(unformattedPhoneNum, msg, code);
        }

        isSuccess = true;
        return isSuccess;
    }


    String textMsg, targetPhoneNum, codeStr;
    private void sendText(String phoneNum, String msg, String code) {
        try {
            textMsg = msg;
            targetPhoneNum = phoneNum;
            codeStr = code;
            requestSmsPermission();
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
            sendSms(targetPhoneNum, textMsg, codeStr);
            displayToastToCashier();
        }
    }

    private void displayToastToCashier() {
        //2 scenarios to send sms: request code; or successful claim confirmation msg if customer opts in
        //the successful scenario will have codeStr = null
        if (codeStr != null && !codeStr.isEmpty()){
            //request code flow
            Toast.makeText(getContext().getApplicationContext(), "Code Sent", Toast.LENGTH_LONG).show();
        }
        else {
            //successful claim confirmation flow
            Toast.makeText(getContext().getApplicationContext(), getString(R.string.claimSuccess_msg_textSent), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    sendSms(targetPhoneNum, textMsg, codeStr);
                    displayToastToCashier();
                } else {
                    // permission denied
                    Toast.makeText(getContext().getApplicationContext(), "permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void sendSms(String phoneNumber, String message, String codeStr){
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);

        if (codeStr != null && !codeStr.isEmpty()){
            //codeStr is only empty when we send the confirmation msg after successful claim
            //so only update db if not empty
            insertOrUpdateClaimCodeDb(phoneNumber, codeStr);
        }
    }

    private void insertOrUpdateClaimCodeDb(String phoneNumber, String codeStr) {
        CustomerClaimCode cc = new CustomerClaimCode(phoneNumber, codeStr, new Date());
        handler.insertOrUpdateCustomerClaimCode(cc);
    }

    private boolean updateCustomerFreeDrink() {
        boolean hasFreeDrink = false;

        TextInputLayout phoneLayout = (TextInputLayout) (getView().findViewById(R.id.phoneLayout));
        String unformattedPhoneNum = Util.getUnformattedPhoneNumber(this.phone.getText().toString());

        if (!Util.isPhoneNumberValid(phoneLayout, getString(R.string.phone_err_msg), unformattedPhoneNum)){
            return false;
        }

        Customer customer = handler.getCustomerById(unformattedPhoneNum);
        int freeDrinkNum = 0;
        if (customer != null){
            freeDrinkNum = (int) (customer.getTotalCredit() / Constants.FREE_DRINK_THRESHOLD);
            hasFreeDrink = (freeDrinkNum > 0? true: false);
        }
        updateFreeDrink(freeDrinkNum);

        return hasFreeDrink;

    }

    private void updateFreeDrink(int freeDrinkNum) {
        EditText freeDrink = (EditText) (getView().findViewById(R.id.freeDrink));
        freeDrink.setText(String.valueOf(freeDrinkNum));
        getCodeBtn = (Button) (getView().findViewById(R.id.getCodeBtn));
        claimCode = (EditText) (getView().findViewById(R.id.claimCode));

        if (freeDrinkNum > 0){
            //enable the getCode button
            getCodeBtn.setEnabled(true);
            claimCode.setEnabled(true);
            claimBtn.setEnabled(true);

            //by default, set the amount claim today to whatever available
            freeDrinkClaimToday.setText(String.valueOf(freeDrinkNum));

            if (freeDrinkNum == 1){
                //disable the field
                freeDrinkClaimToday.setEnabled(false);
            }
            else {
                freeDrinkClaimToday.setEnabled(true);
            }
        }
        else {
            getCodeBtn.setEnabled(false);
            claimCode.setText("");
            claimCode.setEnabled(false);
            claimBtn.setEnabled(false);
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

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

        boolean handled = false;
        String id = textView.getResources().getResourceEntryName(textView.getId());
        String phoneId = phone.getResources().getResourceEntryName(phone.getId());
        String claimCodeId = claimCode.getResources().getResourceEntryName(claimCode.getId());
        String receiptNumId = receiptNum.getResources().getResourceEntryName(receiptNum.getId());
        String cashierCodeId = cashierCode.getResources().getResourceEntryName(cashierCode.getId());
        String amountClaimTodayId = freeDrinkClaimToday.getResources().getResourceEntryName(freeDrinkClaimToday.getId());

        if (id.equals(phoneId)) {
            //when user is done entering phone number
            if (updateCustomerFreeDrink()){
                //don't request the focus if the phone number entry isnt valid
                requestFocusOnClaimCode();
            }
            handled = true;
        }
        else if (id.equals(claimCodeId)){
            if (validateClaimCode()){
                //set focus to claimtoday field if enabled
                if (freeDrinkClaimToday.isEnabled()){
                    freeDrinkClaimToday.requestFocus();
                    freeDrinkClaimToday.setSelection(freeDrinkClaimToday.getText().length());
                }
                else {
                    //set focus to receipt number
                    receiptNum.requestFocus();
                }
            }
        }
        else if (id.equals(amountClaimTodayId)){
            if (validateAmountClaimToday()){
                receiptNum.requestFocus();
            }
        }
        else if (id.equals(receiptNumId)){
            if (validateReceiptNum()){
                cashierCode.requestFocus();
            }
        }
        else if (id.equals(cashierCodeId)){
            validateCashierCode();
        }

        return false;
    }

    private boolean validateAmountClaimToday() {
        boolean isValid = false;
        String amountClaimTodayStr = freeDrinkClaimToday.getText().toString();
        if (!amountClaimTodayStr.isEmpty()){
            int amtClaim = Integer.parseInt(amountClaimTodayStr);
            TextInputLayout freeDrinkClaimTodayLayout = (TextInputLayout) getView().findViewById(R.id.freeDrinkClaimTodayLayout);
            if (amtClaim == 0 || amtClaim > Integer.parseInt(freeDrink.getText().toString())){
                freeDrinkClaimTodayLayout.setError("Invalid Amount");
            }
            else {
                freeDrinkClaimTodayLayout.setErrorEnabled(false);
                isValid = true;
            }
        }

        return isValid;
    }

    private boolean validateCashierCode() {
        String inputCashierCode = cashierCode.getText().toString();
        TextInputLayout cashierLayout = (TextInputLayout) getView().findViewById(R.id.cashierCodeLayout);
        String cashierCodeErrMsg = getString(R.string.claimCode_err_msg);
        return Util.isCashierCodeValid(inputCashierCode, getContext(), cashierLayout, cashierCodeErrMsg);
    }

    private boolean validateReceiptNum() {
        TextInputLayout receiptNumLayout = (TextInputLayout) getView().findViewById(R.id.receiptLayout);
        String receiptNumStr = receiptNum.getText().toString();
        boolean isValid = false;

        if (!receiptNumStr.isEmpty() && receiptNumStr.matches(Constants.AT_LEAST_ONE_DIGIT_REGEXP)){
            isValid = true;
            receiptNumLayout.setErrorEnabled(false);
        }
        else {
            receiptNumLayout.setError(getString(R.string.receipt_err_msg));
        }

        return isValid;
    }

    private void requestFocusOnClaimCode() {
        EditText claimCode = (EditText) (getView().findViewById(R.id.claimCode));
        claimCode.requestFocus();
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
