package com.kpblog.tt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.util.Constants;
import com.kpblog.tt.util.Util;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_Admin.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_Admin#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Admin extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    boolean permissionRequestedOnStart = false;
    EditText adminCode;
    Button getCodeBtn, lockUnlockBtn, exportBtn, importBtn;
    DatabaseHandler handler;
    private OnFragmentInteractionListener mListener;

    public Fragment_Admin() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_Admin.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Admin newInstance(String param1, String param2) {
        Fragment_Admin fragment = new Fragment_Admin();
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
        return inflater.inflate(R.layout.fragment__admin, container, false);
    }


    long getCodeBtnLastClicked, lockUnlockBtnLastClicked, exportBtnLastClicked, importBtnLastClicked = 0;
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        handler = new DatabaseHandler(getContext());
        adminCode = (EditText) (getView().findViewById(R.id.adminCode));

        getCodeBtn = (Button) (getView().findViewById(R.id.getCodeBtn));
        getCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - getCodeBtnLastClicked > Constants.BUTTON_CLICK_ELAPSE_THRESHOLD){
                    sendCode();
                    getCodeBtnLastClicked = SystemClock.elapsedRealtime();
                }
            }
        });

        lockUnlockBtn = (Button) (getView().findViewById(R.id.lockUnlockBtn));
        lockUnlockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - lockUnlockBtnLastClicked > Constants.BUTTON_CLICK_ELAPSE_THRESHOLD){
                    lockUnlockAdminScreen();
                    lockUnlockBtnLastClicked = SystemClock.elapsedRealtime();
                }
            }
        });

        if (!permissionRequestedOnStart){
            requestReadWritePermission_onStart();
        }

        exportBtn = (Button) (getView().findViewById(R.id.exportBtn));
        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - exportBtnLastClicked > Constants.BUTTON_CLICK_ELAPSE_THRESHOLD){
                    String state = Environment.getExternalStorageState();
                    //external storage availability check
                    if (!Environment.MEDIA_MOUNTED.equals(state)) {
                        return;
                    }

                    requestReadWritePermission();
                    exportBtnLastClicked = SystemClock.elapsedRealtime();
                }
            }
        });

        importBtn = (Button) (getView().findViewById(R.id.importBtn));
        importBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - importBtnLastClicked > Constants.BUTTON_CLICK_ELAPSE_THRESHOLD){
                    importBtnLastClicked = SystemClock.elapsedRealtime();

                    final File documentPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                    File exportedFolder = new File (documentPath, Constants.EXPORTED_FOLDER_NAME);

                    final String deviceDbPath = getContext().getDatabasePath(DatabaseHandler.DATABASE_NAME).getPath();
                    final String latestExportedDb = getLatestExportedDb(exportedFolder);

                    boolean importSuccess = false;
                    if (!latestExportedDb.isEmpty()){
                        importSuccess = handler.importDatabase(latestExportedDb, deviceDbPath);
                    }

                    if (importSuccess){
                        String successMsgFormat = getString(R.string.dbImportSuccess);
                        @SuppressLint({"StringFormatInvalid", "LocalSuppress"}) String msg = String.format(successMsgFormat, getLatestExportFileName(latestExportedDb));
                        displayToast(msg);
                    }
                    else {
                        String failureMsgFormat = getString(R.string.dbImportFailed);
                        @SuppressLint({"StringFormatInvalid", "LocalSuppress"}) String msg = String.format(failureMsgFormat, getLatestExportFileName(latestExportedDb));
                        displayToast(msg);
                    }
                }
            }
        });
    }

    private String getLatestExportedFileName() {
        final File documentPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File exportedFolder = new File (documentPath, Constants.EXPORTED_FOLDER_NAME);
        String fullPath = getLatestExportedDb(exportedFolder);
        return getLatestExportPathDisplayName(fullPath);
    }

    @NonNull
    //Document/exportDb/fileName
    private String getLatestExportPathDisplayName(String fullPath) {
        if (fullPath.isEmpty()){
            return "";
        }

        String parts[] = fullPath.split(File.separator);

        int length = parts.length;
        return parts[length - 3] + File.separator + parts[length - 2] + File.separator + parts[length - 1];
    }

    //just the file name
    private String getLatestExportFileName(String fullPath){
        String parts[] = fullPath.split(File.separator);
        return parts[parts.length - 1];
    }

    /**
     * pick the latest file in this folder
     * @param exportedFolder
     * @return
     */
    private String getLatestExportedDb(File exportedFolder) {
        String latestFilePath = "";
        File files[] = exportedFolder.listFiles();

        if (files != null && files.length > 0){
            File latestFile = files[0];

            for (int i = 1; i < files.length; ++i){
                if (files[i].lastModified() > latestFile.lastModified()){
                    latestFile = files[i];
                }
            }

            latestFilePath = latestFile.getAbsolutePath();
        }

        return latestFilePath;
    }

    private static final int REQUEST_CODE_EXTERNAL_STORAGE = 234;


    private final int REQUEST_CODE_EXTERNAL_STORAGE_ONSTART = 456;
    private void requestReadWritePermission_onStart() {
        permissionRequestedOnStart = true;

        int writePermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

        // check permission is given
        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // request permission (see result in onRequestPermissionsResult() method)
            requestPermissions(Constants.PERMISSIONS_STORAGE_SMS, REQUEST_CODE_EXTERNAL_STORAGE_ONSTART);
        } else {
            updateLocationWithLatestFile();
        }
    }

    private void requestReadWritePermission() {

        int writePermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

        // check permission is given
        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // request permission (see result in onRequestPermissionsResult() method)
            requestPermissions(Constants.PERMISSIONS_STORAGE_SMS, REQUEST_CODE_EXTERNAL_STORAGE);
        } else {
            if (exportDatabase()){
                displayToast(getString(R.string.dbExportSuccess));
            }
            else {
                displayToast(getString(R.string.dbExportFail));
            }
        }
    }

    private void displayToast(String msg) {
        Toast.makeText(getContext().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    //http://www.zoftino.com/saving-files-to-internal-storage-&-external-storage-in-android
    private boolean exportDatabase() {

        final String sourceDbName = getContext().getDatabasePath(DatabaseHandler.DATABASE_NAME).getPath();

        String exportedDbPath = Util.exportDatabase(sourceDbName);

        EditText dbExportedLocation = (EditText) (getView().findViewById(R.id.locationInput));
        dbExportedLocation.setText(exportedDbPath);

        return !exportedDbPath.isEmpty();
    }

    private void lockUnlockAdminScreen() {
        final String buttonAction = getString(R.string.unlock);
        if (lockUnlockBtn.getText().toString().equals(buttonAction)){
            unlockScreen();
        }
        else {
            lockScreen();
        }
    }

    private void lockScreen() {
        lockUnlockBtn.setText(getString(R.string.unlock));
        getCodeBtn.setEnabled(true);
        adminCode.setEnabled(true);

        getView().findViewById(R.id.adminLayout).setVisibility(View.INVISIBLE);
    }

    private void unlockScreen() {
        String inputCode = adminCode.getText().toString();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String expectedCode = sp.getString(Constants.SHARED_PREF_ADMIN_CODE_KEY, null); // Second parameter is the default value.

        TextInputLayout adminCodeLayout = (TextInputLayout) (getView().findViewById(R.id.adminCodeLayout));

        if (!inputCode.isEmpty() && inputCode.equals(expectedCode)){
            adminCodeLayout.setErrorEnabled(false);

            //clear sharedpref code
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(Constants.SHARED_PREF_ADMIN_CODE_KEY);
            editor.apply();

            //change unlock button text to lock
            lockUnlockBtn.setText(getString(R.string.lock));
            getCodeBtn.setEnabled(false);
            adminCode.setText("");
            adminCode.setEnabled(false);

            //open admin screen
            getView().findViewById(R.id.adminLayout).setVisibility(View.VISIBLE);
            updateLocationWithLatestFile();
        }
        else {
            adminCodeLayout.setError(getString(R.string.claimCode_err_msg));
        }
    }

    private void sendCode() {
        String code = Util.generateRandom4DigitCode();
        String msg = String.format(getString(R.string.adminCodeTextMsg), code);
        requestPermissionAndSendText(Constants.ADMINS, msg);

        //save into shared pref
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.SHARED_PREF_ADMIN_CODE_KEY, code);
        editor.commit();
    }

    String textMsg;
    String[] targetPhoneNums;
    private void requestPermissionAndSendText(String[] phoneNums, String msg) {
        try {
            textMsg = msg;
            targetPhoneNums = phoneNums;
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
            sendSms(targetPhoneNums, textMsg);
            Toast.makeText(getContext().getApplicationContext(), getString(R.string.adminCodeSentToastMsg), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    sendSms(targetPhoneNums, textMsg);
                    Toast.makeText(getContext().getApplicationContext(), getString(R.string.adminCodeSentToastMsg), Toast.LENGTH_LONG).show();
                } else {
                    // permission denied
                    Toast.makeText(getContext().getApplicationContext(), "permission denied", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case REQUEST_CODE_EXTERNAL_STORAGE: {
                if (exportDatabase()){
                    displayToast(getString(R.string.dbExportSuccess));
                }
                else {
                    displayToast(getString(R.string.dbExportFail));
                }
                break;
            }

            case REQUEST_CODE_EXTERNAL_STORAGE_ONSTART: {
                updateLocationWithLatestFile();
                break;
            }
        }
    }

    private void updateLocationWithLatestFile() {
        String latestFileName = getLatestExportedFileName();
        ((EditText) (getView().findViewById(R.id.locationInput))).setText(latestFileName);
    }

    private void sendSms(String[] phoneNumbers, String message){
        SmsManager sms = SmsManager.getDefault();
        for (String phoneNum : phoneNumbers){
            sms.sendTextMessage(phoneNum, null, message, null, null);
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
