package matrix.bluetooth;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import matrix.bluetooth.util.SportService;

public class ServiceAdapter extends ArrayAdapter<SportService> {
    private static final String TAG = "JWP" ;
    public ServiceAdapter(@NonNull Context context, int resource, @NonNull List<SportService> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        SportService sportService = getItem(position);
        Log.e(TAG, "" + position);
        View view;

        ViewHolder viewHolder;
        if (convertView == null) {

            if (position == 0) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.service_item_header,parent,false);
            }
            else {
                view = LayoutInflater.from(getContext()).inflate(R.layout.service_item,parent,false);
            }

            viewHolder = new ViewHolder();

            viewHolder.serviceImage = view.findViewById(R.id.service_image);
            viewHolder.serviceName = view.findViewById(R.id.service_name);
            view.setTag(viewHolder);
        }
        else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.serviceImage.setImageResource(sportService.getImageID());
        viewHolder.serviceName.setText(sportService.getServiceName());
        return view;
    }

    private class ViewHolder {
        ImageView serviceImage;
        TextView serviceName;
    }

}
