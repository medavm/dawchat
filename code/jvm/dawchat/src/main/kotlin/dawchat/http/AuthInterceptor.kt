package dawchat.http

import dawchat.domain.user.AuthenticatedUser
import dawchat.services.ServiceResult
import dawchat.services.UsersService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthInterceptor(
    private val usersService: UsersService,
) : HandlerInterceptor {

    companion object {
        //private val logger = LoggerFactory.getLogger(AuthenticationInterceptor::class.java)
        const val SCHEME = "bearer"
        const val NAME_AUTHORIZATION_HEADER = "Authorization"
        private const val NAME_WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"

        const val SESS_C = "t"
    }


    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod && handler.methodParameters.any {
                it.parameterType == AuthenticatedUser::class.java
            }
        ) {

            var authUser: AuthenticatedUser? = null
            if(request.cookies!=null){
                for(c in request.cookies){
                    if(c.name == SESS_C){
                        val t = c.value
                        val res = usersService.authenticatedUser(t)
                        when(res){
                            is ServiceResult.Success -> authUser = res.result
                            is ServiceResult.Error -> {
                                val cookie = Cookie(SESS_C, "")
                                cookie.maxAge = 0
                                cookie.path = "/"
                                cookie.isHttpOnly = true
                                response.addCookie(cookie)
                            }
                        }
                        break
                    }
                }
            }

            if(authUser != null){
                AuthUserResolver.addUserTo(authUser, request)
                return true
            }
            else{
                //response.addHeader(NAME_WWW_AUTHENTICATE_HEADER, SCHEME)
                response.sendError(401)
                return false
            }

        }

        return true
    }


}