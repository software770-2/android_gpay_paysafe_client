package com.software770.paysafeforyoutube;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.gson.Gson;
import com.paysafe.customervault.PayWithGooglePaymentToken;
import com.software770.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
public class PaymentMethods extends AppCompatActivity {

    private String c_Amount = "";


    // Context
    private Context mContext;

    // Button
    private Button mBtnSingleUseToken;
    private Button mBackButton;


    private PayWithGooglePaymentToken payWithGooglePayload;
    private PaymentsClient paymentsClient;

    private String mMerchantUsername;

    private String mPwgPayload;
    private String mSignature;
    private String mProtocolVersion;
    private String mSignedMessage;

    // Flags to set/unset payment options
    private int flagPayWithGoogle;
    private int flagAndroidPay;


    // Flag array
    private ArrayList<Integer> flagPaymentMethods;

    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 888;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_methods);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        c_Amount = getIntent().getStringExtra("amount");

        Button btnGoogle = (Button)findViewById(R.id.btnGoogle);

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPayWithGoogle_Clicked();
            }
        });

        Button btnPaySafe = (Button)findViewById(R.id.btnPaySafe);

        btnPaySafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Pay Safe" + c_Amount + "$", Toast.LENGTH_LONG).show();
            }
        });

        // Context
        mContext = this;
        // setMerchantUsername
        try {
            mMerchantUsername = Utils.getProperty("merchant_api_key_id_sbox", mContext);
        } catch(IOException ioExp) {
            Utils.showDialogAlert("IOException: "+ ioExp.getMessage(), mContext);
        }

        // Setup instance for PaymentsClient
        paymentsClient = Wallet.getPaymentsClient(this,
                new Wallet.WalletOptions.Builder()
                        .setEnvironment(WalletConstants.ENVIRONMENT_SANDBOX)
                        //.setEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION)
                        .build());

        this.isReadyToPay();
    }


    // originaly: createPaymentDataRequest()
    private void onPayWithGoogle_Clicked()
    {
        PaymentDataRequest.Builder request =
                PaymentDataRequest .newBuilder()
                        .setTransactionInfo(
                                TransactionInfo.newBuilder()
                                        .setTotalPriceStatus(
                                                WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                                        .setTotalPrice("1.00")
                                        .setCurrencyCode("USD")
                                        .build())
                        .addAllowedPaymentMethods(flagPaymentMethods)
                        .setCardRequirements(
                                CardRequirements.newBuilder()
                                        .addAllowedCardNetworks(Arrays.asList(
                                                WalletConstants.CARD_NETWORK_AMEX,
                                                WalletConstants.CARD_NETWORK_DISCOVER,
                                                WalletConstants.CARD_NETWORK_VISA,
                                                WalletConstants.CARD_NETWORK_MASTERCARD))
                                        .build());
        PaymentMethodTokenizationParameters params =
                PaymentMethodTokenizationParameters .newBuilder()
                        .setPaymentMethodTokenizationType(
                                WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY )
                        .addParameter("gateway", getResources().getString(R.string.gateway))
                        .addParameter("gatewayMerchantId",
                                mMerchantUsername)
                        .build();
        request.setPaymentMethodTokenizationParameters(params);


        // originaly : requestPayment
        PaymentDataRequest theRequest = request.build();

        if (request != null) {
            AutoResolveHelper.resolveTask(
                    paymentsClient.loadPaymentData(theRequest), this,
                    LOAD_PAYMENT_DATA_REQUEST_CODE);
        }
    }

    /**
     * IsReadyToPay
     */
    private void isReadyToPay() {
        IsReadyToPayRequest request = IsReadyToPayRequest.newBuilder()
                .addAllowedPaymentMethod( WalletConstants.PAYMENT_METHOD_CARD)
                .addAllowedPaymentMethod( WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                .build();
        Task<Boolean> task = paymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(
                new OnCompleteListener<Boolean>() {
                    public void onComplete( Task<Boolean> task) {
                        try {
                            boolean result =
                                    task.getResult( ApiException.class);
                            if(result == true) {
                                // Google Payment supported on device
                                //show Google as payment option
                                flagPaymentMethods = new ArrayList<Integer>();
                                // CHECK, if device supports NFC
                                // if YES, show below payment methods
                                // 1. Card on file
                                // 2. Android Pay
                                if (Utils.isNFCAvailable(mContext.getApplicationContext())) {
                                    // set payment methods
                                    flagPayWithGoogle = WalletConstants.PAYMENT_METHOD_CARD;
                                    flagAndroidPay = WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD;
                                    // Add Options to Payment Methods
                                    flagPaymentMethods.add(flagPayWithGoogle);
                                    flagPaymentMethods.add(flagAndroidPay);

                                }
                                else {
                                    // if NO (device does'nt support NFC), show below payment methods
                                    // 1. Card on file
                                    flagPayWithGoogle = WalletConstants.PAYMENT_METHOD_CARD;
                                    // Add Options to Payment Methods
                                    flagPaymentMethods.add(flagPayWithGoogle);

                                }

                            } else {
                                // Google Payment not supported on device
                                //hide Google as payment option
                                Utils.showDialogAlert("Google Payment Option Not Supported!",
                                        mContext);

                                Button btnGoogle = (Button)findViewById(R.id.btnGoogle);
                                btnGoogle.setEnabled(false);

                            }
                        } catch (ApiException exception) {
                            Toast.makeText(getApplicationContext(), exception.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    } // end of isReadyToPay()


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        PaymentData paymentData =
                                PaymentData.getFromIntent(data);
                        String token = paymentData.getPaymentMethodToken().getToken();

                        // Convert from JSON format
                        Gson gson = new Gson();
                        payWithGooglePayload = gson.fromJson(token,
                                PayWithGooglePaymentToken.class);

                        // Get data
                        mPwgPayload = token;
                        mSignature = payWithGooglePayload.getSignature();
                        mProtocolVersion = payWithGooglePayload.getProtocolVersion();
                        mSignedMessage = payWithGooglePayload.getSignedMessage();

                        // Call Next Activity using Intent

                        Intent intentPayWithGoogle = new Intent(PaymentMethods.this,
                                ProcessGooglePayment.class);
                        intentPayWithGoogle.putExtra("PwgPayload", mPwgPayload);
                        intentPayWithGoogle.putExtra("Signature", mSignature);
                        intentPayWithGoogle.putExtra("ProtocolVersion", mProtocolVersion);
                        intentPayWithGoogle.putExtra("SignedMessage", mSignedMessage);
                        intentPayWithGoogle.putExtra("amount", c_Amount);

                        startActivity(intentPayWithGoogle);
                        finish();

                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    case AutoResolveHelper .RESULT_ERROR:
                        Status status =
                                AutoResolveHelper
                                        .getStatusFromIntent(data);

                        // Log the status for debugging
                        // Generally there is no need to show an error to
                        // the user as the Google Payment API will do that
                        // LOG
                        Utils.debugLog("Status Code: " + status.getStatusCode());
                        Utils.debugLog("Status message: " + status.getStatusMessage());
                        Utils.debugLog("Status : " + status.getStatus().toString());
                        break;
                    default:
                        // Do nothing.
                }
                break;
            default:
                // Do nothing.
        }
    } // end of onActivityResult()

}
