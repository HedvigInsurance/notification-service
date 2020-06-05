package com.hedvig.notificationService.customerio

import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl

class WorkspaceSelector(
    private val contractLoader: ContractLoader,
    private val memberServiceImpl: MemberServiceImpl
) {

    fun getWorkspaceForMember(memberId: String): Workspace {
        var marketForMember = contractLoader.getWorkspaceForMember(memberId)
        if (marketForMember == Workspace.NOT_FOUND) {
            val pickedLocale = memberServiceImpl.getPickedLocale(memberId)

            marketForMember =
                Workspace.getWorkspaceFromLocale(pickedLocale)
            if (marketForMember == Workspace.NOT_FOUND)
                throw WorkspaceNotFound("Could not map member $memberId to workspace")
        }
        return marketForMember
    }
}
