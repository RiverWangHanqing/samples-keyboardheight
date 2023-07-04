/*
 * This file is part of Siebe Projects samples.
 *
 * Siebe Projects samples is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Siebe Projects samples is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public License
 * along with Siebe Projects samples.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.siebeprojects.keyboardheight;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;


/**
 * The keyboard height provider, this class uses a PopupWindow
 * to calculate the window height when the floating keyboard is opened and closed.
 */
public class KeyboardHeightProvider extends PopupWindow implements ViewTreeObserver.OnGlobalLayoutListener {

    /**
     * The keyboard height observers
     */
    private final List<KeyboardHeightObserver> observerList = new ArrayList<>();

    /**
     * The last cached height of the keyboard
     */
    private int lastKeyboardHeight = 0;

    /**
     * The view that is used to calculate the keyboard height
     */
    private final View popupView;

    /**
     * The parent view
     */
    private final ViewGroup parentView;

    /**
     * The root activity that uses this KeyboardHeightProvider
     */
    private final Activity activity;

    /**
     * Construct a new KeyboardHeightProvider
     *
     * @param activity The parent activity
     */
    public KeyboardHeightProvider(Activity activity) {
        super(activity);
        this.activity = activity;
        parentView = activity.findViewById(android.R.id.content);

        this.popupView = LayoutInflater.from(activity).inflate(R.layout.popupwindow, parentView, false);
        setContentView(popupView);

        setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE | LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);

        setWidth(0);
        setHeight(LayoutParams.MATCH_PARENT);
    }

    /**
     * Start the KeyboardHeightProvider, this must be called after the onResume of the Activity.
     * PopupWindows are not allowed to be registered before the onResume has finished
     * of the Activity.
     */
    public void start() {
        popupView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        if (!isShowing() && parentView.getWindowToken() != null) {
            setBackgroundDrawable(new ColorDrawable(0));
            showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0);
        }
    }

    /**
     * Stop the KeyboardHeightProvider, this provider can be start again.
     */
    public void stop() {
        popupView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        dismiss();
    }

    /**
     * Add the keyboard height observer to this provider. The
     * observer will be notified when the keyboard height has changed.
     * For example when the keyboard is opened or closed.
     *
     * @param observer The observer to be added to this provider.
     */
    public void addKeyboardHeightObserver(KeyboardHeightObserver observer) {
        if (!observerList.contains(observer)) {
            observerList.add(observer);
        }
    }

    /**
     * Remove the keyboard height observer from this provider.
     *
     * @param observer The observer to be removed from this provider.
     */
    public void removeKeyboardHeightObserver(KeyboardHeightObserver observer) {
        observerList.remove(observer);
    }

    @Override
    public void onGlobalLayout() {
        Rect rect = new Rect();
        popupView.getWindowVisibleDisplayFrame(rect);

        int keyboardHeight;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            Point screenSize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(screenSize);
            keyboardHeight = screenSize.y - rect.bottom;
        } else {
            View decorView = activity.getWindow().getDecorView();
            WindowInsets windowInsets = decorView.getRootWindowInsets();
            int insetsTop;
            int insetsBottom;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                insetsTop = windowInsets.getInsets(WindowInsets.Type.systemBars()).top;
                insetsBottom = windowInsets.getInsets(WindowInsets.Type.systemBars()).bottom;
            } else {
                insetsTop = windowInsets.getSystemWindowInsetTop();
                insetsBottom = windowInsets.getSystemWindowInsetBottom();
            }
            keyboardHeight = decorView.getHeight() - insetsTop - insetsBottom - rect.height();
        }

        if (lastKeyboardHeight != keyboardHeight) {
            notifyKeyboardHeightChanged(keyboardHeight);
            lastKeyboardHeight = keyboardHeight;
        }
    }

    private void notifyKeyboardHeightChanged(int height) {
        for (KeyboardHeightObserver observer : observerList) {
            observer.onKeyboardHeightChanged(height);
        }
    }
}
