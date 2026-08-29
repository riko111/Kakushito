package com.isoffice.kakushito.server

import com.google.firebase.auth.FirebaseToken
import kotlinx.serialization.Serializable
import javax.sql.DataSource

@Serializable
data class User(
    val id: Long,
    val firebaseUid: String,
    val email: String?,
    val displayName: String?,
    val plan: String
)

class UserRepository(
    private val dataSource: DataSource
) {

    fun findByFirebaseUid(firebaseUid: String): User? {
        val sql = """
            SELECT id, firebase_uid, email, display_name, plan
            FROM users
            WHERE firebase_uid = ?
        """.trimIndent()

        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, firebaseUid)

                statement.executeQuery().use { result ->
                    if (!result.next()) {
                        return null
                    }

                    return User(
                        id = result.getLong("id"),
                        firebaseUid = result.getString("firebase_uid"),
                        email = result.getString("email"),
                        displayName = result.getString("display_name"),
                        plan = result.getString("plan")
                    )
                }
            }
        }
    }

    fun createFromFirebase(token: FirebaseToken): User {
        val sql = """
            INSERT INTO users (
                firebase_uid,
                email,
                display_name
            )
            VALUES (?, ?, ?)
        """.trimIndent()

        dataSource.connection.use { connection ->
            connection.prepareStatement(
                sql,
                java.sql.Statement.RETURN_GENERATED_KEYS
            ).use { statement ->

                statement.setString(1, token.uid)
                statement.setString(2, token.email)
                statement.setString(3, token.name)

                statement.executeUpdate()

                statement.generatedKeys.use { keys ->
                    if (!keys.next()) {
                        error("Failed to create user")
                    }

                    return User(
                        id = keys.getLong(1),
                        firebaseUid = token.uid,
                        email = token.email,
                        displayName = token.name,
                        plan = "free"
                    )
                }
            }
        }
    }
}
