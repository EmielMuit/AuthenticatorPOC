package com.example.authenticatorpoc.helper;

import com.example.authenticatorpoc.constants.AUTHENTICATION_STATE;

public interface IBiometricsPromptListener {
    // Action to perform when authentication is finished
    void onAuthenticationResult(AUTHENTICATION_STATE aState);
}
