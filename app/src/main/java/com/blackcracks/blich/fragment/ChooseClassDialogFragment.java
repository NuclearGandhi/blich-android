package com.blackcracks.blich.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.sync.FetchClassService;
import com.blackcracks.blich.util.ClassGroupUtils;
import com.blackcracks.blich.util.Constants.Preferences;
import com.blackcracks.blich.util.RealmUtils;
import com.blackcracks.blich.util.Utilities;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;
import io.realm.Realm;

/**
 * This DialogFragment is showed when the user launches the app for the first time to configure
 * some settings.
 * A similar dialog is shown when the user wants to change these settings:
 * {@link com.blackcracks.blich.preference.ClassPickerPreferenceDialogFragment}
 */
@SuppressWarnings("ConstantConditions")
public class ChooseClassDialogFragment extends DialogFragment {

    private static final String KEY_DATA_VALID = "data_valid";
    public static final String PREF_IS_FIRST_LAUNCH_KEY = "first_launch";

    private Realm mRealm;
    private boolean mIsDataValid = false;

    private AlertDialog mDialog;
    private MaterialNumberPicker mClassIndexPicker;
    private MaterialNumberPicker mGradePicker;
    private FrameLayout mProgressBar;
    private BroadcastReceiver mFetchBroadcastReceiver;
    private OnDestroyListener mOnDestroyListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();

        //Create a {@link BroadcastReceiver} to listen when the data has finished downloading
        mFetchBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isSuccessful =
                        intent.getBooleanExtra(FetchClassService.IS_SUCCESSFUL_EXTRA, false);
                if (isSuccessful) {
                    setDataValid();
                } else {
                    onFetchFailed();
                }
            }
        };

        if (savedInstanceState != null) {
            mIsDataValid = savedInstanceState.getBoolean(KEY_DATA_VALID);
        } else {
            syncData();
        }
    }

    //Setup the Dialog
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setCancelable(false);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        final View rootView = inflater.inflate(
                R.layout.dialog_select_class,
                null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);

        mClassIndexPicker =
                rootView.findViewById(R.id.dialog_choose_class_number_picker);
        mGradePicker =
                rootView.findViewById(R.id.dialog_choose_class_name_picker);

        builder.setPositiveButton(R.string.dialog_okay,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] displayedValues = mGradePicker.getDisplayedValues();
                        String gradeName = displayedValues[mGradePicker.getValue()];
                        int classNum = mClassIndexPicker.getValue();
                        int id;
                        if (mClassIndexPicker.getVisibility() == View.INVISIBLE) {
                            id = RealmUtils.getId(mRealm, gradeName);
                        } else {
                            id = RealmUtils.getId(mRealm, gradeName, classNum);
                        }

                        SharedPreferences sharedPreferences = PreferenceManager
                                .getDefaultSharedPreferences(getContext());

                        sharedPreferences.edit()
                                .putInt(
                                        Preferences.getKey(getContext(), Preferences.PREF_USER_CLASS_GROUP_KEY),
                                        id)
                                .apply();

                        sharedPreferences.edit()
                                .putBoolean(PREF_IS_FIRST_LAUNCH_KEY, false)
                                .apply();
                    }
                });

        mProgressBar = rootView.findViewById(R.id.picker_progressbar);
        mDialog = builder.create();
        return mDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mIsDataValid) {
            setDataValid();
        } else {
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    @Override
    public void onResume() {
        //Start the listener
        super.onResume();
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mFetchBroadcastReceiver,
                new IntentFilter(FetchClassService.ACTION_FINISHED_FETCH));
    }

    @Override
    public void onPause() {
        //Stop the listener
        super.onPause();
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(mFetchBroadcastReceiver);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_DATA_VALID, mIsDataValid);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
        if (mOnDestroyListener != null) mOnDestroyListener.onDestroy(getContext());
    }

    //If the fetching failed, show a Dialog
    private void onFetchFailed() {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fetch_failed,
                null);

        TextView message = view.findViewById(R.id.dialog_message);
        message.setText(R.string.dialog_fetch_class_no_connection_message);
        new AlertDialog.Builder(getContext())
                .setCancelable(false)
                .setView(view)
                .setPositiveButton(R.string.dialog_try_again,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                syncData();
                            }
                        })
                .show();
    }

    //Start to fetch the data
    private void syncData() {
        boolean isConnected = Utilities.isThereNetworkConnection(getContext());
        if (isConnected) {
            Intent intent = new Intent(getContext(), FetchClassService.class);
            getActivity().startService(intent);
        } else {
            onFetchFailed();
        }
    }

    private void setDataValid() {

        mIsDataValid = true;
        mProgressBar.setVisibility(View.GONE);
        mGradePicker.setVisibility(View.VISIBLE);
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

        ClassGroupUtils.loadDataIntoPicker(
                mRealm,
                mGradePicker,
                mClassIndexPicker,
                ClassGroupUtils.getClassValue(getContext()));
    }

    public void setOnDestroyListener(OnDestroyListener listener) {
        mOnDestroyListener = listener;
    }

    public interface OnDestroyListener {
        void onDestroy(Context context);
    }
}
