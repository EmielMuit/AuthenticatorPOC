package com.example.authenticatorpoc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.authenticatorpoc.helpers.Consts;
import com.example.authenticatorpoc.helpers.Logger;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    // UI elements
    ConstraintLayout rootElement;
    LinearLayout loginSquare;

    // Background in between animations
    private int currentBackground;

    // Colors/drawables used in UI
    private Drawable greenGradient;
    private Drawable redGradient;
    private int greenColor;
    private int redColor;
    private int resedaGreenColor;
    private int maroonColor;
    private int eggShellWhiteColor;

    // Checks state of biometrics availability on device
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

    // Puts colors defined in xml files in fields
    private void setUIColors() {
        greenGradient = getResources().getDrawable(R.drawable.green_gradient);
        redGradient = getResources().getDrawable(R.drawable.red_gradient);
        greenColor = getResources().getColor(R.color.colorGreen);
        redColor = getResources().getColor(R.color.colorRed);
        resedaGreenColor = getResources().getColor(R.color.colorResedaGreen);
        maroonColor = getResources().getColor(R.color.colorMaroon);
        eggShellWhiteColor = getResources().getColor(R.color.colorWhiteEggshell);

        currentBackground = ((ColorDrawable) rootElement.getBackground()).getColor();
    }

    // Puts the UI elements used in code in fields
    private void setUIElements() {
        rootElement = findViewById(R.id.rootElement);
        loginSquare = findViewById(R.id.loginSquare);
    }

    // Sets up the prompt to do biometrics authentication
    private void setupBiometricPrompt()
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
                animateLogin(Consts.AUTHENTICATION_STATE.ERROR);
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(),
                        Consts.MESSAGE_AUTHENTICATION_SUCCEEDED, Toast.LENGTH_SHORT).show();
                animateLogin(Consts.AUTHENTICATION_STATE.SUCCEEDED);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), Consts.MESSAGE_AUTHENTICATION_FAILED,
                        Toast.LENGTH_SHORT)
                        .show();
                animateLogin(Consts.AUTHENTICATION_STATE.FAILED);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUIElements();

        setUIColors();

        checkBiometricsAvailability();

        // On initialization error state is sent to display neutral background, with red square
        // Reason: In XML we cannot set square color based on Android version
        setupLayoutOnAuthentication(Consts.AUTHENTICATION_STATE.ERROR);

        setupBiometricPrompt();
    }

    private void animateLogin(final Consts.AUTHENTICATION_STATE aSuccess)
    {
        // Square disappears by rotating Y axis 90 degrees and color changes on animation end
        ObjectAnimator hideAnimation = ObjectAnimator.ofFloat(loginSquare, Consts.Y_ROTATION_PROPERTYNAME, 0.0f, 90f);
        hideAnimation.setDuration(Consts.ANIMATION_LENGTH_MS);
        hideAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                //TODO restart button?

                // Sets the colors based on authentication state
                setupLayoutOnAuthentication(aSuccess);

                // Square appears with updated colors
                ObjectAnimator revealAnimation = ObjectAnimator.ofFloat(loginSquare, Consts.Y_ROTATION_PROPERTYNAME, 90f, 180f);
                revealAnimation.setDuration(Consts.ANIMATION_LENGTH_MS);
                revealAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                revealAnimation.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
        hideAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        hideAnimation.start();
    }

    // Updates the UI colors based on the authentication state
    // Square will use gradients when supported by Android version
    private void setupLayoutOnAuthentication(Consts.AUTHENTICATION_STATE aState)
    {
        // Colors used in background color transition
        ColorDrawable[] colorTransition = new ColorDrawable[2];
        // Transition starts with current background color
        colorTransition[0] = new ColorDrawable(currentBackground);

        switch (aState) {
            case SUCCEEDED:
            {
                colorTransition[1] = new ColorDrawable(resedaGreenColor);
                if (android.os.Build.VERSION.SDK_INT >= 16) {
                    loginSquare.setBackground(greenGradient);
                } else {
                    loginSquare.setBackgroundColor(greenColor);
                }
                break;
            }
            case FAILED:
            {
                colorTransition[1] = new ColorDrawable(maroonColor);
                if (android.os.Build.VERSION.SDK_INT >= 16) {
                    loginSquare.setBackground(redGradient);
                } else {
                    loginSquare.setBackgroundColor(redColor);
                }
                break;
            }
            case ERROR:
            {
                colorTransition[1] = new ColorDrawable(eggShellWhiteColor);
                if (android.os.Build.VERSION.SDK_INT >= 16) {
                    loginSquare.setBackground(redGradient);
                } else {
                    loginSquare.setBackgroundColor(redColor);
                }
                break;
            }
        }

        // transitions background color to color set in switch above
        TransitionDrawable trans = new TransitionDrawable(colorTransition);
        // currentBackground becomes color transitioned to above
        currentBackground = colorTransition[1].getColor();

        //This will work also on old devices. The latest API says you have to use setBackground instead.
        rootElement.setBackgroundDrawable(trans);
        trans.startTransition(Consts.ANIMATION_LENGTH_MS);
    }
}