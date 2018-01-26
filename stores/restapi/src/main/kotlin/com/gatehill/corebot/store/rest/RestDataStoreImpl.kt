package com.gatehill.corebot.store.rest

import com.gatehill.corebot.store.DataStore
import com.gatehill.corebot.store.DataStorePartition
import com.gatehill.corebot.store.rest.config.StoreSettings
import com.gatehill.corebot.util.jsonMapper
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url
import java.lang.reflect.Field

/**
 * A store that delegates to a RESTful backend.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class RestDataStoreImpl : DataStore {
    private val partitions = mutableMapOf<String, DataStorePartition<*, *>>()

    @Suppress("UNCHECKED_CAST")
    override fun <K, V> partitionForClass(partitionId: String, valueClass: Class<V>): DataStorePartition<K, V> =
            partitions[partitionId] as DataStorePartition<K, V>? ?:
                    RestDataStorePartitionImpl<K, V>(valueClass).apply { partitions[partitionId] = this }
}

/**
 * A REST partition.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
private class RestDataStorePartitionImpl<in K, V>(private val clazz: Class<V>) : DataStorePartition<K, V> {
    private val client by lazy {
        Retrofit.Builder()
                .baseUrl(StoreSettings.baseUrl)
                .build()
                .create(CrudApi::class.java)
    }

    private fun mapValues(value: V) = StoreSettings.valueMap.map { (source, target) ->
        val sourceField = getAccessibleField(source)
        target to sourceField.get(value) as String
    }

    private fun getAccessibleField(fieldName: String): Field = clazz.getDeclaredField(fieldName).apply {
        isAccessible = true
    }

    /**
     * Maps the fields in the value class to fields in the request body.
     */
    override fun set(key: K, value: V) {
        val body = mutableMapOf(StoreSettings.keyField to key as String).apply {
            putAll(mapValues(value))
        }
        executeUpdate(body)
    }

    /**
     * Fetches the current values and maps them onto a new instance of the value class.
     */
    override fun get(key: K): V? {
        val fetchUrl = "${StoreSettings.resource}?${StoreSettings.keyField}=$key"

        return client.fetch(fetchUrl).execute().let { response ->
            if (response.isSuccessful) {
                val value = clazz.newInstance()

                StoreSettings.valueMap.forEach { (source, target) ->
                    val sourceField = getAccessibleField(source)
                    sourceField.set(value, jsonMapper.readValue(response.body().byteStream(), Map::class.java)[target])
                }
                value

            } else {
                throw IllegalStateException(response.errorBody().string())
            }
        }
    }

    /**
     * Sets each mapped value to an empty string.
     */
    override fun remove(key: K) {
        val body = mutableMapOf(StoreSettings.keyField to key as String).apply {
            putAll(StoreSettings.valueMap.map { (_, target) ->
                target to ""
            })
        }
        executeUpdate(body)
    }

    private fun executeUpdate(body: MutableMap<String, String>) {
        client.update(StoreSettings.resource,
                RequestBody.create(MediaType.parse("application/json"), jsonMapper.writeValueAsBytes(body))).execute()
    }
}

/**
 * Models a simple CRUD API.
 */
interface CrudApi {
    @GET
    fun fetch(@Url url: String): Call<ResponseBody>

    @POST
    fun update(@Url url: String, @Body body: RequestBody): Call<Void>
}
