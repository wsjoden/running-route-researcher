package com.example.runningrouteresearcher.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.runningrouteresearcher.R
import com.example.runningrouteresearcher.ui.fragments.MapFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MapFragment())
                .commit()
        }
    }
}