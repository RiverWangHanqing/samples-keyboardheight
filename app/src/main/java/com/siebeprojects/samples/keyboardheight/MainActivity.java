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

package com.siebeprojects.samples.keyboardheight;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.siebeprojects.keyboardheight.KeyboardHeightObserver;
import com.siebeprojects.keyboardheight.KeyboardHeightProvider;

/**
 * MainActivity that initializes the keyboardheight provider and observer.
 */
public final class MainActivity extends AppCompatActivity implements KeyboardHeightObserver {

    private final static String TAG = "MainActivity";
    private KeyboardHeightProvider keyboardHeightProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);

        keyboardHeightProvider = new KeyboardHeightProvider(this);
        keyboardHeightProvider.addKeyboardHeightObserver(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // make sure to start the keyboard height provider after the onResume
        // of this activity. This is because a popup window must be initialised
        // and attached to the activity root view.
        View view = findViewById(R.id.activitylayout);
        view.post(() -> keyboardHeightProvider.start());
    }

    @Override
    public void onPause() {
        super.onPause();
        keyboardHeightProvider.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        keyboardHeightProvider.removeKeyboardHeightObserver(this);
    }

    @Override
    public void onKeyboardHeightChanged(int height) {
        Log.i(TAG, "onKeyboardHeightChanged in pixels: " + height);

        TextView tv = (TextView) findViewById(R.id.height_text);
        tv.setText(String.valueOf(height));

        // color the keyboard height view, this will remain visible when you close the keyboard
        View view = findViewById(R.id.keyboard);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);
    }
}
