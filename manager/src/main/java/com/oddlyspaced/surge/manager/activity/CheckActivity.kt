package com.oddlyspaced.surge.manager.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.oddlyspaced.surge.app.common.databinding.ActivityCheckBinding
import com.oddlyspaced.surge.manager.R

class CheckActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCheckBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}