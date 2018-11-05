package com.tan.iotwfirebase;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.tan.iotwfirebase.helper.ChildConstants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by tanli on 1/7/2018.
 */

public class MyMarkerView extends MarkerView implements ChildConstants {

    private TextView tvContent;
    private long referenceTimestamp;  // minimum timestamp in your data set
    private DateFormat mDataFormat;
    private Date mDate;
    private String child;
    private String temp;

    public MyMarkerView (Context context, int layoutResource, long referenceTimestamp, String child) {
        super(context, layoutResource);
        // this markerview only displays a textview

        tvContent = (TextView) findViewById(R.id.tvContent);
        this.referenceTimestamp = referenceTimestamp;
        this.mDataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        this.mDate = new Date();
        this.child = child;
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        long currentTimestamp = (int)e.getX() + referenceTimestamp;

        switch (child) {
            case TEMPCILD:
                temp = "°C";
                break;

            case HUMICHILD:
                temp = " %";
                break;

            case PM25CHILD:
                temp = " mg/m^3";
                break;

            case CO2CHILD:
                temp = " ppm";
                break;

            case VOCCHILD:
                temp = " ppb";
                break;

            case CO2eCHILD:
                temp = " ppm";
                break;


        }

        String text = e.getY() +  temp +" at " + getTimedate(currentTimestamp);
        Log.d("Iot", "refreshContent: " + e.getY());
        Log.d("Iot", "refreshContent: " + getTimedate(currentTimestamp));

        tvContent.setText(text); // set the entry-value as the display text

        super.refreshContent(e, highlight);
    }

    private MPPointF mOffset;

    @Override
    public MPPointF getOffset() {

        if(mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
        }

        return mOffset;
    }

    public int getXOffset() {
        return -(getWidth() / 2);
    }

    public int getYOffset() {
        return -getHeight();
    }

    @Override
    public void draw(Canvas canvas, float posx, float posy)
    {
        // take offsets into consideration
        posx += getXOffset();
        posy=0;

        // AVOID OFFSCREEN
        if(posx<45)
            posx=45;
        if(posx>265)
            posx=265;

        // translate to the correct position and draw
        canvas.translate(posx, posy);
        draw(canvas);
        canvas.translate(-posx, -posy);
    }


    private String getTimedate(long timestamp){

        try{
            mDate.setTime(timestamp*1000);
            return mDataFormat.format(mDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }
}