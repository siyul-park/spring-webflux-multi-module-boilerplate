package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.request.UpdateUserContactRequest
import io.github.siyual_park.application.server.dto.response.UserContactInfo
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.json.patch.PropertyOverridePatch
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.map
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.user.domain.UserContact
import io.github.siyual_park.user.domain.UserStorage
import kotlinx.coroutines.flow.map
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller

@Controller
class UserContactController(
    private val userStorage: UserStorage,
    private val mapperContext: MapperContext
) {
    @PreAuthorize("hasPermission({null, #userId}, {'users.contact:update', 'users[self].contact:update'})")
    suspend fun update(
        userId: ULID,
        request: UpdateUserContactRequest
    ): UserContactInfo {
        val patch = PropertyOverridePatch.of<UserContact, UpdateUserContactRequest>(request)

        val user = userStorage.loadOrFail(userId)
        val contact = user.getContact()

        patch.apply(contact)
        contact.sync()

        return mapperContext.map(contact)
    }
}
