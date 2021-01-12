package edu.utap.mypet.api

class CatRepository(private val catFactApi: CatFactApi) {
    suspend fun fetchCatFact(): String {
        return catFactApi.fetchFact().text
    }
}