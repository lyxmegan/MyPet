package edu.utap.mypet.funFacts

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.utap.mypet.api.CatFactApi
import edu.utap.mypet.api.CatRepository
import edu.utap.mypet.api.DogFactApi
import edu.utap.mypet.api.DogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class FactsViewModel : ViewModel() {
    private val catFactApi = CatFactApi.create()
    private val catRepository = CatRepository(catFactApi)
    private val catFact = MutableLiveData<String>()
    private val dogFactApi = DogFactApi.create()
    private val dogRepository = DogRepository(dogFactApi)
    private val dogFact = MutableLiveData<String>()

    init {
        viewModelScope.launch {
            catFact.value = catRepository.fetchCatFact()
            Log.d("cat fact", "${catRepository.fetchCatFact()}")
            dogFact.value = dogRepository.fetchDogFact()
            Log.d("dog fact", "${dogRepository.fetchDogFact()}")

        }
    }
    fun netRefreshCatFact() = viewModelScope.launch(
        context = viewModelScope.coroutineContext
                + Dispatchers.IO) {
        // Update LiveData from IO dispatcher, use postValue
        catFact.postValue(catRepository.fetchCatFact())
    }

    fun netRefreshDogFact() = viewModelScope.launch(
        context = viewModelScope.coroutineContext
                + Dispatchers.IO) {
        // Update LiveData from IO dispatcher, use postValue
        dogFact.postValue(dogRepository.fetchDogFact())
    }

    fun observeCatFact(): LiveData<String> {
        return catFact
    }

    fun observeDogFact(): LiveData<String> {
        return dogFact
    }
}