package com.example.authenticatorpoc.constants;

public final class Consts {

    // Development variables
    public static final boolean DEV_MODE = true;

    // Logging tag
    public static final String  LOG_TAG_AUTHENTICATION_ACTIVITY = "AUTH";
    // Logging
    public static final String  LOG_BEFORE_AUTHENTICATION = "Authenticating";
    public static final String  LOG_BIOMETRICS_AVAILABLE = "App can authenticate using biometrics.";
    public static final String  LOG_BIOMETRICS_NOT_SETUP = "The user hasn't associated any biometric credentials with their account.";
    public static final String  LOG_BIOMETRICS_UNAVAILABLE = "No biometric features available on this device.";
    public static final String  LOG_BIOMETRICS_UNAVAILABLE_TEMPORARILY = "Biometric features are currently unavailable.";

    // Layout properties used in code
    public static final String  PROPERTY_Y_ROTATION = "rotationY";

    // Presentation strings
    public static final String  BUTTONTEXT_CANCEL = "Cancel";

    public static final String  TOAST_MESSAGE_AUTHENTICATION_ERROR = "Authentication error: ";
    public static final String  TOAST_MESSAGE_AUTHENTICATION_SUCCEEDED = "Authentication succeeded!";
    public static final String  TOAST_MESSAGE_AUTHENTICATION_FAILED = "Authentication failed";

    public static final String  BIOMETRICS_PROMPT_TITLE = "Please login using a fingerprint";
    public static final String  BIOMETRICS_PROMPT_SUBTITLE = "This app will not store data";

    // Numbers
    public static final int     ANIMATION_LENGTH_MS = 600;
    // 30 seconds biometrics unavailable when locked out
    // [C-1-5] - https://source.android.com/compatibility/9/android-9-cdd.html#7_3_10_1_fingerprint_sensors
    public static final int     FINGERPRINT_LOCKED_TIME_IN_MILLIS = 30000;
    public static final int     ONE_SECOND_IN_MILLIS = 1000;
    public static final float   OPAQUE_ALPHA_VALUE = 1.0f;
    public static final float   TRANSPARENT_ALPHA_VALUE = 0.0f;
}
