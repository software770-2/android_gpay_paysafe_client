package com.software770.paysafeforyoutube.otherClasses;

import com.paysafe.PaysafeApiClient;
import com.paysafe.customervault.PayWithGooglePaymentToken;

/**
 * Created by ymaza on 2018-03-13.
 */

public class GoogleReqInfos {
    // Configuration
    public String merchantApiKeyIdSBOX;
    public String merchantApiKeyPasswordSBOX;
    public String merchantAccountNumberSBOX;

    public String mSignature;
    public String mProtocolVersion;
    public String mSignedMessage;

    public PaysafeApiClient client;


    // error messages:
    public String mMessage;
    public String mCode;


    public PayWithGooglePaymentToken payWithGoogleToken;
}
