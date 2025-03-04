package matrix.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import matrix.bluetooth.LVAdapter.LVDevicesAdapter;
import matrix.bluetooth.ble.BLEDevice;
import matrix.bluetooth.ble.BLEManager;
import matrix.bluetooth.ble.OnBleConnectListener;
import matrix.bluetooth.ble.OnDeviceSearchListener;
import matrix.bluetooth.permission.PermissionListener;
import matrix.bluetooth.permission.PermissionRequest;
import matrix.bluetooth.sportdb.SportData;
import matrix.bluetooth.sportdb.SportData.mTime;
import matrix.bluetooth.sportdb.SportData.mDate;
import matrix.bluetooth.sportdb.SportDatabase;
import matrix.bluetooth.util.LineChartManager;
import matrix.bluetooth.util.TypeConversion;

public class ConnectActivity extends Activity {
    private static final String TAG = "JWP" ;
    private static final int REQUEST_CODE = 0x00;
    public static final String SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";  //蓝牙通讯服务
    public static final String READ_UUID = "91ade9ae-3e72-4d67-8028-1fcc3de4a5ab";  //读特征
    public static final String WRITE_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a9";  //写特征
    public static final String READ_UUID_DATA = "d065e403-a689-40d4-af6c-aab882aa2e28";

    private String[] requestPermissionArray = new String[]{
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };

    private static final int CONNECT_SUCCESS = 0x01;
    private static final int CONNECT_FAILURE = 0x02;
    private static final int DISCONNECT_SUCCESS = 0x03;
    private static final int SEND_SUCCESS = 0x04;
    private static final int SEND_FAILURE= 0x05;
    private static final int RECEIVE_STEP= 0x06;
    private static final int RECEIVE_FAILURE =0x07;
    private static final int START_DISCOVERY = 0x08;
    private static final int STOP_DISCOVERY = 0x09;
    private static final int DISCOVERY_DEVICE = 0x0A;
    private static final int DISCOVERY_OUT_TIME = 0x0B;
    private static final int SELECT_DEVICE = 0x0C;
    private static final int BT_OPENED = 0x0D;
    private static final int BT_CLOSED = 0x0E;
    private static final int READ_SUCCESS = 0x0F;
    private static final int CONNECT_ON = 0x10;
    private static final int CONNECT_UNDO = 0x11;
    private static final int DEVICE_RECOVER = 0x12;
    private static final int SEARCH_PRESSED = 0x13;
    private static final int RECEIVE_DATA= 0x14;


    private ImageButton btReturn;
    private Button btSearch;
    private Button btLayoutConnect;
    private Button btLayoutRun;
    private ImageButton btBack;
    private Button btStart;
    private Button btStop;
    private Button btImport;
    private Button btRestart;
    private LineChart lcData;
    private TextView tvTitle;
    private TextView tvCurState;
    private TextView tvCurDevice;
    private  TextView tvDeviceState;
    private LinearLayout llConnectBt;
    private LinearLayout llRunBt;
    private LinearLayout llDeviceList;
    private ListView lvDevices;
    private LVDevicesAdapter lvDevicesAdapter;

    private List<String> deniedPermissionList = new ArrayList<>();
    private Context mContext;
    private BLEManager bleManager;
    private BLEBroadcastReceiver bleBroadcastReceiver;
    private BluetoothDevice curBluetoothDevice;  //当前连接的设备
    //当前设备连接状态
    private boolean curConnState = false;

    private LineChartManager mChartManager;
    //private static final List<String> names = new ArrayList<>(Arrays.asList("total", "x", "y", "z"));
    //private static final List<Integer> colors = new ArrayList<>(Arrays.asList(Color.WHITE, Color.RED, Color.GREEN, Color.BLUE)) ;


    private boolean isSearching = false;
    private boolean THREAD_STARTED = false;
    private boolean onPage = false;
    private boolean isRunning = false;
    private Date startDate;
    private Date endDate;
    SportDatabase DB;

    //SearchThread thread;


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            byte[] bytes;
            String res;
            BluetoothDevice bluetoothDevice;

            switch(msg.what){
                case START_DISCOVERY:
                    Log.d(TAG, "开始搜索设备...");
                    break;

                case STOP_DISCOVERY:
                    Log.d(TAG, "停止搜索设备...");
                    //Toast.makeText(ConnectActivity.this, "停止搜索设备...", Toast.LENGTH_SHORT).show();
                    break;

                case DISCOVERY_DEVICE:  //扫描到设备
                    BLEDevice bleDevice = (BLEDevice) msg.obj;
                    lvDevicesAdapter.addDevice(bleDevice);
                    Log.d(TAG, bleDevice.getBluetoothDevice().getName());
                    if (bleDevice.getBluetoothDevice().getName().equals("JIAOWOPAO") ) {
                        curBluetoothDevice = bleDevice.getBluetoothDevice();
                        connectBtDevice();
                        tvCurState.setText("连接状态: 连接中");
                        Toast.makeText(ConnectActivity.this, "开始连接", Toast.LENGTH_SHORT).show();
                        tvCurDevice.setText("当前设备: " + curBluetoothDevice.getName());
                        tvCurDevice.setTextColor(Color.rgb(200,200,200));
                    }

                    break;

                case DISCOVERY_OUT_TIME:
                    Log.d(TAG, "停止搜索设备...");
                    Toast.makeText(ConnectActivity.this, "停止搜索设备...", Toast.LENGTH_SHORT).show();
                    break;

                case SELECT_DEVICE:
                    bluetoothDevice = (BluetoothDevice) msg.obj;
                    curBluetoothDevice = bluetoothDevice;
                    connectBtDevice();

                    tvCurState.setText("连接状态: 连接中");
                    Toast.makeText(ConnectActivity.this, "开始连接", Toast.LENGTH_SHORT).show();
                    tvCurDevice.setText("当前设备: " + curBluetoothDevice.getName());
                    tvCurDevice.setTextColor(Color.rgb(200,200,200));
                    break;

                case CONNECT_FAILURE: //连接失败
                    Log.d(TAG, "连接失败");
                    curConnState = false;
                    tvCurState.setText("连接状态: 未连接");
                    tvCurDevice.setTextColor(Color.rgb(200,200,200));
                    Toast.makeText(ConnectActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                    break;

                case CONNECT_SUCCESS:  //连接成功
                    Log.d(TAG, "连接成功");
                    curConnState = true;
                    tvCurState.setText("连接状态: 已连接");
                    Toast.makeText(ConnectActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    tvCurDevice.setText("当前设备: " + curBluetoothDevice.getName());
                    tvCurDevice.setTextColor(Color.WHITE);
                    //llDeviceList.setVisibility(View.GONE);
                    break;

                case DISCONNECT_SUCCESS:
                    int status = (int) msg.obj;
                    String disconnectRessult;
                    if (status == 0) {
                        disconnectRessult = "断开成功";
                    }
                    if (status == 8) {
                        disconnectRessult = "设备失联断开";
                    }
                    if (status == 34) {
                        disconnectRessult = "34断开";
                    }
                    else {
                        disconnectRessult = "异常断开";
                    }

                    Log.d(TAG, disconnectRessult);
                    curConnState = false;
                    tvCurState.setText("连接状态: 未连接");
                    Toast.makeText(ConnectActivity.this, disconnectRessult, Toast.LENGTH_SHORT).show();
                    break;

                case SEND_FAILURE: //发送失败
                    /*
                    bytes = (byte[]) msg.obj;
                    res = TypeConversion.bytes2HexString(bytes);
                    Log.e(TAG, "发送失败"+res);
                    Toast.makeText(ConnectActivity.this, "发送失败"+res, Toast.LENGTH_SHORT).show();

                     */
                    break;

                case SEND_SUCCESS:  //发送成功
                    /*
                    bytes = (byte[]) msg.obj;
                    res = TypeConversion.bytes2HexString(bytes);
                    Log.e(TAG, "发送成功"+res);
                    Toast.makeText(ConnectActivity.this, "发送成功"+res, Toast.LENGTH_SHORT).show();

                     */
                    break;

                case RECEIVE_FAILURE: //接收失败
                    Log.e(TAG, "RECEIVE_FAILURE");
                    break;

                case RECEIVE_STEP:  //接收成功


                    byte[] recvBytes = (byte[]) msg.obj;
                    int recvInt = TypeConversion.BytestoInt(recvBytes);
                    Log.d(TAG, "recv: "+TypeConversion.BytestoInt(recvBytes));

                    if (recvInt < 0){
                        if (recvInt == -1) {
                            tvDeviceState.setText("设备状态: 未知指令");
                        }
                        if (recvInt == -2) {
                            tvDeviceState.setText("设备状态: 正在跑步");
                        }
                        if (recvInt == -3) {
                            tvDeviceState.setText("设备状态: 跑步结束");
                        }
                        if (recvInt == -4) {
                            tvDeviceState.setText("设备状态: 仍在跑步");
                        }
                    }
                    else {
                        tvDeviceState.setText("跑步步数: " + recvInt);
                        if (startDate != null && endDate != null){
                            SportData sportData = new SportData(
                                    new SportData.mDate(endDate.getYear() + 1900, endDate.getMonth() + 1, endDate.getDate()),
                                    new SportData.mTime(startDate.getHours(), startDate.getMinutes(), startDate.getSeconds()),
                                    new SportData.mTime(endDate.getHours(), endDate.getMinutes(), endDate.getSeconds()),
                                    recvInt);

                            //Log.d(TAG, "startDate: " + startDate.getHours() +  ":" + startDate.getMinutes() + ":" + startDate.getSeconds());

                            DB.sportDataDao().insertSportData(sportData);
                        }
                    }

                    break;

                case RECEIVE_DATA:
                    byte[] recvBytes1 = (byte[]) msg.obj;
                    float recvFloat = TypeConversion.bytes2Float(recvBytes1);
                    Log.d(TAG, ""+recvFloat);
                    mChartManager.addEntry(recvFloat/2);


                    break;

                case BT_CLOSED:
                    Log.d(TAG, "系统蓝牙已关闭");
                    break;

                case BT_OPENED:
                    Log.d(TAG, "系统蓝牙已打开");
                    break;
                case READ_SUCCESS:
                    break;

                case CONNECT_ON:
                    break;
                case CONNECT_UNDO:
                    tvCurState.setText("连接状态: 断开中");
                    Toast.makeText(ConnectActivity.this, "正在断开", Toast.LENGTH_SHORT).show();
                    break;

                case SEARCH_PRESSED:
                    /*
                    if (!THREAD_STARTED){
                        thread.start();
                        THREAD_STARTED = true;
                    }
                    if (isSearching) {

                        isSearching = false;
                        btSearch.setBackground(getResources().getDrawable(R.drawable.tt_bg1));
                        stopSearchBtDevice();
                    }
                    else {
                        if(lvDevicesAdapter != null){
                            lvDevicesAdapter.clear();  //清空列表
                        }
                        searchBtDevice();
                        isSearching = true;
                        btSearch.setBackground(getResources().getDrawable(R.drawable.tt_bg));
                    }

                     */
                    if(lvDevicesAdapter != null){
                        lvDevicesAdapter.clear();
                    }
                    searchBtDevice();

                    break;

                case DEVICE_RECOVER:
                    bluetoothDevice = (BluetoothDevice) msg.obj;
                    curBluetoothDevice = bluetoothDevice;
                    connectBtDevice();
                    break;
            }
        }
    };
    /*
    private class SearchThread extends Thread{
        @Override
        public void run(){
            while (true){
                if (isSearching){
                    if (bleManager.isDiscovery()) {
                        bleManager.stopDiscoveryDevice();
                    }
                    searchBtDevice();
                }

                Log.d(TAG, "" + isSearching);
                try {
                    Thread.sleep(15000);//卖票速度是1s一张
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.ui_connect);
        setFullscreen(true, true);

        mContext = ConnectActivity.this;

        //动态申请权限（Android 6.0）

        initView();
        initListener();
        initData();
        initBLEBroadcastReceiver();
        initPermissions();

        //searchBtDevice();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDes");

        //注销广播接收
        unregisterReceiver(bleBroadcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode,resultCode, intent);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if (intent != null) {
                BluetoothDevice device = (BluetoothDevice) intent.getExtras().get("device");
                if (device != null){
                    Message message = new Message();
                    message.what = DEVICE_RECOVER;
                    message.obj = device;
                    mHandler.sendMessage(message);
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        BluetoothDevice device = bleManager.getCurConnDevice();
        Intent intent = new Intent(ConnectActivity.this, MainActivity.class);
        intent.putExtra("device", device);
        setResult(RESULT_OK, intent);

        if (bleManager != null && bleManager.isDiscovery()) {
            bleManager.stopDiscoveryDevice();
        }

        finish();


        return super.onKeyDown(keyCode, event);
    }

    private void initView() {

        btReturn = findViewById(R.id.bt_return);
        btLayoutRun = findViewById(R.id.bt_layout_run);
        btLayoutConnect = findViewById(R.id.bt_layout_connect);
        btSearch = findViewById(R.id.bt_search);
        btStart = findViewById(R.id.bt_start_run);
        btStop  = findViewById(R.id.bt_finish_run);
        btImport = findViewById(R.id.bt_import);
        btRestart = findViewById(R.id.bt_restart);


        tvTitle = findViewById(R.id.tv_title);
        tvCurState = findViewById(R.id.tv_curstate);
        tvCurDevice = findViewById(R.id.tv_curdevice);
        tvDeviceState = findViewById(R.id.tv_device_state);

        lcData = findViewById(R.id.line_chart);

        lvDevices = findViewById(R.id.lv_devices);

        llDeviceList = findViewById(R.id.ll_device_list);
        llConnectBt = findViewById(R.id.ll_connect_bt);
        llRunBt = findViewById(R.id.ll_run_bt);
    }


    private void initListener() {
        btReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                BluetoothDevice device = bleManager.getCurConnDevice();
                Intent intent = new Intent(ConnectActivity.this, MainActivity.class);
                intent.putExtra("device", device);
                setResult(RESULT_OK, intent);
                if (bleManager != null) {
                    bleManager.stopDiscoveryDevice();
                }


                finish();
            }
        });

        btLayoutRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onPage = true;
                lcData.setVisibility(View.VISIBLE);
                llDeviceList.setVisibility(View.GONE);

                llConnectBt.setVisibility(View.VISIBLE);
                llRunBt.setVisibility(View.GONE);

                btLayoutConnect.setBackground(getResources().getDrawable(R.drawable.unactive_bt_bg));
                btLayoutRun.setBackground(getResources().getDrawable(R.drawable.active_bt_bg));

                tvTitle.setText("去跑步");

            }
        });

        btLayoutConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPage = false;
                lcData.setVisibility(View.GONE);
                llDeviceList.setVisibility(View.VISIBLE);

                llConnectBt.setVisibility(View.GONE);
                llRunBt.setVisibility(View.VISIBLE);

                btLayoutConnect.setBackground(getResources().getDrawable(R.drawable.active_bt_bg));
                btLayoutRun.setBackground(getResources().getDrawable(R.drawable.unactive_bt_bg));

                tvTitle.setText("连设备");
            }
        });

        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.what = SEARCH_PRESSED;
                mHandler.sendMessage(message);
            }
        });

        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curConnState){
                    String msg = "START";
                    sendMessage(msg);
                    Toast.makeText(ConnectActivity.this, "开始跑步", Toast.LENGTH_SHORT).show();
                    startDate = new Date();
                    isRunning = true;

                }
                else {
                    Toast.makeText(ConnectActivity.this, "当前设备未连接", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curConnState){
                    String msg = "STOP";
                    sendMessage(msg);
                    Toast.makeText(ConnectActivity.this, "结束跑步", Toast.LENGTH_SHORT).show();
                    endDate = new Date();
                    isRunning = false;

                }
                else {
                    Toast.makeText(ConnectActivity.this, "当前设备未连接", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (curConnState){
                    String msg = "EXPORT";
                    sendMessage(msg);
                    Toast.makeText(ConnectActivity.this, "正在导入...", Toast.LENGTH_SHORT).show();

                }
                else {
                    Toast.makeText(ConnectActivity.this, "当前设备未连接", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (curConnState){

                    AlertDialog.Builder builder = new AlertDialog.Builder(ConnectActivity.this);
                    builder.setTitle("重启设备");//设置弹出对话框的标题
                    builder.setIcon(R.drawable.ic_run);//设置弹出对话框的图标
                    builder.setMessage("确认重启当前设备吗");//设置弹出对话框的内容
                    builder.setCancelable(false);//能否被取消

                    builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String msg = "RESTART";
                            sendMessage(msg);
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
                else{
                    Toast.makeText(ConnectActivity.this, "当前设备未连接", Toast.LENGTH_SHORT).show();
                }

            }
        });

        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BLEDevice bleDevice = (BLEDevice) lvDevicesAdapter.getItem(i);
                BluetoothDevice bluetoothDevice = bleDevice.getBluetoothDevice();
                if(bleManager != null){
                    bleManager.stopDiscoveryDevice();
                }
                Message message = new Message();
                message.what = SELECT_DEVICE;
                message.obj = bluetoothDevice;
                mHandler.sendMessage(message);
            }
        });

    }

    private void initData() {
        mChartManager = new LineChartManager(lcData, "G", Color.BLUE);
        mChartManager.setYAxis(15,-5,100);
        mChartManager.setAxisText(8, Color.BLACK);
        mChartManager.setDescription("加速度数据");

        //thread = new SearchThread();

        BluetoothDevice device = (BluetoothDevice) getIntent().getExtras().get("device");
        if (device != null){
            Message message = new Message();
            message.what = DEVICE_RECOVER;
            message.obj = device;
            mHandler.sendMessage(message);
        }

        lvDevicesAdapter = new LVDevicesAdapter(ConnectActivity.this);
        lvDevices.setAdapter(lvDevicesAdapter);

        bleManager = new BLEManager();
        if(!bleManager.initBle(mContext)) {
            Log.d(TAG, "该设备不支持低功耗蓝牙");
            Toast.makeText(mContext, "该设备不支持低功耗蓝牙(BLE)", Toast.LENGTH_SHORT).show();
        }else{
            if(!bleManager.isEnable()){

                bleManager.openBluetooth(mContext,false);
            }
        }


        DB = Room.databaseBuilder(ConnectActivity.this, SportDatabase.class, "sportDB").allowMainThreadQueries().build();
    }

    private void initBLEBroadcastReceiver() {
        //注册广播接收
        bleBroadcastReceiver = new BLEBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //开始扫描
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//扫描结束
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//手机蓝牙状态监听
        registerReceiver(bleBroadcastReceiver,intentFilter);
    }

    private void initPermissions() {
        //Android 6.0以上动态申请权限
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            final PermissionRequest permissionRequest = new PermissionRequest();
            permissionRequest.requestRuntimePermission(ConnectActivity.this, requestPermissionArray, new PermissionListener() {
                @Override
                public void onGranted() {
                    Log.d(TAG,"所有权限已被授予");
                }

                //用户勾选“不再提醒”拒绝权限后，关闭程序再打开程序只进入该方法！
                @Override
                public void onDenied(List<String> deniedPermissions) {
                    deniedPermissionList = deniedPermissions;
                    for (String deniedPermission : deniedPermissionList) {
                        Log.e(TAG,"被拒绝权限：" + deniedPermission);
                    }
                }
            });
        }
    }

    private void searchBtDevice() {
        if(bleManager == null){
            Log.d(TAG, "searchBtDevice()-->bleManager == null");
            return;
        }

        if (bleManager.isDiscovery()) { //当前正在搜索设备...
            bleManager.stopDiscoveryDevice();
        }



        //开始搜索
        bleManager.startDiscoveryDevice(onDeviceSearchListener,15000);
    }

    private void stopSearchBtDevice() {
        if(bleManager == null){
            Log.d(TAG, "searchBtDevice()-->bleManager == null");
            return;
        }



        if (bleManager.isDiscovery()) { //当前正在搜索设备...
            bleManager.stopDiscoveryDevice();
        }
    }

    private void connectBtDevice() {
        if(!curConnState) {
            if(bleManager != null){
                bleManager.connectBleDevice(mContext,curBluetoothDevice,15000,SERVICE_UUID,READ_UUID,WRITE_UUID, READ_UUID_DATA, onBleConnectListener);
            }
        }else{
            Toast.makeText(this, "当前设备已连接", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage(String msg) {
        bleManager.sendMessage(msg);
    }



    //扫描结果回调
    private OnDeviceSearchListener onDeviceSearchListener = new OnDeviceSearchListener() {

        @Override
        public void onDiscoveryStart() {
            Message message = new Message();
            message.what = START_DISCOVERY;
            mHandler.sendMessage(message);
        }

        @Override
        public void onDeviceFound(BLEDevice bleDevice) {
            if (bleDevice.getBluetoothDevice().getName() != null) {
                Message message = new Message();
                message.what = DISCOVERY_DEVICE;
                message.obj = bleDevice;
                mHandler.sendMessage(message);
            }
        }

        @Override
        public void onDiscoveryOutTime() {
            Message message = new Message();
            message.what = DISCOVERY_OUT_TIME;
            mHandler.sendMessage(message);
        }
    };

    //连接回调
    private OnBleConnectListener onBleConnectListener = new OnBleConnectListener() {
        @Override
        public void onConnecting(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice) {
            Message message = new Message();
            message.what = CONNECT_ON;
            mHandler.sendMessage(message);
        }

        @Override
        public void onConnectSuccess(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, int status) {
            /*
            Message message = new Message();
            message.what = CONNECT_SUCCESS;
            mHandler.sendMessage(message);

             */
        }

        @Override
        public void onConnectFailure(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, String exception, int status) {
            Log.d(TAG, exception);
            Message message = new Message();
            message.what = CONNECT_FAILURE;
            mHandler.sendMessage(message);
        }

        @Override
        public void onDisConnecting(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice) {
            Message message = new Message();
            message.what = CONNECT_UNDO;
            mHandler.sendMessage(message);
        }

        @Override
        public void onDisConnectSuccess(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, int status) {
            Message message = new Message();
            message.what = DISCONNECT_SUCCESS;
            message.obj = status;
            mHandler.sendMessage(message);
        }

        @Override
        public void onServiceDiscoverySucceed(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, int status) {
            //因为服务发现成功之后，才能通讯，所以在成功发现服务的地方表示连接成功
            Message message = new Message();
            message.what = CONNECT_SUCCESS;
            mHandler.sendMessage(message);
        }

        @Override
        public void onServiceDiscoveryFailed(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, String failMsg) {
            Message message = new Message();
            message.what = CONNECT_FAILURE;
            mHandler.sendMessage(message);
        }

        @Override
        public void onReceiveMessage(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, BluetoothGattCharacteristic characteristic, byte[] msg) {
            Log.d(TAG, characteristic.getUuid().toString());
            if (characteristic.getUuid().toString().equals(READ_UUID)) {

                Message message = new Message();
                message.what = RECEIVE_STEP;
                message.obj = msg;
                mHandler.sendMessage(message);
            }

            else if (characteristic.getUuid().toString().equals(READ_UUID_DATA)) {

                Message message = new Message();
                message.what = RECEIVE_DATA;
                message.obj = msg;
                mHandler.sendMessage(message);
            }
        }

        @Override
        public void onReceiveError(String errorMsg) {
            Message message = new Message();
            message.what = RECEIVE_FAILURE;
            mHandler.sendMessage(message);
        }

        @Override
        public void onReadMessage(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, BluetoothGattCharacteristic characteristic, byte[] msg) {
            Message message = new Message();
            message.what = READ_SUCCESS;
            message.obj = msg;
            mHandler.sendMessage(message);
        }

        @Override
        public void onWriteSuccess(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, byte[] msg) {
            Message message = new Message();
            message.what = SEND_SUCCESS;
            message.obj = msg;
            mHandler.sendMessage(message);
        }

        @Override
        public void onWriteFailure(BluetoothGatt bluetoothGatt, BluetoothDevice bluetoothDevice, byte[] msg, String errorMsg) {
            Message message = new Message();
            message.what = SEND_FAILURE;
            message.obj = msg;
            mHandler.sendMessage(message);
        }

        @Override
        public void onReadRssi(BluetoothGatt bluetoothGatt, int Rssi, int status) {

        }

        @Override
        public void onMTUSetSuccess(String successMTU, int newMtu) {

        }

        @Override
        public void onMTUSetFailure(String failMTU) {

        }
    };


    private class BLEBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, BluetoothAdapter.ACTION_DISCOVERY_STARTED)) { //开启搜索
                Message message = new Message();
                message.what = START_DISCOVERY;
                mHandler.sendMessage(message);

            } else if (TextUtils.equals(action, BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {//完成搜素
                Message message = new Message();
                message.what = STOP_DISCOVERY;
                mHandler.sendMessage(message);

            } else if(TextUtils.equals(action,BluetoothAdapter.ACTION_STATE_CHANGED)){   //系统蓝牙状态监听

                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,0);
                if(state == BluetoothAdapter.STATE_OFF){
                    Message message = new Message();
                    message.what = BT_CLOSED;
                    mHandler.sendMessage(message);

                }else if(state == BluetoothAdapter.STATE_ON){
                    Message message = new Message();
                    message.what = BT_OPENED;
                    mHandler.sendMessage(message);

                }
            }
        }
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
