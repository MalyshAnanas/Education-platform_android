package com.hfad.digital_assistant.model.api

import com.hfad.digital_assistant.model.local.practicum.PendingPracticumAnswerEntity
import com.hfad.digital_assistant.model.local.practicum.PracticumAnswerEntity
import com.hfad.digital_assistant.model.local.practicum.PracticumCaseEntity
import com.hfad.digital_assistant.model.local.practicum.PracticumDao

class PracticumRepository(
    private val api: PracticumApi,
    private val dao: PracticumDao
) {

    suspend fun getOpenCases(): List<PracticumCase> {
        return api.getOpenCases()
    }

    suspend fun sendAnswer(caseId: Int, text: String) {

        try {

            api.sendAnswer(
                PracAnswerRequest(
                    case = caseId,
                    text = text
                )
            )

        } catch (e: Exception) {

            dao.insertPending(
                PendingPracticumAnswerEntity(
                    caseId = caseId,
                    text = text
                )
            )
        }
    }

    private suspend fun saveCases(cases: List<PracticumCase>) {

        dao.clearAnswers()
        dao.clearCases()

        dao.insertCases(
            cases.map {
                PracticumCaseEntity(
                    id = it.id,
                    name = it.name,
                    description = it.description
                )
            }
        )

        dao.insertAnswers(
            cases.flatMap { case ->

                case.answers.map { answer ->

                    PracticumAnswerEntity(
                        id = answer.id,
                        caseId = case.id,
                        text = answer.text,
                        status = answer.status,
                        comment = answer.comment
                    )
                }
            }
        )
    }

    private suspend fun getCasesFromDb(): List<PracticumCase> {

        val cases = dao.getCases()
        val answers = dao.getAnswers()

        return cases.map { case ->

            PracticumCase(
                id = case.id,
                name = case.name,
                description = case.description,

                answers = answers
                    .filter { it.caseId == case.id }
                    .map { answer ->

                        PracAnswer(
                            id = answer.id,
                            text = answer.text,
                            status = answer.status,
                            comment = answer.comment
                        )
                    }
            )
        }
    }

    private suspend fun syncPendingAnswers() {

        val pending = dao.getPendingAnswers()

        pending.forEach { answer ->

            try {

                api.sendAnswer(
                    PracAnswerRequest(
                        case = answer.caseId,
                        text = answer.text
                    )
                )

                dao.deletePending(answer)

            } catch (_: Exception) {
            }
        }
    }

    suspend fun getAllCases(): List<PracticumCase> {

        return try {

            syncPendingAnswers()

            val open = api.getOpenCases()
            val closed = api.getClosedCases()

            val result = open + closed

            saveCases(result)

            result

        } catch (e: Exception) {

            getCasesFromDb()
        }
    }
}