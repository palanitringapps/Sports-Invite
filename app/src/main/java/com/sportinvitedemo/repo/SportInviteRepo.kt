package com.sportinvitedemo.repo

import com.sportinvitedemo.api.ApiServices
import com.sportinvitedemo.data.InviteLinkResponse
import com.sportinvitedemo.data.MemberInviteModel
import com.sportinvitedemo.data.ResultState
import com.sportinvitedemo.data.RoleBasedInviteRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SportInviteRepo(private val apiServices: ApiServices) {

    suspend fun getInviteInfo(teamId: String): Flow<ResultState<MemberInviteModel>> {
        return flow {
            val response = apiServices.getInviteIdInfo(teamId)
            emit(ResultState.success(response))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getInviteLink(
        teamId: String,
        inviteRequest: RoleBasedInviteRequest
    ): Flow<ResultState<InviteLinkResponse>> {
        return flow {
            val response = apiServices.getInviteLink(teamId, inviteRequest)
            emit(ResultState.success(response))
        }.flowOn(Dispatchers.IO)
    }
}
