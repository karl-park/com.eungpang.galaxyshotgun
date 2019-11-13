package com.eungpang.galaxyshotgun

import android.animation.ObjectAnimator
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.animation.addListener
import androidx.databinding.DataBindingUtil
import com.eungpang.galaxyshotgun.databinding.ActivityMainBinding
import com.eungpang.galaxyshotgun.model.GalaxyShotgunViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var model: GalaxyShotgunViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model = GalaxyShotgunViewModel(application)

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = model
        binding.lifecycleOwner = this

        bullet.visibility = View.GONE
        constraintLayout.setOnClickListener {
            if (model.bullet() == 0) {
                Toast.makeText(this, "Insufficient Bullet. Please buy it more.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ObjectAnimator.ofFloat(bullet, "translationY", -400f).apply {

                bullet.visibility = View.VISIBLE
                addListener(
                    onEnd = {
                        bullet.visibility = View.GONE
                        ObjectAnimator.ofFloat(bullet, "translationY", 400f).apply {
                            duration = 10
                            start()
                        }
                    },
                    onStart = {
                        bullet.visibility = View.VISIBLE
                    },
                    onCancel = {

                    },
                    onRepeat = {

                    }
                )

                duration = 500
                start()
                model.onFire()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        model.onStop()
    }

    override fun onBackPressed() {
        // do nothing
    }
}
