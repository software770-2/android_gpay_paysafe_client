package com.software770.paysafeforyoutube;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private EditText txtAmount;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        txtAmount = (EditText) findViewById(R.id.amount);

        Button btnDonate = (Button)findViewById(R.id.donate);


    }

   public void donate_clicked(View v) {
            Intent intent = new Intent(MainActivity.this, PaymentMethods.class);
            //Bundle b = new Bundle();
            //b.putString("amount", txtAmount.getText().toString()); //Your id
            intent.putExtra("amount", txtAmount.getText().toString());
            startActivity(intent);
            //Toast.makeText(getApplicationContext(), "Your toast message", Toast.LENGTH_LONG).show();
        }
}
