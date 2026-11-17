package com.example.mytripmatee.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NenBongBongView extends View {

    private static class BongBongSang {
        float x, y;
        float vx, vy;
        float banKinh;
        float alphaCoBan;
        float tocDoMach;
        float phaMach;
        int mauSac;
        final int[] mauSacShader = new int[2];
        final float[] diemDungShader = new float[]{0.0f, 1.0f};
    }

    private final List<BongBongSang> danhSachBongBong = new ArrayList<>();
    private final Paint coBongBong = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean dangChay = false;
    private long thoiGianCuoi = 0;
    private final Random ngauNhien = new Random();

    public NenBongBongView(Context context) {
        super(context);
        khoiTao();
    }

    public NenBongBongView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        khoiTao();
    }

    public NenBongBongView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        khoiTao();
    }

    private void khoiTao() {
        setWillNotDraw(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            khoiTaoBongBong(w, h);
        }
    }

    private boolean laCheDoToi() {
        int nightModeFlags = getContext().getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    private void khoiTaoBongBong(int width, int height) {
        danhSachBongBong.clear();
        boolean dark = laCheDoToi();

        int[] mauSacGlow = dark ? new int[]{
                Color.parseColor("#0D9488"),
                Color.parseColor("#10B981"),
                Color.parseColor("#38BDF8"),
                Color.parseColor("#14B8A6"),
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#059669"),
                Color.parseColor("#0EA5E9")
        } : new int[]{
                Color.parseColor("#0D9488"),
                Color.parseColor("#059669"),
                Color.parseColor("#0284C7"),
                Color.parseColor("#10B981"),
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#14B8A6"),
                Color.parseColor("#0EA5E9")
        };

        float minRadius = Math.min(width, height) * 0.38f;
        float maxRadius = Math.max(width, height) * 0.58f;

        for (int color : mauSacGlow) {
            BongBongSang b = new BongBongSang();

            b.x = ngauNhien.nextFloat() * width;
            b.y = ngauNhien.nextFloat() * height;

            double angle = ngauNhien.nextFloat() * 2 * Math.PI;
            float speed = 110f + ngauNhien.nextFloat() * 120f;
            b.vx = (float) (Math.cos(angle) * speed);
            b.vy = (float) (Math.sin(angle) * speed);

            b.banKinh = minRadius + ngauNhien.nextFloat() * (maxRadius - minRadius);
            b.tocDoMach = 0.5f + ngauNhien.nextFloat() * 0.7f;
            b.phaMach = ngauNhien.nextFloat() * (float) (2 * Math.PI);
            b.mauSac = color;

            if (b.mauSac == Color.parseColor("#FFFFFF")) {
                b.alphaCoBan = dark ? 0.10f : 0.22f;
            } else if (b.mauSac == Color.parseColor("#38BDF8") || b.mauSac == Color.parseColor("#0EA5E9")
                    || b.mauSac == Color.parseColor("#0284C7") || b.mauSac == Color.parseColor("#7DD3FC")) {
                b.alphaCoBan = dark ? 0.14f : 0.24f;
            } else {
                b.alphaCoBan = dark ? 0.18f : 0.28f;
            }

            danhSachBongBong.add(b);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        boolean dark = laCheDoToi();
        int baseBgColor = dark ? Color.parseColor("#0F172A") : Color.parseColor("#F0FDF4");
        canvas.drawColor(baseBgColor);

        long now = System.currentTimeMillis();
        float delta = (thoiGianCuoi == 0) ? 0.016f : Math.min((now - thoiGianCuoi) / 1000f, 0.05f);
        thoiGianCuoi = now;

        for (BongBongSang b : danhSachBongBong) {
            if (dangChay) {
                b.x += b.vx * delta;
                b.y += b.vy * delta;

                if (b.x < -b.banKinh) {
                    b.x = width + b.banKinh;
                } else if (b.x > width + b.banKinh) {
                    b.x = -b.banKinh;
                }

                if (b.y < -b.banKinh) {
                    b.y = height + b.banKinh;
                } else if (b.y > height + b.banKinh) {
                    b.y = -b.banKinh;
                }

                b.phaMach += b.tocDoMach * delta;
            }

            float currentAlpha = b.alphaCoBan + (float) Math.sin(b.phaMach) * 0.04f;
            float maxAlpha = dark ? 0.32f : 0.38f;
            currentAlpha = Math.max(0.04f, Math.min(maxAlpha, currentAlpha));

            int alphaInt = (int) (currentAlpha * 255);
            b.mauSacShader[0] = (b.mauSac & 0x00FFFFFF) | (alphaInt << 24);
            b.mauSacShader[1] = (b.mauSac & 0x00FFFFFF);

            RadialGradient shader = new RadialGradient(
                    b.x, b.y, b.banKinh,
                    b.mauSacShader,
                    b.diemDungShader,
                    Shader.TileMode.CLAMP
            );

            coBongBong.setShader(shader);
            canvas.drawCircle(b.x, b.y, b.banKinh, coBongBong);
        }

        if (dangChay) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        dangChay = true;
        thoiGianCuoi = System.currentTimeMillis();
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        dangChay = false;
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            dangChay = true;
            thoiGianCuoi = System.currentTimeMillis();
            invalidate();
        } else {
            dangChay = false;
        }
    }
}
