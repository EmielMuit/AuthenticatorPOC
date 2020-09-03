package com.example.authenticatorpoc.helpers;

public interface IBiometricsPromptCallback {
    public void handleAuthenticationResult(Consts.AUTHENTICATION_STATE aState);
}
