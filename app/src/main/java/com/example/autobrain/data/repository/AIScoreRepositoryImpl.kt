package com.example.autobrain.data.repository

import com.example.autobrain.core.utils.Result
import com.example.autobrain.data.local.dao.AIScoreDao
import com.example.autobrain.data.local.entity.AIScore
import com.example.autobrain.data.local.entity.toAIScore
import com.example.autobrain.data.local.entity.toAIScoreEntity
import com.example.autobrain.data.local.entity.toFirestoreMap
import com.example.autobrain.data.local.entity.toDomain
import com.example.autobrain.domain.repository.AIScoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class AIScoreRepositoryImpl @Inject constructor(
    private val aiScoreDao: AIScoreDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : AIScoreRepository {

    override fun getAIScoresByUser(userId: String): Flow<List<AIScore>> {
        return aiScoreDao.getAIScoresByUser(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getAIScoresByCar(userId: String, carId: String): Flow<List<AIScore>> {
        return aiScoreDao.getAIScoresByCar(userId, carId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getLatestAIScore(userId: String): Flow<AIScore?> {
        return aiScoreDao.getLatestAIScore(userId)
            .map { it?.toDomain() }
    }

    override fun getLatestAIScoreForCar(userId: String, carId: String): Flow<AIScore?> {
        return aiScoreDao.getLatestAIScoreForCar(userId, carId)
            .map { it?.toDomain() }
    }

    override suspend fun getAIScoreById(scoreId: String): AIScore? {
        return aiScoreDao.getAIScoreById(scoreId)?.toDomain()
    }

    override suspend fun saveAIScore(score: AIScore): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.Error(Exception("User not authenticated"))

            val scoreId = if (score.id.isEmpty()) UUID.randomUUID().toString() else score.id
            val scoreWithId = score.copy(id = scoreId, userId = userId)

            aiScoreDao.insertAIScore(scoreWithId.toAIScoreEntity(isSynced = false))

            firestore.collection("users")
                .document(userId)
                .collection("ai_scores")
                .document(scoreId)
                .set(scoreWithId.toFirestoreMap())
                .await()

            aiScoreDao.markAsSynced(scoreId)

            Result.Success(scoreId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteAIScore(scoreId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.Error(Exception("User not authenticated"))

            aiScoreDao.deleteAIScore(scoreId)

            firestore.collection("users")
                .document(userId)
                .collection("ai_scores")
                .document(scoreId)
                .delete()
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun syncAIScores(userId: String): Result<Unit> {
        return try {
            val unsyncedScores = aiScoreDao.getUnsyncedScores(userId)

            unsyncedScores.forEach { entity ->
                firestore.collection("users")
                    .document(userId)
                    .collection("ai_scores")
                    .document(entity.id)
                    .set(entity.toDomain().toFirestoreMap())
                    .await()

                aiScoreDao.markAsSynced(entity.id)
            }

            val cloudScores = firestore.collection("users")
                .document(userId)
                .collection("ai_scores")
                .get()
                .await()

            val scores = cloudScores.documents.mapNotNull { doc ->
                doc.data?.toAIScore()
            }

            aiScoreDao.insertAIScores(scores.map { it.toAIScoreEntity(isSynced = true) })

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getAverageScore(userId: String): Float? {
        return aiScoreDao.getAverageScore(userId)
    }
}
