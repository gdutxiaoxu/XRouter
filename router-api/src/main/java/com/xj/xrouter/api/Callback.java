package com.xj.xrouter.api;

import android.content.Intent;

/**
 * 回调
 */
public interface Callback {
    void onActivityResult(int requestCode, int resultCode, Intent data);
}
