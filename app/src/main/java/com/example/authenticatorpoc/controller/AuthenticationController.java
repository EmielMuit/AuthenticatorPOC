package com.example.authenticatorpoc.controller;

import com.example.authenticatorpoc.activity.IAuthenticationControllerListener;
import com.example.authenticatorpoc.activity.IShowBiometricsPromptListener;
import com.example.authenticatorpoc.constants.AUTHENTICATION_STATE;
import com.example.authenticatorpoc.helper.IBiometricsPromptListener;
import com.example.authenticatorpoc.view.AuthenticationView;

public class AuthenticationController implements IBiometricsPromptListener, IShowBiometricsPromptListener {

    private final AuthenticationView authenticationView;
    private final IAuthenticationControllerListener listener;

    public AuthenticationController(AuthenticationView aAuthenticationView, IAuthenticationControllerListener aAuthenticationControllerListener) {
        this.authenticationView = aAuthenticationView;
        this.listener = aAuthenticationControllerListener;
    }

    @Override
    public void onAuthenticationResult(AUTHENTICATION_STATE aState) {
        switch (aState) {
            case ERROR: {
                authenticationView.animateLogin(AUTHENTICATION_STATE.ERROR);
                listener.onAuthenticationError();
                break;
            }
            case FAILED: {
                authenticationView.animateLogin(AUTHENTICATION_STATE.FAILED);
                listener.onAuthenticationFailure();
                break;
            }
            case SUCCEEDED: {
                authenticationView.animateLogin(AUTHENTICATION_STATE.SUCCEEDED);
                listener.onAuthenticationSuccess();
                break;
            }
        }
    }

    @Override
    public void showPrompt() {
        listener.onResetApp();
    }
}
