package com.lukas.android.booklog;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.preference.Preference.OnPreferenceClickListener;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

//settings let user pich what status is shown and in what order the bookshelf is sorted
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public static class BookPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        private String mUsername;
        private FirebaseAuth mFirebaseAuth;
        private FirebaseAuth.AuthStateListener mAuthStateListener;
        private int preventLogInTwise;
        public static final int RC_SIGN_IN = 1;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            mUsername = "ANONYMOUS";
            mFirebaseAuth = FirebaseAuth.getInstance();

            preventLogInTwise = 0;


            //bind key to preference
            Preference showStatus = findPreference(getString(R.string.settings_show_status_key));
            bindPreferenceSummaryToValue(showStatus);

            Preference orderBy = findPreference(getString(R.string.settings_order_by_key));
            bindPreferenceSummaryToValue(orderBy);

            final Preference authUser = findPreference(getString(R.string.settings_auth_user_key));
            authUser.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    bindAuthPref(preference);
                    return false;
                }
            });

            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        // User is signed in
                        authUser.setSummary(getString(R.string.auth_true_label)+user.getDisplayName());
                    } else {
                        // User is signed out
                        authUser.setSummary(getString(R.string.auth_false_label));
                    }
                }
            };
        }

        @Override
        public void onPause(){
        super.onPause();
            if (mAuthStateListener != null) {
                mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
            }
        }
        @Override
        public void onResume(){
            super.onResume();
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if(value instanceof String){
                String stringValue = value.toString();
                if (preference instanceof ListPreference) {
                    ListPreference listPreference = (ListPreference) preference;
                    int prefIndex = listPreference.findIndexOfValue(stringValue);
                    if (prefIndex >= 0) {
                        CharSequence[] labels = listPreference.getEntries();
                        preference.setSummary(labels[prefIndex]);
                    }
                } else {
                    preference.setSummary(stringValue);
                }
            }else if(value instanceof Boolean){
                //if the prefferance is an boolean, handle authentication

                preventLogInTwise ++;
                if(preventLogInTwise%2==1){
                    if(value.toString().equals("true")){
                        //logging in
                        startActivityForResult(
                                AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setIsSmartLockEnabled(false)
                                        .setAvailableProviders(Arrays.asList(
                                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                                new AuthUI.IdpConfig.GoogleBuilder().build()
                                        ))
                                        .build(),
                                RC_SIGN_IN);

                    }else{
                        //logging out
                        AuthUI.getInstance().signOut(getActivity());
                    }
                }
            }

            return true;
        }

        private void bindAuthPref(Preference preference){
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            boolean preferencesBoolean = preferences.getBoolean(preference.getKey(), false);
            onPreferenceChange(preference, preferencesBoolean);
        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }
    }
}
