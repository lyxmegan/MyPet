package edu.utap.mypet.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import com.google.gson.annotations.SerializedName

data class CatFact(
        @SerializedName("text")
        val text: String,
        @SerializedName("type")
        val type: String
)
interface CatFactApi {
    @GET("facts/random?animal_type=cat&amount=1")
    suspend fun fetchFact(): CatFact

    /**
     * Factory class for convenient creation of the Api Service interface
     */
    companion object Factory {
        fun create(): CatFactApi {
            val retrofit: Retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    // Must end in /!
                    .baseUrl("https://cat-fact.herokuapp.com/")
                    .build()
            return retrofit.create(CatFactApi::class.java)
        }
    }

}