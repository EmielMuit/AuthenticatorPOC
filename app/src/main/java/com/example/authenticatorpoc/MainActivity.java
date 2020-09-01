package com.example.authenticatorpoc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.authenticatorpoc.helpers.Consts;
import com.example.authenticatorpoc.helpers.Logger;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private void animateLogin(final boolean aSuccess)
    {
        final LinearLayout loginSquare = findViewById(R.id.loginSquare);

        ObjectAnimator hideAnimation = ObjectAnimator.ofFloat(loginSquare, Consts.Y_ROTATION_PROPERTYNAME, 0.0f, 90f);
        hideAnimation.setDuration(1200);
        hideAnimation.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //TODO restart button?
                setupLayout(aSuccess);
                ObjectAnimator revealAnimation = ObjectAnimator.ofFloat(loginSquare, Consts.Y_ROTATION_PROPERTYNAME, 90f, 180f);
                revealAnimation.setDuration(1200);
                revealAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                revealAnimation.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        hideAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        hideAnimation.start();
    }

    private void checkBiometricsAvailability()
    {
        //TODO recommend use of biometrics for quick access to app features when unavailable
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Logger.debug(Consts.TAG_AUTHENTICATION_ACTIVITY, Consts.LOG_BIOMETRICS_AVAILABLE);
                break;
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkBiometricsAvailability();
        setupLayout(false);

        setupBiometricPrompt();
    }

    public void setupBiometricPrompt()
    {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        Consts.MESSAGE_AUTHENTICATION_ERROR + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(),
                        Consts.MESSAGE_AUTHENTICATION_SUCCEEDED, Toast.LENGTH_SHORT).show();
                animateLogin(true);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), Consts.MESSAGE_AUTHENTICATION_FAILED,
                        Toast.LENGTH_SHORT)
                        .show();
                animateLogin(false);
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(Consts.PROMPT_TITLE)
                .setSubtitle(Consts.PROMPT_SUBTITLE)
                .setNegativeButtonText(" ") //TODO use negative button?
                .build();

        // Prompt appears on startup.
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
        Logger.debug(Consts.TAG_AUTHENTICATION_ACTIVITY, Consts.LOG_BEFORE_AUTHENTICATION);
        biometricPrompt.authenticate(promptInfo);
    }

    private void setupLayout(boolean aAuthenticated)
    {
        LinearLayout loginSquare = findViewById(R.id.loginSquare);
        if (aAuthenticated)
        {
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                loginSquare.setBackground(getResources().getDrawable(R.drawable.green_gradient));
            }
            else
            {
                loginSquare.setBackgroundColor(getResources().getColor(R.color.colorGreen));
            }
        }
        else
        {
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                loginSquare.setBackground(getResources().getDrawable(R.drawable.red_gradient));
            }
            else
            {
                loginSquare.setBackgroundColor(getResources().getColor(R.color.colorRed));
            }
        }
    }
}