package com.example.authenticatorpoc.activity.implementation;

import android.os.Bundle;
import android.os.Handler;

import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.authenticatorpoc.R;
import com.example.authenticatorpoc.activity.IAuthenticationControllerListener;
import com.example.authenticatorpoc.constants.Consts;
import com.example.authenticatorpoc.controller.AuthenticationController;
import com.example.authenticatorpoc.helper.BiometricsHandler;
import com.example.authenticatorpoc.view.AuthenticationView;

public class MainActivity extends AppCompatActivity implements IAuthenticationControllerListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sets up the authentication view
        AuthenticationView authenticationView = this.findViewById(R.id.AuthenticationView);
        // Pass in regularly referenced UI elements, getting in constructor will not work
        authenticationView.setUIElements(authenticationView, (LinearLayout) this.findViewById(R.id.loginSquare), (Button)this.findViewById(R.id.btReset));

        // Set up authentication controller
        AuthenticationController authenticationController = new AuthenticationController(authenticationView, this);
        // Connect controller to resets sent from view
        authenticationView.setResetListener(authenticationController);
        // Add authenticationController as listener for biometrics authentication
        BiometricsHandler.addAuthenticationResultListener(authenticationController);

        // Prints debug logging about biometrics, may be used to request setting up biometrics
        BiometricsHandler.checkBiometricsAvailability(this);
        showBiometricsPrompt();
    }

    @Override
    public void onAuthenticationSuccess() {
        // Go to next activity?
    }

    @Override
    public void onAuthenticationFailure() {
        // Open help?
    }

    @Override
    public void onAuthenticationError() {
        // Show error message?
    }

    @Override
    public void onResetApp() {
        showBiometricsPrompt();
    }

    // Sets up a biometrics prompt for current activity
    public void showBiometricsPrompt() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BiometricsHandler.setupBiometricPrompt(Consts.BIOMETRICS_PROMPT_TITLE, Consts.BIOMETRICS_PROMPT_SUBTITLE, MainActivity.this);
            }
        }, Consts.ONE_SECOND_IN_MILLIS);
    }
}