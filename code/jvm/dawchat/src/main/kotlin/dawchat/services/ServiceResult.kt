package dawchat.services


sealed class ServiceResult<out T, out E> {
    data class Success<out T>(val result: T):       ServiceResult<T, Nothing>()
    data class Error<out E>(val error: E):          ServiceResult<Nothing, E>()
}