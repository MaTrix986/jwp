package matrix.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.room.Room;

import java.util.List;

import matrix.bluetooth.LVAdapter.LVSportDataAdapter;
import matrix.bluetooth.sportdb.SportData;
import matrix.bluetooth.sportdb.SportDatabase;

public class DataActivity extends Activity {

    private static final String TAG = "JWP" ;
    private static final int REQUEST_CODE = 0x00;
    private static final int DEVICE_RECOVER = 0x00;

    private ImageButton btReturn;
    private ImageButton btDeleteAll;
    private ListView lvSportData;
    private LVSportDataAdapter lvSportDataAdapter;
    Context mContext;

    BluetoothDevice device;

    List<SportData> data;

    SportDatabase DB;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case DEVICE_RECOVER:
                    device = (BluetoothDevice) msg.obj;
                    break;
            }

        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_data);
        setFullscreen(true, true);

        mContext = DataActivity.this;

        //动态申请权限（Android 6.0）

        initView();
        initData();
        iniListener();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Intent intent = new Intent(DataActivity.this, MainActivity.class);
        intent.putExtra("device", device);
        setResult(RESULT_OK, intent);

        finish();


        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode,resultCode, intent);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if (intent != null) {
                device = (BluetoothDevice) intent.getExtras().get("device");
            }
        }
    }

    private void initView() {
        btReturn = findViewById(R.id.bt_return);
        btDeleteAll = findViewById(R.id.bt_delete_all);
        lvSportData = findViewById(R.id.lv_sport_data);
    }

    void initData(){
        lvSportDataAdapter = new LVSportDataAdapter(DataActivity.this);
        lvSportData.setAdapter(lvSportDataAdapter);
        Log.d(TAG, "lvSportDataAdapter bind success!");

        DB = Room.databaseBuilder(this, SportDatabase.class, "sportDB").allowMainThreadQueries().build();
        data = DB.sportDataDao().loadAll();

        lvSportDataAdapter.addAllDevice(data);
    }

    private void iniListener() {
        btReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DataActivity.this, MainActivity.class);
                intent.putExtra("device", device);
                setResult(RESULT_OK, intent);


                finish();
            }
        });

        btDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v){

                AlertDialog.Builder builder = new AlertDialog.Builder(DataActivity.this);
                builder.setTitle("删除数据");//设置弹出对话框的标题
                builder.setIcon(R.drawable.ic_data);//设置弹出对话框的图标
                builder.setMessage("确认删除所有数据吗？");//设置弹出对话框的内容
                builder.setCancelable(false);//能否被取消

                builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DB.sportDataDao().deleteSportData();
                        lvSportDataAdapter.clear();
                        Toast.makeText(DataActivity.this, "已全部删除", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });

                builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        lvSportData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SportData sportData = (SportData) lvSportDataAdapter.getItem(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(DataActivity.this);
                builder.setTitle("删除数据");//设置弹出对话框的标题
                builder.setIcon(R.drawable.ic_data);//设置弹出对话框的图标
                builder.setMessage("确认删除当前数据吗？步数:" + sportData.stepCount);//设置弹出对话框的内容
                builder.setCancelable(false);//能否被取消

                builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DB.sportDataDao().deleteSportData(sportData);

                        lvSportDataAdapter.clear();
                        data = DB.sportDataDao().loadAll();
                        lvSportDataAdapter.addAllDevice(data);

                        Toast.makeText(DataActivity.this, "已删除", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });

                builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    public void setFullscreen(boolean isShowStatusBar, boolean isShowNavigationBar) {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        if (!isShowStatusBar) {
            uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        if (!isShowNavigationBar) {
            uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);

        //隐藏标题栏
        // getSupportActionBar().hide();
        setNavigationStatusColor(Color.TRANSPARENT);
    }

    public void setNavigationStatusColor(int color) {
        //VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setNavigationBarColor(color);
            getWindow().setStatusBarColor(color);
        }
    }
}
