package com.oddlyspaced.surge.app.common.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.oddlyspaced.surge.app.common.R
import com.oddlyspaced.surge.app.common.databinding.ActivityCheckBinding

class CheckActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCheckBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}