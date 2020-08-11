package com.hedvig.notificationService.customerio.state

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinMapper
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.mapper.RowMapperFactory
import org.jdbi.v3.core.statement.StatementContext
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.SQLException
import java.time.Instant

@Repository
class IdempotenceHashRepositoryImpl(
    val jdbi: Jdbi
) : IdempotenceHashRepository {
    override fun save(memberId: String, hash: String) {
        jdbi.withHandle<Int, RuntimeException> {
            val stmt =
                """
                    INSERT INTO idempotence_hash (
                        member_id,
                        hash,
                        created_at
                    )
                    VALUES (
                        :memberId,
                        :hash,
                        :createdAt
                    )
                    ON CONFLICT (member_id, hash) DO NOTHING
                """

            it.createUpdate(stmt)
                .bind("memberId", memberId)
                .bind("hash", memberId)
                .bind("createdAt", Instant.now())
                .execute()
        }
    }

    override fun findBefore(before: Instant): List<IdempotenceHashEntry> =
        jdbi.withHandle<List<IdempotenceHashEntry>, RuntimeException> {
            it.registerRowMapper(
                RowMapperFactory.of(
                    IdempotenceHashEntry::class.java,
                    KotlinMapper(IdempotenceHashEntry::class.java)
                )
            )
            val query = """
                SELECT * 
                FROM idempotence_hash 
                WHERE created_at < :before
            """.trimIndent()

            it.createQuery(query)
                .bind("before", before)
                .map(IdempotenceHashEntryMapper())
                .list()
                .toList()
        }

    override fun delete(memberId: String, hash: String) {
        jdbi.withHandle<Int, RuntimeException> {
            val statement = """
                DELETE FROM idempotence_hash 
                WHERE member_id = :memberId AND hash = :hash
            """.trimIndent()

            it.createUpdate(statement)
                .bind("memberId", memberId)
                .bind("hash", hash)
                .execute()
        }
    }

    internal class IdempotenceHashEntryMapper :
        RowMapper<IdempotenceHashEntry> {
        @Throws(SQLException::class)
        override fun map(rs: ResultSet, ctx: StatementContext?): IdempotenceHashEntry {
            return IdempotenceHashEntry(
                rs.getString("member_id"),
                rs.getString("hash"),
                rs.getTimestamp("created_at").toInstant()
            )
        }
    }
}