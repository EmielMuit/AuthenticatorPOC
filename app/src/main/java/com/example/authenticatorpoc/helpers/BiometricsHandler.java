package com.example.authenticatorpoc.helpers;

import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

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
                Logger.debug(Consts.TAG_AUTHENTICATION_ACTIVITY, Consts.LOG_BIOMETRICS_AVAILABLE);
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Logger.debug(Consts.TAG_AUTHENTICATION_ACTIVITY, Consts.LOG_BIOMETRICS_UNAVAILABLE);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Logger.debug(Consts.TAG_AUTHENTICATION_ACTIVITY, Consts.LOG_BIOMETRICS_UNAVAILABLE_TEMPORARILY);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Logger.debug(Consts.TAG_AUTHENTICATION_ACTIVITY, Consts.LOG_BIOMETRICS_NOT_SETUP);
                break;
        }
        return false;
    }

    // Sets up and displays the prompt to do biometrics authentication
    // By using Androidx biometrics we ensure compatibility down to Marshmellow, as well
    // as allowing the device to decide which biometrics authentication method to use.
    public void setupBiometricPrompt(final IBiometricsPromptCallback authenticationCallback)
    {
        Executor executor = ContextCompat.getMainExecutor(activity);
        BiometricPrompt biometricPrompt = new BiometricPrompt((FragmentActivity) activity,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // errorCode 5 means operation was 'canceled'
                // - Somehow on my Samsung device the biometric dialogue immediately disappears
                // - and returns a callback with errorCode 5. If/Else is to prevent loops of dialogues
                // errorCode 13 means negative button was pressed
                // - When the user presses cancel, wait a second, then show the biometric dialogue again
                if (errorCode != 5 && errorCode != 13) {
                    Toast.makeText(activity,
                            Consts.MESSAGE_AUTHENTICATION_ERROR + errString, Toast.LENGTH_SHORT)
                            .show();
                    authenticationCallback.handleAuthenticationResult(Consts.AUTHENTICATION_STATE.ERROR);
                }
                else
                {
                    if (errorCode != 5) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setupBiometricPrompt(authenticationCallback);
                            }
                        }, 1000);
                    }
                }
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(activity,
                        Consts.MESSAGE_AUTHENTICATION_SUCCEEDED, Toast.LENGTH_SHORT).show();
                authenticationCallback.handleAuthenticationResult(Consts.AUTHENTICATION_STATE.SUCCEEDED);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(activity, Consts.MESSAGE_AUTHENTICATION_FAILED,
                        Toast.LENGTH_SHORT)
                        .show();
                authenticationCallback.handleAuthenticationResult(Consts.AUTHENTICATION_STATE.FAILED);
            }
        });

        // Set up the visual prompt
        BiometricPrompt.PromptInfo.Builder builder = new BiometricPrompt.PromptInfo.Builder();
        builder.setTitle(Consts.PROMPT_TITLE);
        builder.setSubtitle(Consts.PROMPT_SUBTITLE);
        builder.setNegativeButtonText("Cancel");
        BiometricPrompt.PromptInfo promptInfo = builder.build();

        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
        Logger.debug(Consts.TAG_AUTHENTICATION_ACTIVITY, Consts.LOG_BEFORE_AUTHENTICATION);
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
