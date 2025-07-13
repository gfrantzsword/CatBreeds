package com.example.catbreeds.data.remote

import com.example.catbreeds.domain.models.Breed
import retrofit2.http.GET

interface RemoteService {
    @GET("breeds")
    suspend fun getBreeds(): List<Breed>
}