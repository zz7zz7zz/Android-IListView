package com.open.widgetdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;

import com.open.widgets.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_pull);
        initView();
    }

    private void initView(){
        findViewById(R.id.listView).setOnClickListener(clickListener);
        findViewById(R.id.recyclerview).setOnClickListener(clickListener);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.listView:
                    startActivity(new Intent(getApplicationContext(),IListViewActivity.class));
                    break;

                case R.id.recyclerview:
                    startActivity(new Intent(getApplicationContext(),IRecyclerViewActivity.class));
                    break;

            }
        }
    };


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            android.os.Process.killProcess(android.os.Process.myPid());
//			System.exit(0);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
