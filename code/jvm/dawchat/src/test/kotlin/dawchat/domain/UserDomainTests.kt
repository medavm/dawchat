package dawchat.domain

import dawchat.domain.user.Sha256TokenEncoder
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UserDomainTests {

    @Test
    fun `can generate valid random token`(){
        val encoder = Sha256TokenEncoder()
        val token = "the-token"
        val validationInformation = encoder.createValidationInfo(token)

        assertNotEquals(token, validationInformation.value)
    }



    @Test
    fun `can validate a token`() {

        val encoder = Sha256TokenEncoder()
        val token = "the-token"
        val validationInformation = encoder.createValidationInfo(token)

        assertNotEquals(token, validationInformation.value)
        val newValidationInformation = encoder.createValidationInfo(token)
        assertEquals(validationInformation.value, newValidationInformation.value)
    }

    @Test
    fun `test token expired`(){

    }


}