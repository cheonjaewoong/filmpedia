package io.woong.filmpedia.repository

import io.woong.filmpedia.data.Person
import io.woong.filmpedia.network.PeopleService
import io.woong.filmpedia.network.TmdbClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PeopleRepository {

    private val service: PeopleService = TmdbClient.instance.create(PeopleService::class.java)

    fun fetchDetail(
        key: String,
        id: Int,
        lang: String,
        onResponse: (person: Person?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        val response = service.getDetail(personId = id, apiKey = key, language = lang)

        if (response.isSuccessful) {
            onResponse(response.body())
        } else {
            onResponse(null)
        }
    }
}
