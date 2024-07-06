package com.example.newsapp.repository

import com.example.newsapp.api.REtrofitInstance
import com.example.newsapp.db.ArticleDatabase
import com.example.newsapp.model.Article

class NewsRepository (val db:ArticleDatabase){

    suspend fun getHeadlines(countryCode:String,pageNumber:Int)=
        REtrofitInstance.api.getHeadlines(countryCode,pageNumber)


    suspend fun searchNews(searchQuerry:String, pageNumber: Int) =
        REtrofitInstance.api.searchForNews(searchQuerry,pageNumber)

    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)

    fun getFavouriteNews() = db.getArticleDao().getAllArticles()

    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)

}