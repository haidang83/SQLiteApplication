package com.kpblog.sqliteapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kpblog.sqliteapplication.adapter.AddressAdapter;
import com.kpblog.sqliteapplication.dao.DatabaseHandler;
import com.kpblog.sqliteapplication.model.Customer;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;

/**
 * issues:
 * 1. only update the opt-in, opt-out date when the value was changed from previous value (don't update every time) [DONE]
 * 2. don't show the opt-in if user already opted in(we'll have an unsubscribe button on the admin tab to opt-out)
 * 3. update missing credit when Phone/previousCredit/todayCredit is updated [DONE]
 *
 * 4. tab to claim discount:
 *      a. if previous credit = 10, go to discount tab with phone number filled in
 *      b. if previous credit + todayCredit = 10, pressing confirm will take user to discount tab with phone number filled in
 *
 * 5. Admin tab
 *      a. opt-out
 *      b. add test user
 *      c. export/import db
 *      d. raffle/promotion
 */
public class MainActivity extends AppCompatActivity implements TextView.OnEditorActionListener{

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phone = (EditText)findViewById(R.id.phone);
        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus){
                    if (getCustomerInfoFromDatabaseAndUpdateScreen()){
                        requestFocusOnTodayCredit();
                    }
                }
            }
        });
        phone.setOnEditorActionListener(this);

        previousCredit = (EditText) findViewById(R.id.previousCredit);
        previousCredit.setText(String.valueOf(0));

        todayCredit = (EditText) findViewById(R.id.todayCredit);
        todayCredit.setText(String.valueOf(1));
        todayCredit.setOnEditorActionListener(this);
        todayCredit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus){
                    if(isTodayCreditValid()){
                        updateMissingCredit();
                        ((EditText) findViewById(R.id.receiptNumber)).requestFocus();
                    }
                }
            }
        });

        missingCredit = (EditText) findViewById(R.id.missingCredit);
        missingCredit.setText(String.valueOf(FREE_DRINK_THRESHOLD - getTodayCredit()));

        receiptNum = (EditText) findViewById(R.id.receiptNumber);
        receiptNum.setOnEditorActionListener(this);

        optIn = (CheckBox) findViewById(R.id.checkbox_optIn);
        optIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //there's an issue while on todayCredit, user clicks back,
                //after exiting the keyboard, the focus is still on the todayCredit
                todayCredit.clearFocus();
                phone.clearFocus();
            }
        });

        confirmBtn = (Button) findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerOrUpdateCustomer();
            }
        });

        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                gotoHomeScreen();
            }
        });

        handler = new DatabaseHandler(this);

        //uncomment to see the db entries on screen
        /*listView = (ListView) findViewById(R.id.addressListView);
        list = handler.getAllAddress();
        addressAdapter = new AddressAdapter(this);
        listView.setAdapter(addressAdapter);*/

    }

    private boolean isTodayCreditValid() {
        boolean isValid = false;
        final String todayCreditStr = MainActivity.this.todayCredit.getText().toString();
        if (todayCreditStr != null && todayCreditStr.matches(AT_LEAST_ONE_DIGIT_REGEXP)){
            int todayCredit = Integer.parseInt(todayCreditStr);
            isValid = (todayCredit < TODAY_CREDIT_LIMIT);
        }

        TextInputLayout todayCreditLayout = (TextInputLayout) findViewById(R.id.todayCreditlayout);
        if (isValid){
            todayCreditLayout.setErrorEnabled(false);
        }
        else {
            todayCreditLayout.setError(getString(R.string.todayCredit_err_msg));
            //((EditText) findViewById(R.id.todayCredit)).requestFocus();
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
                Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                        Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }
    }


    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 123;

    private void requestSmsPermission() {

        // check permission is given
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // request permission (see result in onRequestPermissionsResult() method)
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        } else {
            // permission already granted run sms send
            sendSms(targetPhoneNum, confirmationMsg);
            Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    sendSms(targetPhoneNum, confirmationMsg);
                    Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
                } else {
                    // permission denied
                    Toast.makeText(getApplicationContext(), "permission denied", Toast.LENGTH_LONG).show();
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
        //go back to home screen
        Intent myIntent = new Intent(MainActivity.this, MainActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        MainActivity.this.startActivity(myIntent);
    }

    private void updateMissingCredit() {
        //done with editing
        EditText missingCreditView = (EditText) findViewById(R.id.missingCredit);
        int previousCreditValue = getPreviousCredit();

        int todayCredit = getTodayCredit();
        int totalCredit = previousCreditValue + todayCredit;
        missingCreditView = (EditText) findViewById(R.id.missingCredit);
        final int missingCredit = FREE_DRINK_THRESHOLD - totalCredit;

        if (missingCredit > 0){
            ((TextInputLayout) findViewById(R.id.missingCreditlayout)).setHintEnabled(true);
            missingCreditView.setText(String.valueOf(missingCredit));
        }
        else {
            //qualifies for free drink
            missingCreditView.setText(R.string.freeDrinkAchieved);
            ((TextInputLayout) findViewById(R.id.missingCreditlayout)).setHintEnabled(false);
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
        final String todayCreditStr = MainActivity.this.todayCredit.getText().toString();
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
                ((EditText) findViewById(R.id.receiptNumber)).requestFocus();
            }
        }
        else if (id.equals(receiptNumId)){
            isReceiptNumberValid();
        }

        return handled;
    }

    private void requestFocusOnTodayCredit() {
        //clearCurrentFocus();
        todayCredit = (EditText) findViewById(R.id.todayCredit);
        todayCredit.requestFocus();
        todayCredit.setSelection(todayCredit.getText().length());
    }

    private boolean isReceiptNumberValid(){
        boolean isValid = false;
        receiptNum = (EditText) findViewById(R.id.receiptNumber);
        String receiptNumStr = receiptNum.getText().toString();

        TextInputLayout receiptNumLayout = (TextInputLayout) findViewById(R.id.receiptLayout);
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
                //default the checkbox to checked
                ((CheckBox) findViewById(R.id.checkbox_optIn)).setChecked(true);
            }

            previousCredit = (EditText) findViewById(R.id.previousCredit);
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
        TextInputLayout phoneLayout = (TextInputLayout) findViewById(R.id.phoneLayout);
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

    private void clearCurrentFocus() {
        if (getCurrentFocus() != null){
            getCurrentFocus().clearFocus();
        }
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

                //only set the opt-in date if it's new customer, or existing customer but the value changed
                if (optIn.isChecked()){
                    customer.setOptInDate(today);
                    sendConfirmationText = true;
                }
            }
            else {
                //existing customer, check if the screen's opt-in value is different from loaded db value
                final Date emptyDate = new Date(0);
                if (customer.isOptIn() && !optIn.isChecked()){
                    //previously opted in, but now opted out from screen, set the opt-out date (we'll set the opt-in bool later)
                    customer.setOptOutDate(today);
                    customer.setOptInDate(emptyDate); //clear opt-in date
                }
                else if (optIn.isChecked() && !customer.isOptIn()){
                    //previously opted out, but now opted in from screen, set the opt-in date
                    customer.setOptInDate(today);
                    customer.setOptOutDate(emptyDate); //clear opt-out date
                    sendConfirmationText = true;
                }
            }
            customer.setOptIn(optIn.isChecked());
            customer.setTotalCredit(getPreviousCredit() + getTodayCredit());
            customer.setLastVisitDate(today);

            handler.registerOrUpdateCustomer(customer);

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
}
