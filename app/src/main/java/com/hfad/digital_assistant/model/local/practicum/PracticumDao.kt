package com.hfad.digital_assistant.model.local.practicum

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hfad.digital_assistant.model.local.practicum.PendingPracticumAnswerEntity

@Dao
interface PracticumDao {

    @Query("SELECT * FROM practicum_cases")
    suspend fun getCases(): List<PracticumCaseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCases(cases: List<PracticumCaseEntity>)

    @Query("DELETE FROM practicum_cases")
    suspend fun clearCases()

    @Query("SELECT * FROM practicum_answers")
    suspend fun getAnswers(): List<PracticumAnswerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(
        answers: List<PracticumAnswerEntity>
    )

    @Query("DELETE FROM practicum_answers")
    suspend fun clearAnswers()

    @Query("SELECT * FROM pending_practicum_answers")
    suspend fun getPendingAnswers():
            List<PendingPracticumAnswerEntity>

    @Insert
    suspend fun insertPending(
        entity: PendingPracticumAnswerEntity
    )

    @Delete
    suspend fun deletePending(
        entity: PendingPracticumAnswerEntity
    )
}