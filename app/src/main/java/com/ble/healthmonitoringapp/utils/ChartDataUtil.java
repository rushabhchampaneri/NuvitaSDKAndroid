package com.ble.healthmonitoringapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.ble.healthmonitoringapp.R;
import java.util.ArrayList;
import java.util.List;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.AbstractChartView;
import lecho.lib.hellocharts.view.LineChartView;



public class ChartDataUtil {
    private static final String TAG = "ChartDataUtil";


//    public static void initChartView(AbstractChartView chart, float top, float bottom, float left, float right) {
//        Viewport viewport = chart.getCurrentViewport();
//        viewport.top = top;
//        viewport.bottom = bottom;
//        viewport.left = left;
//        viewport.right = right;
//        chart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL_IN_ScrollView);
//        chart.setViewportCalculationEnabled(false);
//        chart.setMaximumViewport(viewport);
//        chart.setZoomType(ZoomType.HORIZONTAL);
//        chart.setCurrentViewport(viewport, false);
//        // chart.setZoomEnabled(false);
//    }

    public static void initDataChartView(AbstractChartView chart, float left, float top, float right, float bottom) {
        Viewport viewport = chart.getCurrentViewport();
        viewport.top = top;
        viewport.bottom = bottom;
        viewport.left = left;
        viewport.right = right;
        chart.setScrollEnabled(true);
        chart.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);
        chart.setViewportCalculationEnabled(false);
        chart.setMaximumViewport(viewport);
        chart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        chart.setCurrentViewport(viewport);
    }











    public static LineChartData getEcgLineChartData(Context context, List<Integer> queue, int color) {
        LineChartData lineChartData = new LineChartData();
        List<Line> listLines = new ArrayList<>();
        Line line = new Line();
        List<PointValue> listPoint = new ArrayList<>();
        List<AxisValue> axisValues = new ArrayList<>();
        Axis axisX = new Axis();
        for (int i = 0; i < 4; i++) {
            axisValues.add(new AxisValue(i * 512).setLabel(String.valueOf(i  + "s").toCharArray()));
        }
        axisX.setValues(axisValues);
        for (int i = 0; i < queue.size(); i++) {
            int data = queue.get(i);
            PointValue pointValue = new PointValue();
            pointValue.set(i, data);
            listPoint.add(pointValue);
        }
        line.setValues(listPoint);
        line.setColor(color);
        line.setCubic(true);
        line.setStrokeWidth(2);
        line.setHasPoints(false);
        listLines.add(line);
        lineChartData.setLines(listLines);
        //Bitmap bitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.bg_ecg_h);
       // lineChartData.(bitmap);

        lineChartData.setAxisXBottom(axisX.setHasLines(false));
        return lineChartData;
    }



    public static LineChartData getEcgLineChartData(Context context, List<Double> queue, int color, int second, int index) {
        LineChartData lineChartData = new LineChartData();
        List<Line> listLines = new ArrayList<>();
        Line line = new Line();
        List<PointValue> listPoint = new ArrayList<>();
        List<AxisValue> axisValues = new ArrayList<>();
        Axis axisX = new Axis();
        for (int i = 0; i < second; i++) {//512为1秒，480为更好显示
            axisValues.add(new AxisValue(i * 480+50).setLabel(String.valueOf(i + "s").toCharArray()));
        }
        axisX.setValues(axisValues);
        axisX.setTextSize(14);
        int size = queue.size();
        int maxSize = (second - 1) * 512;
        for (int i = 0; i < maxSize; i++) {
            if (i + index >= size) break;
            Double data = queue.get(i + index);
            PointValue pointValue = new PointValue();
            if (data != null) {
                double v=data;
                pointValue.set(i, (float) v);
                listPoint.add(pointValue);
            }

        }
        line.setValues(listPoint);
        line.setColor(color);
        line.setCubic(true);
        line.setStrokeWidth(1);
        line.setHasPoints(false);
        listLines.add(line);
        lineChartData.setLines(listLines);
        lineChartData.setAxisXBottom(axisX.setHasLines(false));
        return lineChartData;
    }



    public static LineChartData getPpgLineChartData(Context context, List<Integer> queue, int color) {
        LineChartData lineChartData = new LineChartData();
        List<Line> listLines = new ArrayList<>();
        Line line = new Line();
        List<PointValue> listPoint = new ArrayList<>();
        List<AxisValue> axisValues = new ArrayList<>();
        Axis axisX = new Axis();
        for (int i = 0; i < 7; i++) {
            axisValues.add(new AxisValue(i * 200).setLabel(String.valueOf(i  + "s").toCharArray()));
        }
        axisX.setValues(axisValues);
        for (int i = 0; i < queue.size(); i++) {
            int data = queue.get(i);
            PointValue pointValue = new PointValue();
            pointValue.set(i, data);
            listPoint.add(pointValue);
        }
        line.setValues(listPoint);
        line.setColor(color);
        line.setCubic(true);
        line.setStrokeWidth(1);
        line.setHasPoints(false);
        listLines.add(line);
        lineChartData.setLines(listLines);
       // Bitmap bitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.bg_ecg_h);
      //  lineChartData.setBitmap(bitmap);
        lineChartData.setAxisXBottom(axisX.setHasLines(false));
        return lineChartData;
    }



    public static long showMills;
    public static LineChartData getPpgChartData(LineChartView lineChartView, Context context, List<Float> queue, int color) {
        LineChartData lineChartData = new LineChartData();
        List<Line> listLines = new ArrayList<>();
        Line line = new Line();
        List<PointValue> listPoint = new ArrayList<>();
        List<AxisValue> axisValues = new ArrayList<>();
        Axis axisX = new Axis();
        int xSize = queue.size() / 200;
        for (int i = 0; i <=xSize; i++) {
            axisValues.add(new AxisValue(i * 188+20).setLabel(String.valueOf(i + "s").toCharArray()));
        }
        axisX.setValues(axisValues);
        axisX.setTextSize(14);
        double[]result=getPPgData(queue);
        float maxValue=0;
        float minValue=10000;
        for (int i = 0; i < result.length; i++) {
            float data = (float) result[i];
            maxValue=Math.max(maxValue,data);
            minValue=Math.min(minValue,data);
            PointValue pointValue = new PointValue();
            pointValue.set(i, data);
            listPoint.add(pointValue);
        }
        float top=0;
        float bottom=0;
        if(maxValue!=0){
            Log.i(TAG, "getPpgChartData: "+maxValue+" "+minValue);
            if(maxValue-minValue<40){
                float offset=(50-(maxValue-minValue))/2;
                top=maxValue+offset;
                bottom=minValue-offset;
                if(maxValue-minValue<10){
                    Log.i(TAG, "getPpgChartData: 按压太紧或者漏光");
                    if(System.currentTimeMillis()-showMills>4000) {
                        //   ScreenUtils.showSetSuccessFul(lineChartView,context.getResources().getString(R.string.ppgTips));
                        showMills=System.currentTimeMillis();
                    }

                }
            }else{
                top=maxValue*1.25f;
                bottom=minValue*1.25f;
            }
        }
        Log.i(TAG, "getPpgChartData: top "+top+" "+bottom);
        initDataChartView(lineChartView, 0, top, queue.size(), bottom);

        line.setValues(listPoint);
        line.setColor(color);
        line.setCubic(true);
        line.setStrokeWidth(1);
        line.setHasPoints(false);
        listLines.add(line);
        lineChartData.setLines(listLines);

        lineChartData.setAxisXBottom(axisX.setHasLines(false));
        return lineChartData;
    }


    public synchronized static double[] getPPgData(List<Float>value) {
        Float[]source=value.toArray(new Float[value.size()]);
        for(int i=4;i<source.length;i++){
            source[i]=(source[i]+source[i-1]+source[i-2]+source[i-3]+source[i-4])/5;
        }
        if(source.length<4)return new double[4];
        Float[]avgSource=new Float[source.length-4];
        System.arraycopy(source,4,avgSource,0,avgSource.length);
        double[]s_proc=new double[avgSource.length];
        for(int i=4;i<s_proc.length;i++){
            s_proc[i]=0.0001*IIR8_B[0] * avgSource[i] +
                    0.0001*IIR8_B[1] * avgSource[i-1] +
                    0.0001*IIR8_B[2] * avgSource[i-2]+
                    0.0001*IIR8_B[3] * avgSource[i-3] +
                    0.0001*IIR8_B[4] * avgSource[i-4] -
                    IIR8_A[1] * s_proc[i-1] -
                    IIR8_A[2] * s_proc[i-2]-
                    IIR8_A[3] * s_proc[i-3] -
                    IIR8_A[4] * s_proc[i-4];
        }
        double[]result=new double[s_proc.length-4];
        System.arraycopy(s_proc,4,result,0,result.length);

        return s_proc;
    }



    static double[] tmp_x = new double[5];
    static double[] tmp_y = new double[5];
    static double IIR8_B[] = {
            2.740724471807023,0,-5.481448943614046,0, 2.740724471807023
    };
    static double IIR8_A[] = {
            1.0000,-3.966563683749257,5.903228691653986 ,-3.906734871440351,0.970070822869830
    };
}
