package com.sportinvitedemo.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sportinvitedemo.R
import com.sportinvitedemo.databinding.ActivityMainBinding
import com.sportinvitedemo.ui.invitemember.InviteMemberActivity

class InviteHomeActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = DataBindingUtil.setContentView(this, R.layout.activity_main)
        bind.btnInvite.setOnClickListener {
            startActivity(Intent(this, InviteMemberActivity::class.java))
        }
        bind.toolbar.ivBack.setOnClickListener {
            onBackPressed()
        }
        bind.toolbar.tvTitle.text = getString(R.string.welcome)
    }
}