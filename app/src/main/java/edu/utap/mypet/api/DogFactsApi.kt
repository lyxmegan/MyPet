package edu.utap.mypet.api

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class DogFact(
    @SerializedName("facts")
    val facts: Array<String>,
    @SerializedName("success")
    val success: Boolean
)
interface DogFactApi {
    @GET("api/facts")
    suspend fun fetchFact(): DogFact

    /**
     * Factory class for convenient creation of the Api Service interface
     */
    companion object Factory {
        fun create(): DogFactApi {
            val retrofit: Retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                // Must end in /!
                .baseUrl("http://dog-api.kinduff.com/")
                .build()
            return retrofit.create(DogFactApi::class.java)
        }
    }
}