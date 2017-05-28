package holder;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wzh.appplayer321.R;

/**
 * Created by WZH on 2017/5/27.
 */

public class ADHolder {
    TextView tvContext;
    ImageView ivImageIcon;
    Button btnInstall;

    public ADHolder(View convertView) {
        //中间公共部分 -所有的都有
        tvContext = (TextView) convertView.findViewById(R.id.tv_context);
        btnInstall = (Button) convertView.findViewById(R.id.btn_install);
        ivImageIcon = (ImageView) convertView.findViewById(R.id.iv_image_icon);
    }
}
