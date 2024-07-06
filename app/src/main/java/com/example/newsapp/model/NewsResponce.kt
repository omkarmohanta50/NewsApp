package com.example.newsapp.model

data class NewsResponce(
    val articles: MutableList<Article>,
    val status: String,
    val totalResults: Int
)