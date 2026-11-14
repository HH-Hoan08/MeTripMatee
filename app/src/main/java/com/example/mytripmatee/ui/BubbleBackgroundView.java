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

/**
 * Animated ambient light bubble background.
 * Features dynamic floating motion across the screen with random trajectories.
 * Dominant Green/Teal colors with soft Ocean Blue and White light accents.
 */
public class BubbleBackgroundView extends View {

    private static class LightBubble {
        float x, y;
        float vx, vy;
        float radius;
        float baseAlpha;
        float pulseSpeed;
        float pulsePhase;
        int color;
        final int[] shaderColors = new int[2];
        final float[] shaderStops = new float[]{0.0f, 1.0f};
    }

    private final List<LightBubble> bubbles = new ArrayList<>();
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean isRunning = false;
    private long lastTime = 0;
    private final Random random = new Random();

    public BubbleBackgroundView(Context context) {
        super(context);
        init();
    }

    public BubbleBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BubbleBackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            initBubbles(w, h);
        }
    }

    private boolean isDarkMode() {
        int nightModeFlags = getContext().getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    private void initBubbles(int width, int height) {
        bubbles.clear();

        boolean dark = isDarkMode();

        // Distinct colors for Light vs Dark mode to ensure clear visibility in Light mode
        int[] glowColors = dark ? new int[]{
                Color.parseColor("#0D9488"), // Teal (Main Dominant)
                Color.parseColor("#10B981"), // Emerald Green (Main Dominant)
                Color.parseColor("#38BDF8"), // Ocean Sky Blue (Accent Highlight)
                Color.parseColor("#14B8A6"), // Teal 500 (Main Dominant)
                Color.parseColor("#FFFFFF"), // Soft White Light (Accent Highlight)
                Color.parseColor("#059669"), // Deep Emerald (Main Dominant)
                Color.parseColor("#0EA5E9")  // Deep Sky Blue (Accent Highlight)
        } : new int[]{
                Color.parseColor("#0D9488"), // Rich Teal (Main Dominant)
                Color.parseColor("#059669"), // Rich Emerald Green (Main Dominant)
                Color.parseColor("#0284C7"), // Vibrant Ocean Blue (Accent Highlight)
                Color.parseColor("#10B981"), // Emerald Green (Main Dominant)
                Color.parseColor("#FFFFFF"), // Pure White Glow (Accent Highlight)
                Color.parseColor("#14B8A6"), // Teal 500 (Main Dominant)
                Color.parseColor("#0EA5E9")  // Sky Blue (Accent Highlight)
        };

        float minRadius = Math.min(width, height) * 0.38f;
        float maxRadius = Math.max(width, height) * 0.58f;

        for (int color : glowColors) {
            LightBubble b = new LightBubble();

            // Random initial position anywhere on the screen
            b.x = random.nextFloat() * width;
            b.y = random.nextFloat() * height;

            // Faster speed (110 - 230 px/sec) and random floating direction angles
            double angle = random.nextFloat() * 2 * Math.PI;
            float speed = 110f + random.nextFloat() * 120f;
            b.vx = (float) (Math.cos(angle) * speed);
            b.vy = (float) (Math.sin(angle) * speed);

            b.radius = minRadius + random.nextFloat() * (maxRadius - minRadius);
            b.pulseSpeed = 0.5f + random.nextFloat() * 0.7f;
            b.pulsePhase = random.nextFloat() * (float) (2 * Math.PI);
            b.color = color;

            // Higher alpha contrast for Light Mode so effect is clearly visible on light backgrounds
            if (b.color == Color.parseColor("#FFFFFF")) {
                b.baseAlpha = dark ? 0.10f : 0.22f; // Soft white glow
            } else if (b.color == Color.parseColor("#38BDF8") || b.color == Color.parseColor("#0EA5E9")
                    || b.color == Color.parseColor("#0284C7") || b.color == Color.parseColor("#7DD3FC")) {
                b.baseAlpha = dark ? 0.14f : 0.24f; // Ocean blue accent
            } else {
                b.baseAlpha = dark ? 0.18f : 0.28f; // Main Green/Teal dominant glow
            }

            bubbles.add(b);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        boolean dark = isDarkMode();

        // Base soothing dark/light background (Soft Mint tint in Light Mode for high contrast glow)
        int baseBgColor = dark ? Color.parseColor("#0F172A") : Color.parseColor("#F0FDF4");
        canvas.drawColor(baseBgColor);

        long now = System.currentTimeMillis();
        float delta = (lastTime == 0) ? 0.016f : Math.min((now - lastTime) / 1000f, 0.05f);
        lastTime = now;

        // Render ambient floating light bubbles
        for (LightBubble b : bubbles) {
            if (isRunning) {
                // Smooth position update
                b.x += b.vx * delta;
                b.y += b.vy * delta;

                // Seamless screen wrapping so bubbles continuously float all around the screen
                if (b.x < -b.radius) {
                    b.x = width + b.radius;
                } else if (b.x > width + b.radius) {
                    b.x = -b.radius;
                }

                if (b.y < -b.radius) {
                    b.y = height + b.radius;
                } else if (b.y > height + b.radius) {
                    b.y = -b.radius;
                }

                // Smooth pulse phase
                b.pulsePhase += b.pulseSpeed * delta;
            }

            // Calculate current alpha opacity
            float currentAlpha = b.baseAlpha + (float) Math.sin(b.pulsePhase) * 0.04f;
            float maxAlpha = dark ? 0.32f : 0.38f;
            currentAlpha = Math.max(0.04f, Math.min(maxAlpha, currentAlpha));

            int alphaInt = (int) (currentAlpha * 255);
            b.shaderColors[0] = (b.color & 0x00FFFFFF) | (alphaInt << 24);
            b.shaderColors[1] = (b.color & 0x00FFFFFF); // Fully transparent edge

            RadialGradient shader = new RadialGradient(
                    b.x, b.y, b.radius,
                    b.shaderColors,
                    b.shaderStops,
                    Shader.TileMode.CLAMP
            );

            bubblePaint.setShader(shader);
            canvas.drawCircle(b.x, b.y, b.radius, bubblePaint);
        }

        if (isRunning) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isRunning = true;
        lastTime = System.currentTimeMillis();
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isRunning = false;
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            isRunning = true;
            lastTime = System.currentTimeMillis();
            invalidate();
        } else {
            isRunning = false;
        }
    }
}