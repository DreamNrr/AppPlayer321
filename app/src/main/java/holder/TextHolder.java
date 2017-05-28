package holder;

import android.view.View;
import android.widget.TextView;

import com.example.wzh.appplayer321.R;
import com.example.wzh.appplayer321.view.BaseViewHolder;

import domain.NetAudioBean;

/**
 * Created by WZH on 2017/5/27.
 */
public class TextHolder extends BaseViewHolder{
    TextView tvContext;

    public TextHolder(View convertView) {
        super(convertView);
        //中间公共部分 -所有的都有
        tvContext = (TextView) convertView.findViewById(R.id.tv_context);


    }

    public void setData(NetAudioBean.ListBean mediaItem) {
        super.setData(mediaItem);
        //设置文本-所有的都有
        tvContext.setText(mediaItem.getText() + "_" + mediaItem.getType());
    }
}
