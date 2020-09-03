package com.example.authenticatorpoc.helpers;

import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.authenticatorpoc.constants.AUTHENTICATION_STATE;
import com.example.authenticatorpoc.constants.Consts;

import java.util.concurrent.Executor;

import static androidx.biometric.BiometricConstants.ERROR_CANCELED;
import static androidx.biometric.BiometricConstants.ERROR_NEGATIVE_BUTTON;

public class BiometricsHandler {

    private static Activity activity = null;

    private static BiometricsHandler biometricsHandlerInstance = null;

    private BiometricsHandler() {}

    // Checks state of biometrics availability on device
    public boolean checkBiometricsAvailability()
    {
        //TODO recommend use of biometrics for quick access to app features when unavailable
        BiometricManager biometricManager = BiometricManager.from(activity);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Logger.debug(Consts.LOG_TAG_AUTHENTICATION_ACTIVITY, Consts.LOG_BIOMETRICS_AVAILABLE);
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Logger.debug(Consts.LOG_TAG_AUTHENTICATION_ACTIVITY, Consts.LOG_BIOMETRICS_UNAVAILABLE);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Logger.debug(Consts.LOG_TAG_AUTHENTICATION_ACTIVITY, Consts.LOG_BIOMETRICS_UNAVAILABLE_TEMPORARILY);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Logger.debug(Consts.LOG_TAG_AUTHENTICATION_ACTIVITY, Consts.LOG_BIOMETRICS_NOT_SETUP);
                break;
        }
        return false;
    }

    // Sets up and displays the prompt to do biometrics authentication
    // By using Androidx biometrics we ensure compatibility down to Marshmallow, as well
    // as allowing the device to decide which biometrics authentication method to use.
    public void setupBiometricPrompt(final IBiometricsPromptCallback authenticationCallback, final String aTitle, final String aSubtitle)
    {
        Executor executor = ContextCompat.getMainExecutor(activity);
        BiometricPrompt biometricPrompt = new BiometricPrompt((FragmentActivity) activity,
                executor, new BiometricPrompt.AuthenticationCallback() {

            // Errors may occur among others when a user is locked out, or presses cancel
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // errorCode 5 = ERROR_CANCELED
                // - Somehow on my Samsung device the biometric dialogue immediately disappears
                // - and returns a callback with errorCode 5. If/Else is to prevent loops of dialogues
                // errorCode 13 = ERROR_NEGATIVE_BUTTON
                // - When the user presses cancel, wait a second, then show the biometric dialogue again
                // Refer to BiometricPrompt.java for more information
                if (errorCode != ERROR_CANCELED && errorCode != ERROR_NEGATIVE_BUTTON) {
                    Toast.makeText(activity,
                            Consts.TOAST_MESSAGE_AUTHENTICATION_ERROR + errString, Toast.LENGTH_SHORT)
                            .show();
                    authenticationCallback.onAuthenticationResult(AUTHENTICATION_STATE.ERROR);
                }
                else
                {
                    if (errorCode != ERROR_CANCELED) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setupBiometricPrompt(authenticationCallback, aTitle, aSubtitle);
                            }
                        }, Consts.ONE_SECOND_IN_MILLIS);
                    }
                }
            }

            // Authentication succeeds when the fingerprint matches those stored by device
            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(activity,
                        Consts.TOAST_MESSAGE_AUTHENTICATION_SUCCEEDED, Toast.LENGTH_SHORT).show();
                authenticationCallback.onAuthenticationResult(AUTHENTICATION_STATE.SUCCEEDED);
            }

            // Authentication fails when the fingerprint does not match those stored by device
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(activity, Consts.TOAST_MESSAGE_AUTHENTICATION_FAILED,
                        Toast.LENGTH_SHORT)
                        .show();
                authenticationCallback.onAuthenticationResult(AUTHENTICATION_STATE.FAILED);
            }
        });

        // Set up the visual prompt
        BiometricPrompt.PromptInfo.Builder builder = new BiometricPrompt.PromptInfo.Builder();
        builder.setTitle(aTitle);
        builder.setSubtitle(aSubtitle);
        builder.setNegativeButtonText(Consts.BUTTONTEXT_CANCEL);
        BiometricPrompt.PromptInfo promptInfo = builder.build();

        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
        Logger.debug(Consts.LOG_TAG_AUTHENTICATION_ACTIVITY, Consts.LOG_BEFORE_AUTHENTICATION);
        biometricPrompt.authenticate(promptInfo);
    }

    // A BiometricsHandler instance may be used in several activities
    public static BiometricsHandler getBiometricsHandlerInstance(Activity aForActivity)
    {
        activity = aForActivity;
        if (biometricsHandlerInstance == null)
        {
            biometricsHandlerInstance = new BiometricsHandler();
        }
        return biometricsHandlerInstance;
    }
}
