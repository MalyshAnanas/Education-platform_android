package com.hfad.digital_assistant.model.api

class PracticumRepository(
    private val api: PracticumApi
) {

    suspend fun getOpenCases(): List<PracticumCase> {
        return api.getOpenCases()
    }

    suspend fun sendAnswer(caseId: Int, text: String): PracAnswerResponse {
        return api.sendAnswer(
            PracAnswerRequest(case = caseId, text = text)
        )
    }

    suspend fun getAllCases(): List<PracticumCase> {

        val open = api.getOpenCases()
        val closed = api.getClosedCases()

        return open + closed
    }
}