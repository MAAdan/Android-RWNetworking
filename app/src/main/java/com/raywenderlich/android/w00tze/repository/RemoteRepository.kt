package com.raywenderlich.android.w00tze.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import com.raywenderlich.android.w00tze.app.Constants
import com.raywenderlich.android.w00tze.app.Injection
import com.raywenderlich.android.w00tze.app.isNullOrBlankOrNullString
import com.raywenderlich.android.w00tze.model.Gist
import com.raywenderlich.android.w00tze.model.Repo
import com.raywenderlich.android.w00tze.model.User
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import java.io.IOException
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by MiguelAngel on 28/6/18.
 */

object RemoteRepository : Repository {
    private const val TAG = "RemoteRepository"
    private const val LOGIN = "w00tze"
    private val api = Injection.provideGitHubApi()

    override fun getRepos(): LiveData<List<Repo>> {
        val liveData = MutableLiveData<List<Repo>>()
        api.getRepos(LOGIN).enqueue(object : Callback<List<Repo>> {
            override fun onFailure(call: Call<List<Repo>>?, t: Throwable?) {

            }

            override fun onResponse(call: Call<List<Repo>>?, response: Response<List<Repo>>?) {
                if (response != null) {
                    liveData.value = emptyList()
                }
            }
        })

        return liveData
    }

    override fun getGists(): LiveData<List<Gist>> {
        val liveData = MutableLiveData<List<Gist>>()

        FetchAsyncTask("/users/$LOGIN/gists", ::parseGists, { gists ->
            liveData.value = gists
        }).execute()

        return liveData
    }

    override fun getUser(): LiveData<User> {
        val liveData = MutableLiveData<User>()

        FetchAsyncTask("/users/$LOGIN", ::parseUser, { user ->
            liveData.value = user
        }).execute()

        return liveData
    }

    private fun <T> fetch(path: String, parser: (String) -> T): T? {
        try {
            val url = Uri.parse(Constants.fullUrlString(path)).toString()
            val jsonString = getUrlAsString(url)
            return parser(jsonString)
        } catch (e: IOException) {
            Log.e(TAG, "Error retriving path: $path ::: ${e.localizedMessage}")
        } catch (e: JSONException) {
            Log.e(TAG, "Error retriving path: $path ::: ${e.localizedMessage}")
        }

        return null
    }

    private fun parseRepos(jsonString: String): List<Repo> {
        val repos = mutableListOf<Repo>()

        val reposArray = JSONArray(jsonString)
        for (i in 0 until reposArray.length()) {
            val repoObject = reposArray.getJSONObject(i)
            val repo = Repo(repoObject.getString("name"))
            repos.add(repo)
        }

        return repos
    }

    private fun parseGists(jsonString: String): List<Gist> {
        val gists = mutableListOf<Gist>()

        val gistsArray = JSONArray(jsonString)
        for (i in 0 until gistsArray.length()) {
            val gistObject = gistsArray.getJSONObject(i)
            val gist = Gist(
                    gistObject.getString("created_at"),
                    gistObject.getString("description")
            )
            gists.add(gist)
        }

        return gists
    }

    private fun parseUser(jsonString: String): User {
        val userObject = JSONObject(jsonString)
        val id = userObject.getLong("id")
        val name = if (userObject.getString("name").isNullOrBlankOrNullString()) "" else userObject.getString("name")
        val login = userObject.getString("login")
        val company = if (userObject.getString("company").isNullOrBlankOrNullString()) "" else userObject.getString("company")
        val avatarUrl = userObject.getString("avatar_url")

        return User(
                id,
                name,
                login,
                company,
                avatarUrl
        )
    }

    private class FetchAsyncTask<T> (val path: String, val parser: (String) -> T, val callback: (T) -> Unit)
        : AsyncTask<(T) -> Unit, Void, T>() {

        override fun doInBackground(vararg params: ((T) -> Unit)?): T? {
            return fetch(path, parser)
        }

        override fun onPostExecute(result: T) {
            super.onPostExecute(result)
            if( result != null ) {
                callback(result)
            }
        }
    }
}