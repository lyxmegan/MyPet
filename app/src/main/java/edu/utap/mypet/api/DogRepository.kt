package edu.utap.mypet.api

class DogRepository(private val DogFactApi: DogFactApi) {
    suspend fun fetchDogFact(): String {
        return DogFactApi.fetchFact().facts[0]
    }
}