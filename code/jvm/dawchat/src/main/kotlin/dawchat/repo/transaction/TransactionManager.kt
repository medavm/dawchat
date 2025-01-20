package dawchat.repo.transaction

interface TransactionManager {
    fun <R> run(block: (Transaction) -> R): R
}