package com.software770.paysafeforyoutube.otherClasses;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.paysafe.Environment;
import com.paysafe.PaysafeApiClient;
import com.paysafe.common.Error;
import com.paysafe.common.PaysafeException;
import com.paysafe.customervault.Card;
import com.paysafe.customervault.SingleUseToken;
import com.software770.utils.Constants;
import com.software770.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import static com.software770.utils.Utils.showDialogAlert;

/**
 * Created by ymaza on 2018-03-13.
 */

public class ThreadSingleUseToken extends AsyncTask<String, Void, SingleUseToken> {

    Context mContext;
    String mWait_mesage;
    GoogleReqInfos mModel;
    String mAmount;

    public ThreadSingleUseToken(Context p_context, String p_wait_mesage, GoogleReqInfos p_model, String p_amount)
    {
        mContext = p_context;
        this.mWait_mesage = p_wait_mesage;
        this.mModel = p_model;
        this.mAmount = p_amount;
        //HttpURLConnection
    }

    /**
     * On Pre Execute.
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Utils.startProgressDialog(mContext, this.mWait_mesage);

        //this.d();
        //getString(R.string.loading_text));
    } // end of onPreExecute()

    @Override
    protected void onPostExecute(SingleUseToken singleUseTokenObject) {
        super.onPostExecute(singleUseTokenObject);

        try {
            Utils.stopProgressDialog();

            showDialogAlert(this.mModel.merchantApiKeyIdSBOX + ", " + this.mModel.merchantApiKeyPasswordSBOX + ", " + this.mModel.merchantAccountNumberSBOX, mContext);

           /* if (singleUseTokenObject == null )
            {
                showDialogAlert("singleUseTokenObject is null !", mContext);
            }*/

            if(this.mModel.mMessage != null || this.mModel.mCode != null){
                showDialogAlert(Constants.ERROR_CODE+this.mModel.mCode+"\n"+Constants.ERROR_MESSAGE+this.mModel.mMessage, mContext);
            }else if(singleUseTokenObject != null) {
                // pay with google payment token
                this.mModel.payWithGoogleToken = singleUseTokenObject.getPayWithGooglePaymentToken();
                String mPaymentToken = singleUseTokenObject.getPaymentToken();
                String mPaymentMethod = singleUseTokenObject.getPayWithGooglePaymentToken()
                        .getPaymentMethod();
                Integer mTimeToLiveSeconds = singleUseTokenObject.getTimeToLiveSeconds();
                // card
                Card card = singleUseTokenObject.getCard();
                String lastDigits = card.getLastDigits();
                String status = card.getStatus();

                //error
                Error error = singleUseTokenObject.getError();

                String connectivityError = singleUseTokenObject.getConnectivityError();
                if (!Utils.isEmpty(connectivityError)) {
                    showDialogAlert(Constants.PLEASE_TURN_ON_YOUR_INTERNET, mContext);
                }
                if (error != null) {
                    String strMessage = error.getMessage();
                    String strCode = error.getCode();
                    showDialogAlert(strCode + ": " + strMessage, mContext);
                } else if (!Utils.isEmpty(mPaymentToken)) {
                    if (!Utils.isEmpty(lastDigits)) {
                        showDialogAlert("Payment Token :" + "  " + mPaymentToken + "\n" +
                                "Time To Live Seconds :" + "  " + mTimeToLiveSeconds + "\n" +
                                "Card Last Digit :" + " " + lastDigits + "\n" +
                                "Card Status :" + " " + status, mContext);
                    } else {
                        showDialogAlert("Pay With Google Token :" + "  " + mPaymentToken, mContext);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will make a call to Single Use Token  API.
     *
     * @return Single Use Token Object.
     */
    protected SingleUseToken doInBackground(String... args){

        //showDialogAlert("before comm server!", mContext);

        /*AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(mContext);
        dlgAlert.setMessage("This is an alert with no consequence");
        dlgAlert.create().show();*/

        try {
            this.mModel.merchantApiKeyIdSBOX = Utils.getProperty("merchant_api_key_id_sbox", mContext);
            this.mModel.merchantApiKeyPasswordSBOX = Utils.getProperty("merchant_api_key_password_sbox", mContext);
            this.mModel.merchantAccountNumberSBOX = Utils.getProperty("merchant_account_number_sbox", mContext);
        } catch(IOException ioExp) {
            showDialogAlert("IOException: "+ ioExp.getMessage(), mContext);
        }

        // Client Object
        this.mModel.client = new PaysafeApiClient(this.mModel.merchantApiKeyIdSBOX, this.mModel.merchantApiKeyPasswordSBOX,
                Environment.TEST, this.mModel.merchantAccountNumberSBOX);


        try {
            SingleUseToken sObjResponse;

            // Make API call for single use token
            sObjResponse = this.mModel.client.customerVaultService()
                    .createPayWithGooglePaymentToken(
                            SingleUseToken.builder()
                                    .payWithGooglePaymentToken()
                                    .signature(this.mModel.mSignature)
                                    .protocolVersion(this.mModel.mProtocolVersion)
                                    .signedMessage(this.mModel.mSignedMessage)
                                    .done()
                                    .build());

            Gson gson = new Gson();
            System.err.print("***************************************************************\n");
            System.err.print("***************************************************************\n");
            System.err.print(sObjResponse.getCard().toString() + "\n");
            //System.err.print(sObjResponse.getError().toString() + "\n");
            System.err.print("Token:  " + sObjResponse.getPaymentToken().toString() + "\n");

            DonationToken tempTokenToSend = new DonationToken();


            tempTokenToSend.prpToken = sObjResponse.getPaymentToken().toString();
            tempTokenToSend.amount = this.mAmount;


            System.err.print("\n***************************************************************\n");
            System.err.print("***************************************************************\n");
            //String requestData = gson.toJson(sObjResponse);
            String requestData = gson.toJson(tempTokenToSend);

            this.post(requestData);
            //
            // this.fncSendTokenToServer(requestData);
            /*if (sObjResponse == null )
            {
                showDialogAlert("sObjResponse is null !", mContext);
            }
            else
            {
                showDialogAlert("sObjResponse is not null !", mContext);
            }*/
            return sObjResponse;
        } catch (PaysafeException e) {

            this.mModel.mMessage = e.getMessage();
            this.mModel.mCode = e.getCode();

            // LOG
            Utils.debugLog("PwgPayloadActivity-Error Code: "+ this.mModel.mCode);
            Utils.debugLog("PwgPayloadActivity-Error Message: "+ this.mModel.mMessage);

        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    } // end of singleUseTokenRequest()

/*
    private HttpClient instanciateHttpClient()
    {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(params, true);

        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

        return new DefaultHttpClient(conMgr, params);
    }*/

    public void sendData(String p_data)
    {

    }

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    String post(String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url("http://www.donations613.com/Home/PostUser")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public void fncSendTokenToServer(String p_json){
        String urlString = "http://www.donations613.com/Home/PostUser"; //params[0]; // URL to call

        String data = p_json;//params[1]; //data to post

        OutputStream out = null;
        try {

            URL url = new URL(urlString);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            out = new BufferedOutputStream(urlConnection.getOutputStream());

            BufferedWriter writer = new BufferedWriter (new OutputStreamWriter(out, "UTF-8"));

            writer.write(data);

            writer.flush();

            writer.close();

            out.close();

            urlConnection.connect();


        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
    /*
    // http://hmkcode.com/android-send-json-data-to-server/
    public void fncSendTokenToServer(String json){
        //String url,

        InputStream inputStream = null;
        String result = "";

        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost("http://www.donations613.com/process.aspx");

            // 3 and 4 convert to json
            //...

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";
        } catch (Exception e) {
            //Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        //return result;
    }*/


    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}



