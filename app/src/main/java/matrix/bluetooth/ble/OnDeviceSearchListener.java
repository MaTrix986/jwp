package matrix.bluetooth.ble;


/**
 * 蓝牙设备搜索监听者
 * 1、开启搜索
 * 2、完成搜索
 * 3、搜索到设备
 */
public interface OnDeviceSearchListener {


    void onDiscoveryStart();
    void onDeviceFound(BLEDevice bleDevice);  //搜索到设备
    void onDiscoveryOutTime(); //扫描超时


}
