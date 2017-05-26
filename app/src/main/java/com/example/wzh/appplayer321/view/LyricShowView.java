package com.example.wzh.appplayer321.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;

import domain.Lyric;

/**
 * Created by WZH on 2017/5/26.
 */

public class LyricShowView extends TextView {
    private Paint paintGreen;
    private Paint paintWhite;
    private int width;
    private int height;
    private ArrayList<Lyric> lyrics;
    private int index = 0;
    private float textHeight = 20;
    private int currentPosition;
    public LyricShowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }


    private void initView() {
        paintGreen = new Paint();
        paintGreen.setColor(Color.GREEN);
        paintGreen.setTextSize(28);
        paintGreen.setTextAlign(Paint.Align.CENTER);


        paintWhite = new Paint();
        paintWhite.setColor(Color.WHITE);
        paintWhite.setTextSize(28);
        paintWhite.setTextAlign(Paint.Align.CENTER);

//准备歌词
        lyrics = new ArrayList<>();
        Lyric lyric = new Lyric();
        for (int i = 0; i < 10000; i++) {
            //不同歌词
            lyric.setContent("aaaaaaaaaaaa_" + i);
            lyric.setSleepTime(2000);
            lyric.setTimePoint(2000*i);
            //添加到集合
            lyrics.add(lyric);
            //重新创建新对象
            lyric = new Lyric();
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (lyrics != null && lyrics.size() > 0) {
            String currentContent = lyrics.get(index).getContent();
            canvas.drawText(currentContent, width / 2, height / 2, paintGreen);
            float tempY = height / 2;
            for (int i = index - 1; i >= 0; i--) {
                String preContent = lyrics.get(i).getContent();
                tempY = tempY - textHeight;
                if (tempY < 0) {
                    break;
                }
                //绘制内容
                canvas.drawText(preContent, width / 2, tempY, paintWhite);
            }
            tempY = height / 2;

            //绘制后面部分
            for (int i = index + 1; i < lyrics.size(); i++) {
                //得到后一部分多月的歌词内容
                String nextContent = lyrics.get(i).getContent();

                tempY = tempY + textHeight;
                if (tempY > height) {
                    break;
                }
                //绘制内容
                canvas.drawText(nextContent, width / 2, tempY, paintWhite);
            }
        } else {
            canvas.drawText("没有找到歌词", getWidth() / 2, getHeight() / 2, paintGreen);
        }
    }

    public void setNextShowLyric(int currentPosition) {
        this.currentPosition = currentPosition;
        if (lyrics == null || lyrics.size() == 0)
            return;
        for (int i = 1; i < lyrics.size(); i++) {
            if (currentPosition < lyrics.get(i).getTimePoint()) {
                int tempIndex = i - 1;
                if (currentPosition >= lyrics.get(tempIndex).getTimePoint()) {
                    //中间高亮显示的哪一句
                    index = tempIndex;
                }
            }

        }
        invalidate();
    }
}
