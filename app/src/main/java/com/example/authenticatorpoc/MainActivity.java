package com.example.authenticatorpoc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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

import com.example.authenticatorpoc.constants.AUTHENTICATION_STATE;
import com.example.authenticatorpoc.helpers.BiometricsHandler;
import com.example.authenticatorpoc.constants.Consts;
import com.example.authenticatorpoc.helpers.IBiometricsPromptCallback;

public class MainActivity extends AppCompatActivity implements IBiometricsPromptCallback {

    // UI elements, prevent repeated calls to find by id
    ConstraintLayout rootElement;
    LinearLayout loginSquare;
    Button btReset;

    // UI values
    boolean squareFlipped = false;
    AUTHENTICATION_STATE previousAuthenticationResult;

    // current background color
    private int currentBackground;

    // Colors/drawables used in UI
    private Drawable greenGradient;
    private Drawable redGradient;
    private int resedaGreenColor;
    private int maroonColor;
    private int eggShellWhiteColor;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUIElements();

        setUIColors();

        // Prints debug logging about biometrics, may be used to request setting up biometrics
        BiometricsHandler.getBiometricsHandlerInstance(this).checkBiometricsAvailability();

        showBiometricsPrompt();
    }

    private void animateLogin(final AUTHENTICATION_STATE aSuccess)
    {
        // We need to know if the square has been flipped, otherwise the gradient will suddenly
        // be mirrored on screen after the flip animation
        final float currentAngle = squareFlipped ?180f:0.0f;
        squareFlipped = !squareFlipped;

        // Square disappears by rotating Y axis 90 degrees and color changes on animation end
        ObjectAnimator hideAnimation = ObjectAnimator.ofFloat(loginSquare, Consts.PROPERTY_Y_ROTATION, currentAngle+0.0f, currentAngle+90f);
        hideAnimation.setDuration(Consts.ANIMATION_LENGTH_MS);
        hideAnimation.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animator) {} // No action on start
            @Override
            public void onAnimationEnd(Animator animator) {
                // Sets the colors based on authentication state
                setupLayoutOnAuthentication(aSuccess);

                // Square appears with updated colors
                ObjectAnimator revealAnimation = ObjectAnimator.ofFloat(loginSquare, Consts.PROPERTY_Y_ROTATION, currentAngle+90f, currentAngle+180f);
                revealAnimation.setDuration(Consts.ANIMATION_LENGTH_MS);
                revealAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                revealAnimation.start();
            }
            @Override public void onAnimationCancel(Animator animator) {} // No action on cancel
            @Override public void onAnimationRepeat(Animator animator) {} // No action on repeat
        });
        hideAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        hideAnimation.start();
    }

    // Updates the UI colors based on the authentication state
    // Square will use gradients when supported by Android version
    private void setupLayoutOnAuthentication(AUTHENTICATION_STATE aState)
    {
        // Colors used in background color transition
        ColorDrawable[] colorTransition = new ColorDrawable[2];
        // colorTransition[0] = FROM, colorTransition[1] = TO color
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
                }, Consts.FINGERPRINT_LOCKED_TIME_IN_MILLIS);
                break;
            }
            case RESET:
            {
                colorTransition[1] = new ColorDrawable(eggShellWhiteColor);
                loginSquare.setBackground(redGradient);
                showBiometricsPrompt();
                break;
            }
        }

        // transitions background color to color set in switch above
        TransitionDrawable trans = new TransitionDrawable(colorTransition);

        // currentBackground is stored for future transitions
        currentBackground = colorTransition[1].getColor();

        // transition starts
        rootElement.setBackgroundDrawable(trans);
        trans.startTransition(Consts.ANIMATION_LENGTH_MS);
    }

    // Restart the authentication, called from XML, hence parameter v
    public void Reset(View v)
    {
        setResetButtonVisibility(false);
        animateLogin(AUTHENTICATION_STATE.RESET);
    }

    // Set reset button visibility, with animations!
    public void setResetButtonVisibility(boolean aVisible)
    {
        if (aVisible)
        {
            btReset.setAlpha(Consts.TRANSPARENT_ALPHA_VALUE);
            btReset.setVisibility(View.VISIBLE);
            btReset.animate().alpha(Consts.OPAQUE_ALPHA_VALUE);
        }
        else
        {
            btReset.setAlpha(Consts.OPAQUE_ALPHA_VALUE);
            btReset.animate().alpha(Consts.TRANSPARENT_ALPHA_VALUE).withEndAction(new Runnable() {
                @Override
                public void run() {
                    btReset.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    // Sets up a biometrics prompt for current activity
    public void showBiometricsPrompt()
    {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BiometricsHandler.getBiometricsHandlerInstance(MainActivity.this).setupBiometricPrompt(MainActivity.this, Consts.BIOMETRICS_PROMPT_TITLE, Consts.BIOMETRICS_PROMPT_SUBTITLE);
            }
        }, Consts.ONE_SECOND_IN_MILLIS);
    }

    // Starts animation after authentication result is received
    @Override
    public void onAuthenticationResult(AUTHENTICATION_STATE aState) {
        animateLogin(aState);
    }
}