package com.suibiankan.tv.ui.main

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

/**
 * Single-activity host for the TV app.
 * All screens are fragments managed by this activity.
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, MainFragment())
                .commit()
        }
    }
}
