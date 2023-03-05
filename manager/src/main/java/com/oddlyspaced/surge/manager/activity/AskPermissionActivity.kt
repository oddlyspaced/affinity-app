package com.oddlyspaced.surge.manager.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.oddlyspaced.surge.manager.R
import com.oddlyspaced.surge.manager.databinding.ActivityAskPermissionBinding

class AskPermissionActivity : AppCompatActivity() {

    private val binding by lazy { ActivityAskPermissionBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}