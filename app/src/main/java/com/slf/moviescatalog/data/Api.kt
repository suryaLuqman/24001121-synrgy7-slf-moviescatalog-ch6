package com.slf.moviescatalog.data

import android.annotation.SuppressLint
import com.slf.moviescatalog.GetMoviesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface Api {
    @SuppressLint("all")
    @GET("movie/popular")
    fun getPopularMovies(
        @Query("api_key") apiKey: String = "ae595f6daffa988ff6f10f2105f86b13",
        @Query("page") page: Int
    ): Call<GetMoviesResponse>

    @GET("movie/top_rated")
    fun getTopRatedMovies(
        @Query("api_key") apiKey: String = "ae595f6daffa988ff6f10f2105f86b13",
        @Query("page") page: Int
    ): Call<GetMoviesResponse>

    @GET("movie/upcoming")
    fun getUpcomingMovies(
        @Query("api_key") apiKey: String = "ae595f6daffa988ff6f10f2105f86b13",
        @Query("page") page: Int
    ): Call<GetMoviesResponse>
}