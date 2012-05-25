package com.android.inputmethod.norwegian;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

public class InfoScreen extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        this.setContentView(R.layout.info_screen);
        this.getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);
        this.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        
        TextView enableText = (TextView) this.findViewById(R.id.app_info_enable);
        Button enableButton = (Button) this.findViewById(R.id.app_info_enable_button);
        Button chooseButton = (Button) this.findViewById(R.id.app_info_choose_button);
        Button closeButton = (Button) this.findViewById(R.id.app_info_button_close);
        
        if(Integer.parseInt(Build.VERSION.SDK) < 5)
            enableText.setText(getResources().getString(R.string.app_info_enable).replace("Language & keyboard", "Locale & text"));
        
        enableButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS));
            }
        });
        
        chooseButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker();
            }
        });
        
        closeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
}
