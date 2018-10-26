package com.beiing.flikerprogressbar;

/*
 *    Copyright  2016 LineChen <15764230067@163.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author chenliu
 * @date 2016/8/26
 * 描述：Einsteinford修改源码 2018/10/22
 */

public class FlikerProgressBar extends View {

    private float maxProgress = 100f;

    private Paint textPaint;

    private Paint pgPaint;

    private String progressText;

    private Rect textRect;

    private RectF bgRectF;

    private Canvas pgCanvas;

    /**
     * 当前进度
     */
    private float progress;

    private boolean isFinish;

    private boolean isStop = true;

    /**
     * 下载中颜色
     */
    private int loadingColor;

    /**
     * 暂停时颜色
     */
    private int stopColor;

    /**
     * 进度文本、边框、进度条颜色
     */
    private int progressColor;

    /**
     * 未下载时的按钮背景色
     */
    private int bgColor;

    /**
     * 下载完成被选中的颜色
     */
    private int selectedColor;

    private int textSize;

    /**
     * 未下载时文本颜色
     */
    private int textColor;

    private int textTempColor;

    /**
     * 在进度条上的文本颜色
     */
    private int textLoadingColor;

    /**
     * 下载完成时的文本颜色
     */
    private int textSelectedColor;

    private int radius;


    public FlikerProgressBar(Context context) {
        this(context, null, 0);
    }

    public FlikerProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlikerProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs, defStyleAttr);
    }

    private void initAttrs(AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.FlikerProgressBar, defStyleAttr, 0);
        try {
            textSize = (int) ta.getDimension(R.styleable.FlikerProgressBar_textSize, getResources().getDimensionPixelSize(R.dimen.button_text_size_default));
            textColor = ta.getColor(R.styleable.FlikerProgressBar_textColor, Color.parseColor("#FFFFFF"));
            textLoadingColor = ta.getColor(R.styleable.FlikerProgressBar_textLoadingColor, Color.parseColor("#FFFFFF"));
            textSelectedColor = ta.getColor(R.styleable.FlikerProgressBar_textSelectedColor, Color.parseColor("#BBBBBB"));
            loadingColor = ta.getColor(R.styleable.FlikerProgressBar_loadingColor, Color.parseColor("#4FAAFF"));
            bgColor = ta.getColor(R.styleable.FlikerProgressBar_bgColor, Color.parseColor("#6D8CFF"));
            stopColor = ta.getColor(R.styleable.FlikerProgressBar_stopColor, Color.parseColor("#F88080"));
            selectedColor = ta.getColor(R.styleable.FlikerProgressBar_SelectedColor, Color.parseColor("#F0F0F0"));
            radius = (int) ta.getDimension(R.styleable.FlikerProgressBar_buttonRadius, 0);
        } finally {
            ta.recycle();
        }
    }

    private void init() {
        pgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pgPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textSize);

        textRect = new Rect();
        bgRectF = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());

        if (isStop) {
            if (isSelected()) {
                progressColor = selectedColor;
            } else {
                progressColor = stopColor;
            }
        } else {
            progressColor = loadingColor;
        }

        if (isSelected()) {
            textTempColor = textSelectedColor;
        } else {
            textTempColor = textLoadingColor;
        }

        initPgBitmap();
    }

    private void initPgBitmap() {
        if (pgPaint != null) {
            /**
             * 进度条 bitmap ，包含滑块
             */
            Bitmap pgBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            pgCanvas = new Canvas(pgBitmap);
            BitmapShader bitmapShader = new BitmapShader(pgBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            pgPaint.setShader(bitmapShader);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height, width;
        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int specHeight = MeasureSpec.getSize(heightMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                height = specHeight;
                break;
            default:
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                height = getResources().getDimensionPixelSize(R.dimen.button_default_height);
                break;
        }

        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                width = specWidth;
                break;
            default:
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                width = getResources().getDimensionPixelSize(R.dimen.button_default_width);
                break;
        }
        setMeasuredDimension(width, height);

        if (pgPaint == null || textPaint == null || textRect == null || bgRectF == null) {
            init();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //进度
        drawProgress(canvas);

        //进度text
        drawProgressText(canvas);

        //变色处理
        drawColorProgressText(canvas);
    }

    /**
     * 进度
     */
    private void drawProgress(Canvas canvas) {
        pgCanvas.save(Canvas.CLIP_SAVE_FLAG);
        pgCanvas.clipRect(0, 0, (progress / maxProgress) * getMeasuredWidth(), getMeasuredHeight());
        pgCanvas.drawColor(progressColor);
        pgCanvas.restore();

        pgCanvas.save(Canvas.CLIP_SAVE_FLAG);
        pgCanvas.clipRect((progress / maxProgress) * getMeasuredWidth(), 0, getMeasuredWidth(), getMeasuredHeight());
        pgCanvas.drawColor(bgColor);
        pgCanvas.restore();

        //控制显示区域
        canvas.drawRoundRect(bgRectF, radius, radius, pgPaint);
    }

    /**
     * 进度提示文本
     *
     * @param canvas
     */
    private void drawProgressText(Canvas canvas) {
        textPaint.setColor(textColor);
        progressText = getProgressText();
        textPaint.getTextBounds(progressText, 0, progressText.length(), textRect);
        int xPos = (canvas.getWidth() - textRect.width()) / 2;
        int textAdjust = (int) (textPaint.descent() + textPaint.ascent()) / 2;
        int yPos = ((canvas.getHeight() / 2) - textAdjust);
        canvas.drawText(progressText, xPos, yPos, textPaint);
    }

    /**
     * 变色处理
     *
     * @param canvas
     */
    private void drawColorProgressText(Canvas canvas) {
        textPaint.setColor(textTempColor);
        int xPos = (canvas.getWidth() - textRect.width()) / 2;
        int textAdjust = (int) (textPaint.descent() + textPaint.ascent()) / 2;
        int yPos = ((canvas.getHeight() / 2) - textAdjust);
        float progressWidth = (progress / maxProgress) * getMeasuredWidth();
        if (progressWidth > xPos) {
            canvas.save(Canvas.CLIP_SAVE_FLAG);
            float right = Math.min(progressWidth, xPos + textRect.width() * 1.1f);
            canvas.clipRect(xPos, 0, right, getMeasuredHeight());
            canvas.drawText(progressText, xPos, yPos, textPaint);
            canvas.restore();
        }
    }

    public void setProgress(float progress) {
        if (progress < maxProgress) {
            this.progress = progress;
            invalidate();
        } else {
            this.progress = maxProgress;
            finishLoad();
        }
    }

    public void setStop(boolean stop) {
        isStop = stop;
        if (isStop) {
            if (isSelected()) {
                progressColor = selectedColor;
            } else {
                progressColor = stopColor;
            }
        } else {
            progressColor = loadingColor;
        }
        invalidate();
    }

    public void finishLoad() {
        isFinish = true;
        setStop(true);
    }

    public void toggle() {
        if (!isFinish) {
            if (isStop) {
                setStop(false);
            } else {
                setStop(true);
            }
        }
    }

    /**
     * 重置
     */
    public void reset() {
        setStop(true);
        progress = 0;
        isFinish = false;
        isStop = true;
        progressColor = loadingColor;
        textTempColor = textLoadingColor;
        progressText = "";
        initPgBitmap();
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (isFinish && isStop) {
            if (selected) {
                progressColor = selectedColor;
                textTempColor = textSelectedColor;
            } else {
                progressColor = stopColor;
                textTempColor = textLoadingColor;
            }
        }
        invalidate();
    }

    public float getProgress() {
        return progress;
    }

    public boolean isStop() {
        return isStop;
    }

    public boolean isFinish() {
        return isFinish;
    }

    private String getProgressText() {
        String text = "";
        if (!isFinish) {
            if (!isStop) {
                text = progress + "%";
            } else if (progress != 0) {
                text = "继续";
            } else {
                text = "下载";
            }
        } else {
            if (isSelected()) {
                text = "使用中";
            } else {
                text = "使用";
            }
        }

        return text;
    }
}