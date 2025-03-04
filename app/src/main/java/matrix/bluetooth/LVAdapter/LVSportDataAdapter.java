package matrix.bluetooth.LVAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import matrix.bluetooth.R;
import matrix.bluetooth.sportdb.SportData;

public class LVSportDataAdapter extends BaseAdapter {

    private Context context;
    private List<SportData> list;

    public LVSportDataAdapter(Context context) {
        this.context = context;
        list = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return list == null ?  0 : list.size();
    }

    @Override
    public Object getItem(int i) {
        if(list == null){
            return null;
        }
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        DeviceViewHolder viewHolder;
        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.layout_lv_sportdata_item,null);
            viewHolder = new DeviceViewHolder();
            viewHolder.tvDate = view.findViewById(R.id.tv_date);
            viewHolder.tvDuration = view.findViewById(R.id.tv_duration);
            viewHolder.tvStepCount = view.findViewById(R.id.tv_step_count);
            view.setTag(viewHolder);
        }else{
            viewHolder = (DeviceViewHolder) view.getTag();
        }

        if (list != null){
            viewHolder.tvDate.setText("日期: " + list.get(i).getDateFormat());
            viewHolder.tvDuration.setText("跑步时间: " + list.get(i).getDuration().getFormat());
            viewHolder.tvStepCount.setText("步数：" + list.get(i).getStepCount());
        }
        else {

        }

        return view;
    }

    /**
     * 初始化所有设备列表
     * @param sportData
     */
    public void addAllDevice(List<SportData> sportData){
        if(list != null){
            list.clear();
            list.addAll(sportData);
            notifyDataSetChanged();
        }

    }


    /**
     * 添加列表子项
     * @param sportData
     */
    public void addDevice(SportData sportData){
        if(list == null){
            return;
        }
        for (SportData sportData1: list) {
            if (sportData1.getDateFormat().equals(sportData.getDateFormat()) && sportData1.getDuration() == sportData.getDuration()) {
                return;
            }
        }
        list.add(sportData);

        notifyDataSetChanged();   //刷新
    }

    /**
     * 清空列表
     */
    public void clear(){
        if(list != null){
            list.clear();
        }
        notifyDataSetChanged(); //刷新
    }

    public void notifyChanged(){
        notifyDataSetChanged();
    }

    class DeviceViewHolder {

        TextView tvDate;
        TextView tvDuration;
        TextView tvStepCount;
    }

}
