package com.sportinvitedemo.ui.invitemember

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.sportinvitedemo.R
import com.sportinvitedemo.data.InviteLinkResponse
import com.sportinvitedemo.data.MemberInviteModel
import com.sportinvitedemo.data.Status
import com.sportinvitedemo.databinding.ActivityInviteMemberLayoutBinding
import com.sportinvitedemo.utils.Constants
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class InviteMemberActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private val TAG = InviteMemberActivity::class.java.simpleName
    private lateinit var bind: ActivityInviteMemberLayoutBinding
    private var memberInviteModel: MemberInviteModel? = null
    private var inviteLinkResponse: InviteLinkResponse? = null

    private val viewModel: InviteMemberViewModel by viewModels {
        InviteMemberViewModel.Factory(
            application
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = DataBindingUtil.setContentView(this, R.layout.activity_invite_member_layout)

        viewModel.getMemberInfo("sample teamID")

        bind.toolbar.ivBack.setOnClickListener {
            onBackPressed()
        }
        bind.toolbar.tvTitle.text = getString(R.string.invite_members)

        lifecycleScope.launch {
            viewModel.memberDetails.collect {
                when (it.status) {
                    Status.LOADING -> {
                        bind.progress.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        bind.progress.visibility = View.GONE
                        memberInviteModel = it.data
                        viewModel.updateAdapterArray(it.data)
                        updateUI(it.data)
                        setSpinnerAdapter(viewModel.positionAdapter)
                    }
                    Status.ERROR -> {
                        bind.progress.visibility = View.GONE
                        showToastMessage(it.message ?: "")
                    }
                    else -> Log.i(TAG, "Handled default case")
                }
            }
        }

        lifecycleScope.launch {
            viewModel.inviteLink.collect {
                when (it.status) {
                    Status.LOADING -> {
                        bind.progress.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        bind.progress.visibility = View.GONE
                        inviteLinkResponse = it.data
                    }
                    Status.ERROR -> {
                        bind.progress.visibility = View.GONE
                        showToastMessage(it.message ?: "")
                    }
                    else -> Log.i(TAG, "Handled default case")
                }
            }
        }



        bind.btnShareQRCode.setOnClickListener {
            startActivity(Intent(this, QRScanActivity::class.java).apply {
                putExtra(Constants.TOKEN, inviteLinkResponse?.url)
            })
        }

        bind.btnCopyLink.setOnClickListener {
            val clipboard: ClipboardManager =
                getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(Constants.INVITE_URL, inviteLinkResponse?.url ?: "")
            clipboard.setPrimaryClip(clip)
            showToastMessage(getString(R.string.copied))
        }

        viewModel.isDisableInvite.observe(this, {
            if (it) disableInvite()
        })
    }

    private fun updateUI(memberInviteModel: MemberInviteModel?) {
        if (memberInviteModel?.plan?.supporterLimit ?: 0 > 0) {
            bind.tvCurrentSupporters.visibility = View.VISIBLE
            bind.tvSupporterLimit.visibility = View.VISIBLE
            bind.tvSupporterLimit.text = getString(R.string.limit).plus(" ")
                .plus(memberInviteModel?.plan?.supporterLimit ?: 0)
            bind.tvCurrentSupporters.text = getString(R.string.current_supporters).plus(" ")
                .plus(memberInviteModel?.members?.supporters)
        } else {
            bind.tvCurrentSupporters.visibility = View.GONE
            bind.tvSupporterLimit.visibility = View.GONE
        }
        val memberCount = (memberInviteModel?.members?.total ?: 0).minus(
            memberInviteModel?.members?.supporters ?: 0
        )
        bind.tvCurrentMembers.text = getString(R.string.current_members).plus(" ").plus(memberCount)
        bind.tvMemberLimit.text =
            getString(R.string.limit).plus(" ").plus(memberInviteModel?.plan?.memberLimit ?: 0)
    }

    private fun setSpinnerAdapter(data: Array<String>) {
        val positionAdapter: ArrayAdapter<String> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, data)
        bind.spInvitePermission.adapter = positionAdapter
        bind.spInvitePermission.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        viewModel.getInviteLink(memberInviteModel?.id ?: "", viewModel.createEditorData(position))
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Log.i(TAG, "Nothing doing here")
    }

    private fun disableInvite() {
        bind.btnCopyLink.isEnabled = false
        bind.btnCopyLink.isClickable = false
        bind.btnShareQRCode.isClickable = false
        bind.btnShareQRCode.isClickable = false
        bind.spInvitePermission.isEnabled = false
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}