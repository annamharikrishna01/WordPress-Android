
package org.wordpress.android.ui.accounts;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.wordpress.rest.RestRequest;

import org.json.JSONException;
import org.json.JSONObject;

import org.wordpress.android.Config;
import org.wordpress.android.Constants;
import org.wordpress.android.R;
import org.wordpress.android.WordPress;
import org.wordpress.android.util.AlertUtil;
import org.wordpress.android.widgets.WPTextView;

public class NewUserPageFragment extends NewAccountAbstractPageFragment {

    private EditText emailTextField;
    private EditText passwordTextField;
    private EditText usernameTextField;

    public NewUserPageFragment() {
    }
    
    private boolean checkUserData() {
        // try to create the user
        final String email = emailTextField.getText().toString().trim();
        final String password = passwordTextField.getText().toString().trim();
        final String username = usernameTextField.getText().toString().trim();
        
        if (email.equals("") || password.equals("")) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            NUXDialogFragment nuxAlert = NUXDialogFragment
                    .newInstance(getString(R.string.required_fields), getString(R.string.username_password_required), getString(R.string.nux_tap_continue), R.drawable.nux_icon_alert);
            nuxAlert.show(ft, "alert");
            return false;
        }

        if (password.length() < 4) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            NUXDialogFragment nuxAlert = NUXDialogFragment
                    .newInstance(getString(R.string.invalid_password_title), getString(R.string.invalid_password_message), getString(R.string.nux_tap_continue), R.drawable.nux_icon_alert);
            nuxAlert.show(ft, "alert");
            return false;
        }
        
        if (username.length() > 60) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            NUXDialogFragment nuxAlert = NUXDialogFragment
                    .newInstance(getString(R.string.invalid_username_title), getString(R.string.invalid_username_length), getString(R.string.nux_tap_continue), R.drawable.nux_icon_alert);
            nuxAlert.show(ft, "alert");
            return false;
        }

        final String emailRegEx = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
        final Pattern emailRegExPattern = Pattern.compile(emailRegEx,
                Pattern.DOTALL);
        Matcher matcher = emailRegExPattern.matcher(email);
        if (!matcher.find() || email.length() > 100) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            NUXDialogFragment nuxAlert = NUXDialogFragment
                    .newInstance(getString(R.string.invalid_email_title), getString(R.string.invalid_email_message), getString(R.string.nux_tap_continue), R.drawable.nux_icon_alert);
            nuxAlert.show(ft, "alert");
            return false;
        }

        return true;
    }

    OnClickListener signupClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
        
            //TODO: The following lines ensure that no .com account are available in the app - change this!!!!
            WordPress.signOut(getActivity());
            
            //reset the data
            NewAccountActivity act = (NewAccountActivity)getActivity();
            act.validatedEmail = null;
            act.validatedPassword = null;
            act.validatedUsername = null;
            
            if (mSystemService.getActiveNetworkInfo() == null) {
                AlertUtil.showAlert(getActivity(), R.string.no_network_title, R.string.no_network_message);
                return;
            }
            
            // try to create the user
            final String email = emailTextField.getText().toString().trim();
            final String password = passwordTextField.getText().toString().trim();
            final String username = usernameTextField.getText().toString().trim();

            if (false == checkUserData())
                return;

            pd = ProgressDialog.show(NewUserPageFragment.this
                    .getActivity(),
                    getString(R.string.account_setup),
                    getString(R.string.validating_user_data), true, false);

            String path = "users/new";
            Map<String, String> params = new HashMap<String, String>();
            params.put("username", username);
            params.put("password", password);
            params.put("email", email);
            params.put("validate", "1");
            params.put("client_id", Config.OAUTH_APP_ID);
            params.put("client_secret", Config.OAUTH_APP_SECRET);

            restClient.post(path, params, null,
                    new RestRequest.Listener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (pd != null)
                                pd.dismiss();
                            Log.d("1. New User PAGE", String.format("OK %s", response.toString()));
                            try {
                                if(response.getBoolean("success")) {
                                    NewAccountActivity act = (NewAccountActivity)getActivity();
                                    act.validatedEmail = email;
                                    act.validatedPassword = password;
                                    act.validatedUsername = username;
                                    act.showNextItem();
                                } else {
                                    showError(getString(R.string.error_generic));
                                }
                            } catch (JSONException e) {
                                showError(getString(R.string.error_generic));
                            }
                        }
                    },
                    new ErrorListener()
                    );
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.new_account_user_fragment_screen, container, false);
        
        rootView.findViewById(R.id.l_agree_terms_of_service).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.parse(Constants.URL_TOS);
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    }
                }
                );
        
        WPTextView signupButton = (WPTextView) rootView.findViewById(R.id.signup_button);
        signupButton.setOnClickListener(signupClickListener);

        emailTextField = (EditText) rootView.findViewById(R.id.email_address);
        passwordTextField = (EditText) rootView.findViewById(R.id.password);
        usernameTextField = (EditText) rootView.findViewById(R.id.username);

        return rootView;
    }
}