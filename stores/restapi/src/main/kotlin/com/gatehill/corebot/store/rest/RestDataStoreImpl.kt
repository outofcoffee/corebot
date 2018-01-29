package com.gatehill.corebot.store.rest

import com.gatehill.corebot.store.DataStore
import com.gatehill.corebot.store.DataStorePartition
import com.gatehill.corebot.store.rest.config.StoreSettings
import com.gatehill.corebot.store.rest.populator.PopulationStrategy
import com.gatehill.corebot.util.jsonMapper
import com.jayway.jsonpath.JsonPath
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url
import java.util.Objects.nonNull
import kotlin.reflect.KClass

/**
 * A store that delegates to a RESTful backend.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class RestDataStoreImpl : DataStore {
    private val partitions = mutableMapOf<String, DataStorePartition<*, *>>()

    @Suppress("UNCHECKED_CAST")
    override fun <K, V : Any> partitionForClass(partitionId: String, valueClass: KClass<V>): DataStorePartition<K, V> =
            partitions[partitionId] as DataStorePartition<K, V>?
                    ?: RestDataStorePartitionImpl<K, V>(valueClass).apply { partitions[partitionId] = this }
}

/**
 * A REST partition.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
private class RestDataStorePartitionImpl<in K, V : Any>(private val clazz: KClass<V>) : DataStorePartition<K, V> {
    private val client by lazy {
        Retrofit.Builder()
                .baseUrl(StoreSettings.baseUrl)
                .build()
                .create(CrudApi::class.java)
    }

    /**
     * Maps the fields in the value class to fields in the request body.
     */
    override fun set(key: K, value: V) {
        val sourceObject: Map<String, *> = StoreSettings.jsonPath?.let {
            JsonPath.read<Map<String, *>>(jsonMapper.writeValueAsString(value), StoreSettings.jsonPath)
        } ?: run {
            @Suppress("UNCHECKED_CAST")
            jsonMapper.convertValue<Map<*, *>>(value, Map::class.java) as Map<String, *>
        }

        val body = mutableMapOf(StoreSettings.keyField to key as String).apply {
            putAll(StoreSettings.valueMap.map { (source, target) ->
                target to sourceObject[source] as String
            })
        }
        executeUpdate(body)
    }

    /**
     * Fetches the current values and maps them onto a new instance of the value class.
     */
    override fun get(key: K): V? {
        if (nonNull(StoreSettings.jsonPath)) {
            throw IllegalArgumentException("Fetching from REST API not supported when JsonPath is specified")
        }

        val fetchUrl = "${StoreSettings.resource}?${StoreSettings.keyField}=$key"

        return client.fetch(fetchUrl).execute().let { response ->
            if (response.isSuccessful) {
                return response.body().byteStream().use { bodyStream ->
                    if (bodyStream.available() > 0) {
                        val inputs = jsonMapper.readValue(bodyStream, Map::class.java)
                        if (inputs.isNotEmpty()) {
                            @Suppress("UNCHECKED_CAST")
                            PopulationStrategy.infer(clazz).populate(inputs as Map<String, *>)
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }

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
