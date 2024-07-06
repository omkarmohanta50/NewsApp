package com.example.newsapp.api

import com.example.newsapp.databinding.FragmentSearchBinding
import com.example.newsapp.model.NewsResponce
import com.example.newsapp.util.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale.IsoCountryCode

interface NewsAPI {
    @GET("v2/top-headlines")
    suspend fun getHeadlines(
        @Query("country")
        countryCode: String = "in",
        @Query("page")
        pageNumber :Int = 1,
        @Query("apiKey")
        apiKey :String = API_KEY

    ): Response<NewsResponce>

    @GET("v2/everything")
    suspend fun searchForNews(
        @Query("q")
        searchQuery: String,
        @Query("page")
        pageNumber: Int= 1,
        @Query("apiKey")
        apiKey : String = API_KEY

    ):Response<NewsResponce>
}