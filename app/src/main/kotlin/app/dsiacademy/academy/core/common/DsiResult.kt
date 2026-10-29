package app.dsiacademy.academy.core.common

/**
 * Résultat standard de toutes les opérations domain/data.
 * Kotlin pur — aucun import Android.
 */
sealed class DsiResult<out T> {
    data class Success<T>(val data: T) : DsiResult<T>()
    data class Error(val error: AppError) : DsiResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = (this as? Success)?.data

    inline fun <R> map(transform: (T) -> R): DsiResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    inline fun onSuccess(block: (T) -> Unit): DsiResult<T> {
        if (this is Success) block(data)
        return this
    }

    inline fun onError(block: (AppError) -> Unit): DsiResult<T> {
        if (this is Error) block(error)
        return this
    }
}
