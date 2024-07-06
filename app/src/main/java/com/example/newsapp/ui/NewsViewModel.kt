package com.example.newsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.newsapp.model.Article
import com.example.newsapp.model.NewsResponce
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.http.Query
import java.io.IOException
import kotlin.jvm.internal.Ref.BooleanRef

class NewsViewModel(app: Application, val newsRepository: NewsRepository) : AndroidViewModel(app) {

    val headlines: MutableLiveData<Resource<NewsResponce>> = MutableLiveData()
    var headlinesPage = 1
    var headlinesResponse: NewsResponce? = null

    val searchNews: MutableLiveData<Resource<NewsResponce>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponce: NewsResponce? = null
    var newSearchQuerry: String? = null
    var oldSearchQuerry: String? = null

    init {
        getHeadlines("in")
    }

    fun getHeadlines(countryCode: String) = viewModelScope.launch {
        headlinesInternet(countryCode)
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }


    private fun handleHeadLinesResponce(responce: Response<NewsResponce>): Resource<NewsResponce> {
        if (responce.isSuccessful) {
            responce.body()?.let { resultResponce ->
                headlinesPage++
                if (headlinesResponse == null) {
                    headlinesResponse = resultResponce
                } else {
                    val oldArticles = headlinesResponse?.articles
                    val newArticles = resultResponce.articles
                    oldArticles?.addAll(newArticles)

                }
                return Resource.Success(headlinesResponse ?: resultResponce)
            }
        }
        return Resource.Error(responce.message())
    }

    private fun handlerSearchNewsResponce(responce: Response<NewsResponce>): Resource<NewsResponce> {
        if (responce.isSuccessful) {
            responce.body()?.let { resultResponce ->
                if (searchNewsResponce == null || newSearchQuerry != oldSearchQuerry) {
                    searchNewsPage = 1
                    oldSearchQuerry = newSearchQuerry
                    searchNewsResponce = resultResponce
                } else{
                    searchNewsPage++
                    val oldArticles = searchNewsResponce?.articles
                    val newArticles = resultResponce.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponce?: resultResponce)
            }
        }
        return Resource.Error(responce.message())
    }
    fun addTofavourites(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }
    fun getFavouriteNews() = newsRepository.getFavouriteNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }
    fun internetConnection(context: Context):Boolean{
        (context.getSystemService(Context.CONNECTIVITY_SERVICE)as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when{
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI)-> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)->true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)->true
                    else-> false
                }
            }?:false
        }
    }

    private suspend fun headlinesInternet(countryCode: String){
        headlines.postValue(Resource.Loading())
        try{
            if (internetConnection((this.getApplication()))){
                val responce = newsRepository.getHeadlines(countryCode, headlinesPage)
                headlines.postValue(handleHeadLinesResponce(responce))
            }
            else{
                headlines.postValue(Resource.Error("No Internet Connection"))
            }
        } catch (t:Throwable){
            when(t){
                is IOException ->headlines.postValue(Resource.Error("Unable to connect"))
                else -> headlines.postValue(Resource.Error("No signal"))

            }
        }
    }

    private suspend fun searchNewsInternet(searchQuery: String){
      newSearchQuerry = searchQuery
      searchNews.postValue(Resource.Loading())
      try{
          if(internetConnection(this.getApplication())){
              val responce = newsRepository.searchNews(searchQuery,searchNewsPage)
              searchNews.postValue(handlerSearchNewsResponce(responce))
          } else{
              searchNews.postValue(Resource.Error("No internet connection"))
          }
      }catch (t:Throwable){
          when(t){
              is IOException -> searchNews.postValue(Resource.Error("Unable to connect"))
              else -> headlines.postValue(Resource.Error("No signal"))
          }
      }
    }
}