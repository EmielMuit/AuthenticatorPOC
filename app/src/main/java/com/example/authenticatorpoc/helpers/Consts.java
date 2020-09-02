package com.example.authenticatorpoc.helpers;

public final class Consts {

    public static final int ANIMATION_LENGTH_MS = 600;

    public static final boolean DEV_MODE = true;

    // Authentication result enum
    public enum AUTHENTICATION_STATE {
        SUCCEEDED,
        FAILED,
        ERROR
    }

    // Properties
    public static final String Y_ROTATION_PROPERTYNAME = "rotationY";


    // Logging tag
    public static final String TAG_AUTHENTICATION_ACTIVITY = "AUTH";
    // Logging
    public static final String LOG_BEFORE_AUTHENTICATION = "Authenticating";
    public static final String LOG_BIOMETRICS_AVAILABLE = "App can authenticate using biometrics.";
    public static final String LOG_BIOMETRICS_UNAVAILABLE = "No biometric features available on this device.";
    public static final String LOG_BIOMETRICS_UNAVAILABLE_TEMPORARILY = "Biometric features are currently unavailable.";
    public static final String LOG_BIOMETRICS_NOT_SETUP = "The user hasn't associated any biometric credentials with their account.";


    // Presentation strings
    public static final String MESSAGE_AUTHENTICATION_ERROR = "Authentication error: ";
    public static final String MESSAGE_AUTHENTICATION_SUCCEEDED = "Authentication succeeded!";
    public static final String MESSAGE_AUTHENTICATION_FAILED = "Authentication failed";

    public static final String PROMPT_TITLE = "Please login using a fingerprint";
    public static final String PROMPT_SUBTITLE = "This app will not store data";
}
