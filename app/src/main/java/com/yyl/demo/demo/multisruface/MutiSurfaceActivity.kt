package com.yyl.demo.demo.multisruface

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import com.yyl.demo.R
import kotlinx.android.synthetic.main.activity_muti_surface.*

class MutiSurfaceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muti_surface)

        multiSurface.renderSurface.onRenderListener = {
            runOnUiThread {
                if (multiLayout.childCount > 0) {
                    multiLayout.removeAllViews()
                }

                for (i in 0..2) {
                    val surfaceChild = MutiSurfaceViewChild(this)

                    surfaceChild.renderSurface.textureId=it
                    surfaceChild.setSurfaceAndEglContext(null,multiSurface.eglContext())

                    multiLayout.addView(surfaceChild,LinearLayout.LayoutParams(300, LinearLayout.LayoutParams.MATCH_PARENT))
                }


            }
        }
    }
}
