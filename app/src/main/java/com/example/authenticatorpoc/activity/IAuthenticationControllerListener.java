package com.example.authenticatorpoc.activity;

public interface IAuthenticationControllerListener {
    void onAuthenticationSuccess();

    void onAuthenticationFailure();

    void onAuthenticationError();
}
