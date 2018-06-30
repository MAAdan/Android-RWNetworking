package com.raywenderlich.android.w00tze.repository

import retrofit2.Call
import com.raywenderlich.android.w00tze.model.Repo
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by MiguelAngel on 30/6/18.
 */
interface GitHubApi {
    @GET("users/{user}/repos")
    fun getRepos(@Path("user") user: String): Call<List<Repo>>
}