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

import com.example.authenticatorpoc.helpers.BiometricsHandler;
import com.example.authenticatorpoc.helpers.Consts;
import com.example.authenticatorpoc.helpers.IBiometricsPromptCallback;
import com.example.authenticatorpoc.helpers.Logger;

public class MainActivity extends AppCompatActivity implements IBiometricsPromptCallback {

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
                Logger.debug(Consts.TAG_AUTHENTICATION_ACTIVITY, "Biometrics available? " + String.valueOf(BiometricsHandler.getBiometricsHandlerInstance(this).checkBiometricsAvailability()));
                showBiometricsPrompt();
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

    public void showBiometricsPrompt()
    {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BiometricsHandler.getBiometricsHandlerInstance(MainActivity.this).setupBiometricPrompt(MainActivity.this);
            }
        }, 1000);
    }

    @Override
    public void handleAuthenticationResult(Consts.AUTHENTICATION_STATE aState) {
        animateLogin(aState);
    }
}