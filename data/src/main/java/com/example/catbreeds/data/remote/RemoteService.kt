package com.example.catbreeds.data.remote

import retrofit2.http.GET

interface RemoteService {
    @GET("breeds")
    suspend fun getBreeds(): List<BreedDto>
}