package dawchat.repojdbi.transaction

import dawchat.repo.transaction.Transaction
import dawchat.repo.transaction.TransactionManager
import org.jdbi.v3.core.Jdbi
import org.springframework.stereotype.Component


@Component
class JdbiTransactionManager(
    private val jdbi: Jdbi,
) : TransactionManager {
    override fun <R> run(block: (Transaction) -> R): R =
        jdbi.inTransaction<R, Exception> { handle ->
            val transaction = JdbiTransaction(handle)
            block(transaction)
        }
}