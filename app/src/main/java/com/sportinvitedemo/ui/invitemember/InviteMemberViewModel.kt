package com.sportinvitedemo.ui.invitemember

import android.app.Application
import android.widget.ArrayAdapter
import androidx.lifecycle.*
import com.google.gson.Gson
import com.sportinvitedemo.R
import com.sportinvitedemo.api.ApiServices
import com.sportinvitedemo.data.*
import com.sportinvitedemo.repo.SportInviteRepo
import com.sportinvitedemo.utils.Constants
import com.sportinvitedemo.utils.checkForInternet
import com.sportinvitedemo.utils.getResponseFromAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InviteMemberViewModel(var app: Application) : AndroidViewModel(app) {

    private val repo = SportInviteRepo(ApiServices.service)
    val memberDetails =
        MutableStateFlow(ResultState(Status.NONE, MemberInviteModel(null, null, null), ""))
    val inviteLink = MutableStateFlow(ResultState(Status.NONE, InviteLinkResponse(""), ""))
    var positionAdapter: Array<String> = emptyArray()
    val isDisableInvite = MutableLiveData<Boolean>()

    fun getMemberInfo(teamId: String) {
        memberDetails.value = ResultState.loading()
        if (checkForInternet(app)) {
            viewModelScope.launch {
                /*repo.getInviteInfo(teamId).catch {*/ // Commented API call ro repository class due
                // to server unavailability
                memberListDetails().catch {
                    memberDetails.value = ResultState.error(it.message.toString())
                }.collect {
                    memberDetails.value = ResultState.success(it.data)
                }
                /*}
                    .collect { memberDetails.value = ResultState.success(it.data) }*/
            }
        } else memberDetails.value = ResultState.error(app.getString(R.string.internet_error))
    }

    fun getInviteLink(teamId: String, editor: RoleBasedInviteRequest) {
        inviteLink.value = ResultState.loading()
        if (checkForInternet(app)) {
            viewModelScope.launch {
                /*repo.getInviteLink(teamId, editor).catch {*/ // commented API call from repository callback
                inviteLinkUrl(editor.role).catch {
                    inviteLink.value = ResultState.error(it.message.toString())
                }.collect {
                    inviteLink.value = ResultState.success(it.data)
                }
            }
            /*    .collect { inviteLink.value = ResultState.success(it.data) }
        }*/
        } else inviteLink.value = ResultState.error(app.getString(R.string.internet_error))
    }


    private suspend fun memberListDetails(): Flow<ResultState<MemberInviteModel>> {
        return flow {
            val fileName = when ((1..100).random()) {
                in 1..25 -> "response1.json"
                in 26..50 -> "response2.json"
                in 50..75 -> "response3.json"
                else -> "response4.json"
            }
            val model = getResponseFromAsset(app, fileName)
            val gson = Gson()
            val inviteResponse =
                gson.fromJson(model, MemberInviteModel::class.java)
            emit(ResultState.success(inviteResponse))
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun inviteLinkUrl(editor: String): Flow<ResultState<InviteLinkResponse>> {
        return flow {
            val fileName = when (editor) {
                Constants.MANAGER -> "inviteByCoach.json"
                Constants.EDITOR -> "inviteByPCoach.json"
                Constants.MEMBER -> "inviteByPlayer.json"
                Constants.READ_ONLY -> "inviteBySupport.json"
                else -> ""
            }
            val model = getResponseFromAsset(app, fileName)
            val gson = Gson()
            val inviteLink =
                gson.fromJson(model, InviteLinkResponse::class.java)
            emit(ResultState.success(inviteLink))
        }.flowOn(Dispatchers.IO)
    }

    fun createEditorData(editorPosition: Int): RoleBasedInviteRequest {
        return when (positionAdapter[editorPosition]) {
            Constants.COACH -> RoleBasedInviteRequest(Constants.MANAGER)
            Constants.PLAYER_COACH -> RoleBasedInviteRequest(Constants.EDITOR)
            Constants.PLAYER -> RoleBasedInviteRequest(Constants.MEMBER)
            Constants.SUPPORTERS -> RoleBasedInviteRequest(Constants.READ_ONLY)
            else -> RoleBasedInviteRequest("")
        }
    }


    fun updateAdapterArray(memberInviteModel: MemberInviteModel?) {
        val memberTotal = (memberInviteModel?.members?.total ?: 0).minus(
            memberInviteModel?.members?.supporters ?: 0
        )
        val supporterLimit = memberInviteModel?.plan?.supporterLimit ?: 0
        val memberLimit = memberInviteModel?.plan?.memberLimit ?: 0
        val supportedCount = memberInviteModel?.members?.supporters ?: 0

        if (memberTotal < memberLimit && supporterLimit > 0 && supportedCount < supporterLimit) {
            positionAdapter = app.resources.getStringArray(R.array.all_position)
        } else if (memberTotal < memberLimit && (supporterLimit == 0 || supportedCount >= supporterLimit)) {
            positionAdapter = app.resources.getStringArray(R.array.position_without_supporters)
        } else if (memberTotal >= memberLimit && (supporterLimit > 0 && supportedCount < supporterLimit)) {
            positionAdapter = app.resources.getStringArray(R.array.position_only_supporter)
        } else {
            isDisableInvite.value = true
            positionAdapter = emptyArray()
        }
        if (positionAdapter.isNotEmpty()) {
            getInviteLink(memberInviteModel?.id ?: "", createEditorData(0))
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InviteMemberViewModel(app) as T
        }

    }
}