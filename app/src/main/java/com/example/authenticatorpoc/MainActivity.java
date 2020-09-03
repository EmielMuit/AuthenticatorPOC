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
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.authenticatorpoc.helpers.Consts;
import com.example.authenticatorpoc.helpers.Logger;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    // UI elements, prevent repeated calls to find by id
    ConstraintLayout rootElement;
    LinearLayout loginSquare;
    Button btReset;

    // UI values
    boolean squareFlipped = false;
    Consts.AUTHENTICATION_STATE previousAuthenticationResult;

    // current background color
    private int currentBackground;

    // Colors/drawables used in UI
    private Drawable greenGradient;
    private Drawable redGradient;
    private int resedaGreenColor;
    private int maroonColor;
    private int eggShellWhiteColor;

    // Checks state of biometrics availability on device
    private boolean checkBiometricsAvailability()
    {
        //TODO recommend use of biometrics for quick access to app features when unavailable
        BiometricManager biometricManager = BiometricManager.from(this);
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

    // Puts colors defined in xml files in fields
    private void setUIColors() {
        greenGradient = getResources().getDrawable(R.drawable.green_gradient);
        redGradient = getResources().getDrawable(R.drawable.red_gradient);
        resedaGreenColor = getResources().getColor(R.color.colorResedaGreen);
        maroonColor = getResources().getColor(R.color.colorMaroon);
        eggShellWhiteColor = getResources().getColor(R.color.colorWhiteEggshell);

        currentBackground = ((ColorDrawable) rootElement.getBackground()).getColor();
    }

    // Puts the UI elements used in code in fields
    private void setUIElements() {
        rootElement = findViewById(R.id.rootElement);
        loginSquare = findViewById(R.id.loginSquare);
        btReset = (Button) findViewById(R.id.btReset);
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
                // errorCode 5 means operation was 'canceled'
                // - Somehow on my Samsung device the biometric dialogue immediately disappears
                // - and returns a callback with errorCode 5. If/Else is to prevent loops of dialogues
                // errorCode 13 means negative button was pressed
                // - When the user presses cancel, wait a second, then show the biometric dialogue again
                if (errorCode != 5 && errorCode != 13) {
                    Toast.makeText(getApplicationContext(),
                            Consts.MESSAGE_AUTHENTICATION_ERROR + errString, Toast.LENGTH_SHORT)
                            .show();
                    animateLogin(Consts.AUTHENTICATION_STATE.ERROR);
                }
                else
                {
                    if (errorCode != 5) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setupBiometricPrompt();
                            }
                        }, 1000);
                    }
                }
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

        BiometricPrompt.PromptInfo.Builder builder = new BiometricPrompt.PromptInfo.Builder();
        builder.setTitle(Consts.PROMPT_TITLE);
        builder.setSubtitle(Consts.PROMPT_SUBTITLE);
        builder.setNegativeButtonText("Cancel");
        BiometricPrompt.PromptInfo promptInfo = builder.build();

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

        // On initialization reset state is sent to display neutral background, with red square
        // This also (re)triggers biometric prompts
        setupLayoutOnAuthentication(Consts.AUTHENTICATION_STATE.RESET);
    }

    private void animateLogin(final Consts.AUTHENTICATION_STATE aSuccess)
    {
        // We need to know if the square has been flipped, otherwise the gradient will suddenly
        // be mirrored on screen after the flip animation
        final float currentAngle = squareFlipped ?180f:0.0f;
        squareFlipped = !squareFlipped;

        // Square disappears by rotating Y axis 90 degrees and color changes on animation end
        ObjectAnimator hideAnimation = ObjectAnimator.ofFloat(loginSquare, Consts.Y_ROTATION_PROPERTYNAME, currentAngle+0.0f, currentAngle+90f);
        hideAnimation.setDuration(Consts.ANIMATION_LENGTH_MS);
        hideAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                // Sets the colors based on authentication state
                setupLayoutOnAuthentication(aSuccess);

                // Square appears with updated colors
                ObjectAnimator revealAnimation = ObjectAnimator.ofFloat(loginSquare, Consts.Y_ROTATION_PROPERTYNAME, currentAngle+90f, currentAngle+180f);
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
                loginSquare.setBackground(greenGradient);
                setResetButtonVisibility(true);
                break;
            }
            case FAILED:
            {
                colorTransition[1] = new ColorDrawable(maroonColor);
                loginSquare.setBackground(redGradient);
                break;
            }
            case ERROR:
            {
                colorTransition[1] = new ColorDrawable(eggShellWhiteColor);
                loginSquare.setBackground(redGradient);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setResetButtonVisibility(true);
                    }
                }, 30000);
                break;
            }
            case RESET:
            {
                colorTransition[1] = new ColorDrawable(eggShellWhiteColor);
                loginSquare.setBackground(redGradient);
                Logger.debug(Consts.TAG_AUTHENTICATION_ACTIVITY, "Biometrics available? " + String.valueOf(checkBiometricsAvailability()));

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setupBiometricPrompt();
                    }
                }, 1000);
                break;
            }
        }

        // transitions background color to color set in switch above
        TransitionDrawable trans = new TransitionDrawable(colorTransition);
        // currentBackground becomes color transitioned to above
        currentBackground = colorTransition[1].getColor();

        rootElement.setBackgroundDrawable(trans);
        trans.startTransition(Consts.ANIMATION_LENGTH_MS);

        previousAuthenticationResult = aState;
    }

    public void Reset(View v)
    {
        setResetButtonVisibility(false);
        animateLogin(Consts.AUTHENTICATION_STATE.RESET);
    }

    public void setResetButtonVisibility(boolean aVisible)
    {
        if (aVisible)
        {
            btReset.setAlpha(0.0f);
            btReset.setVisibility(View.VISIBLE);
            btReset.animate().alpha(1f);
        }
        else
        {
            btReset.setAlpha(1.0f);
            btReset.animate().alpha(0.0f).withEndAction(new Runnable() {
                @Override
                public void run() {
                    btReset.setVisibility(View.INVISIBLE);
                }
            });
        }
    }
}