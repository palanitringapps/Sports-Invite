package com.sportinvitedemo.ui.invitemember

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.sportinvitedemo.R
import com.sportinvitedemo.databinding.ActivityScanQrLayoutBinding
import com.sportinvitedemo.qr.QRCodeGen
import com.sportinvitedemo.utils.Constants

class QRScanActivity : AppCompatActivity() {
    private lateinit var bind: ActivityScanQrLayoutBinding
    private val qr = QRCodeGen()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = DataBindingUtil.setContentView(this, R.layout.activity_scan_qr_layout)
        val url = intent.getStringExtra(Constants.TOKEN)
        generateQRCode(url ?: "")
        bind.toolbar.ivBack.setOnClickListener {
            onBackPressed()
        }
        bind.toolbar.tvTitle.text = getString(R.string.qr_code)
    }

    private fun generateQRCode(inviteCode: String) {
        val bitmap =
            qr.encodeAsBitmap(inviteCode, 300, 300, this)
        bind.ivQRCode.setImageBitmap(bitmap)
    }
}