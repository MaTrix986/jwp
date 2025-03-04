package matrix.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import matrix.bluetooth.util.SportService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "JWP" ;
    private static final int REQUEST_CODE = 0x00;
    private ListView lvService;
    private ServiceAdapter lvServiceAdapter;
    private List<SportService> serviceList;
    private static final int SELECT_SERVICE = 0x00;
    private static final int SERVICE_CONNECT = 0x00;
    private static final int SERVICE_RUN = 0x01;
    private static final int SERVICE_DATA = 0x02;

    BluetoothDevice device;


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch(msg.what){
                case SELECT_SERVICE:
                    int serviceID = (int) msg.obj;
                    Log.d(TAG, ""+serviceID);

                    startSportService(serviceID);
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        initView();
        initData();
        initListener();

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

    void initView() {
        lvService = (ListView) findViewById(R.id.lv_service);
    }

    private void initData() {

        SportService s;
        serviceList = new ArrayList<>();
        s = new SportService(R.drawable.ic_run, "连设备！", SERVICE_CONNECT);
        serviceList.add(s);
        s = new SportService(R.drawable.ic_data, "看数据！", SERVICE_DATA);
        serviceList.add(s);

        lvServiceAdapter = new ServiceAdapter(MainActivity.this, R.layout.service_item, serviceList);
        lvService.setAdapter(lvServiceAdapter);
    }

    void initListener() {
        lvService.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                int serviceID = serviceList.get(position).getServiceID() ;

                Message msg = new Message();
                msg.what = SELECT_SERVICE;
                msg.obj = serviceID;
                mHandler.sendMessage(msg);
            }
        });
    }

    private void startSportService(int serviceID){
        switch (serviceID) {
            case SERVICE_CONNECT:
                Intent ConnectIntent = new Intent(MainActivity.this,
                        ConnectActivity.class);
                ConnectIntent.putExtra("device", device);
                startActivityForResult(ConnectIntent, REQUEST_CODE);
                break;

            case SERVICE_DATA:
                Intent DataIntent = new Intent(MainActivity.this, DataActivity.class);
                DataIntent.putExtra("device", device);
                startActivityForResult(DataIntent, REQUEST_CODE);

                break;
        }
    }
}
