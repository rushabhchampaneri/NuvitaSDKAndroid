package com.ble.healthmonitoringapp.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.text.TextUtils;

import com.jstyle.blesdk2025.model.UserInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PDFCreate {
    private static final String TAG = "PDFCreate";
    static final int maxXSize = 5120;
    static float length = 20.0F;
    static float totalWidth = 1070.0F;
    static int startX = 35;
    static float endX;
    static float startY;
    private static float endY;
    private static float height;
    private static float width;
    private static int strokeWidthTime;
    private static int strokeWidthLine;
    private static float totalHeight;

    public PDFCreate() {
    }

    public static void createPdf(String path, Context context, List<Integer> data, UserInfo userInfo) {
        int size = data.size();
        float col = size % 5120 == 0 ? (float)(size / 5120) : (float)(size / 5120 + 1);
        length = (float)dip2px(context, 10.0F);
        height = length;
        width = length;
        startX = dip2px(context, 20.0F);
        endX = (float)startX + 50.0F * length;
        totalWidth = endX + (float)startX;
        totalHeight = col * height * 5.0F + (float)dip2px(context, 130.0F);
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = (new PdfDocument.PageInfo.Builder((int)totalWidth, (int)totalHeight, 1)).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        strokeWidthTime = dip2px(context, 1.5F);
        strokeWidthLine = dip2px(context, 0.5F);
        drawReportInfo(context, canvas, paint, userInfo);
        Path pathCanvas = new Path();
        endY = startY + col * height * 5.0F;
        paint.setTextSize((float)dip2px(context, 15.0F));
        drawAxes(pathCanvas, canvas, paint, col);
        drawTimeLine(pathCanvas, canvas, paint);
        drawDataLine(context, pathCanvas, canvas, paint, col, data);
        pdfDocument.finishPage(page);
        File file = new File(path);
        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException var16) {
            var16.printStackTrace();
        }

        try {
            pdfDocument.writeTo(outputStream);
        } catch (IOException var15) {
            var15.printStackTrace();
        }

    }

    private static void drawReportInfo(Context context, Canvas canvas, Paint paint, UserInfo userInfo) {
        paint.setTextSize((float)dip2px(context, 15.0F));
        paint.setColor(-16777216);
        paint.setStyle(Paint.Style.FILL);
        if (null != userInfo && !TextUtils.isEmpty(userInfo.getEcgTitle())) {
            String title = userInfo.getEcgTitle();
            Rect rect = new Rect();
            paint.getTextBounds(title, 0, title.length(), rect);
            int heightTitle = rect.height();
            float startTextY = (float)(heightTitle + dip2px(context, 8.0F));
            float marginTop = (float)dip2px(context, 4.0F);
            float marginRight = (float)dip2px(context, 8.0F);
            canvas.drawText(title, totalWidth / 2.0F - 150.0F, startTextY, paint);
            paint.setTextSize((float)dip2px(context, 12.0F));
            int widthTips = rect.width();
            int heightTips = rect.height();
            if (!TextUtils.isEmpty(userInfo.getEcgReportTips())) {
                String tips = userInfo.getEcgReportTips();
                float tipsTextY = startTextY + (float)heightTitle + marginTop;
                paint.getTextBounds(tips, 0, tips.length(), rect);
                canvas.drawText(tips, totalWidth - (float)widthTips - marginRight, tipsTextY, paint);
            }

            float tipsTextY = startTextY + (float)heightTitle + marginTop;
            String name = userInfo.getName();
            String genderString = userInfo.getGender();
            String age = userInfo.getAge();
            String userWeight = userInfo.getWeight();
            String userHeight = userInfo.getHeight();
            float infoTextY = tipsTextY + (float)heightTips + marginTop;
            String info = name + "  " + genderString + "  " + age + "  " + userHeight + "  " + userWeight;
            paint.getTextBounds(info, 0, info.length(), rect);
            int heightInfo = rect.height();
            int widthInfo = rect.width();
            canvas.drawText(info, totalWidth / 2.0F - (float)(widthInfo >> 1), infoTextY, paint);
            float dateTextY = infoTextY + (float)heightInfo + marginTop;
            String dateString = userInfo.getDate();
            paint.getTextBounds(dateString, 0, dateString.length(), rect);
            int widthDate = rect.width();
            int heightDate = rect.height();
            canvas.drawText(dateString, totalWidth - (float)widthDate - marginRight, dateTextY, paint);
            startY = dateTextY + (float)heightDate + marginTop;
        }

    }

    private static void drawAxes(Path pathCanvas, Canvas canvas, Paint paint, float col) {
        int colorLine = Color.rgb(243, 119, 99);
        paint.setColor(-16777216);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float)strokeWidthLine);
        paint.setAlpha(200);

        int i;
        for(i = 0; i < 51; ++i) {
            pathCanvas.moveTo((float)i * width + (float)startX, startY);
            pathCanvas.lineTo((float)i * width + (float)startX, endY);
        }

        for(i = 0; (float)i <= col * 5.0F; ++i) {
            pathCanvas.moveTo((float)(startX - (strokeWidthTime >> 1)), (float)i * height + startY);
            pathCanvas.lineTo(endX + (float)(strokeWidthTime >> 1), (float)i * height + startY);
        }

        canvas.drawPath(pathCanvas, paint);
        pathCanvas.reset();
    }

    private static void drawTimeLine(Path pathCanvas, Canvas canvas, Paint paint) {
        paint.setColor(-16777216);
        paint.setStrokeWidth((float)strokeWidthTime);
        paint.setStyle(Paint.Style.FILL);
        Rect rect = new Rect();

        int colorS;
        for(colorS = 0; colorS < 11; ++colorS) {
            pathCanvas.moveTo((float)(colorS * 5) * width + (float)startX, startY);
            pathCanvas.lineTo((float)(colorS * 5) * width + (float)startX, endY + 20.0F);
            String time = colorS + "s";
            paint.getTextBounds(time, 0, time.length(), rect);
            int widthTime = rect.width();
            canvas.drawText(time, (float)(colorS * 5) * width + (float)startX - (float)(widthTime >> 1), endY + 50.0F, paint);
        }

        colorS = Color.rgb(255, 119, 99);
        paint.setColor(-16777216);
        paint.setAlpha(255);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(pathCanvas, paint);
        pathCanvas.reset();
    }

    private static void drawDataLine(Context context, Path pathCanvas, Canvas canvas, Paint paint, float col, List<Integer> data) {
        int size = data.size();
        paint.setStrokeWidth((float)strokeWidthLine);
        paint.setColor(Color.RED);

        label26:
        for(int i = 0; (float)i < col; ++i) {
            int startPoint = i * 5120;

            for(int j = 0; j < 5120; ++j) {
                if (startPoint + j >= size) {
                    break label26;
                }

                Integer y = (Integer)data.get(startPoint + j);
                if (j == 0) {
                    pathCanvas.moveTo((float)startX, (float)i * height * 5.0F + getCanvasY((double)y, context));
                }

                pathCanvas.lineTo((float)startX + (float)j * (endX - (float)startX) / 5120.0F, (float)i * height * 5.0F + getCanvasY((double)y, context));
            }
        }

        canvas.drawPath(pathCanvas, paint);
    }

    private static float getCanvasY(double value, Context context) {
        double height = 0.0031250000465661287D;
        return (float)dip2px(context, (float)(height * (8000.0D - value))) + startY;
    }

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5F);
    }

    static {
        endX = (float)(startX + 1000);
    }
}

