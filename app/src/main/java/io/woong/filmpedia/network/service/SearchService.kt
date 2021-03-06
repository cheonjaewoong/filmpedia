package io.woong.filmpedia.network.service

import io.woong.filmpedia.data.movie.Movies
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchService {

    @GET("search/movie")
    fun getMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = true,
        @Query("region") region: String = "US",
        @Query("year") year: Int? = null,
        @Query("primary_release_year") primaryReleaseYear: Int? = null
    ): Call<Movies>
}
