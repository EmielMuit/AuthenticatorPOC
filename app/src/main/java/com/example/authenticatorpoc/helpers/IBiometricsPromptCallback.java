package com.example.authenticatorpoc.helpers;

import com.example.authenticatorpoc.constants.AUTHENTICATION_STATE;

public interface IBiometricsPromptCallback {
    // Action to perform when authentication is finished
    public void onAuthenticationResult(AUTHENTICATION_STATE aState);
}
