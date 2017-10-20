package com.gatehill.corebot.store.mysql

import com.fasterxml.jackson.databind.ObjectMapper
import com.gatehill.corebot.store.DataStore
import com.gatehill.corebot.store.DataStorePartition
import com.gatehill.corebot.store.mysql.config.StoreSettings
import com.gatehill.corebot.util.jsonMapper
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.createMissingTablesAndColumns
import org.jetbrains.exposed.sql.Slf4jSqlLogger
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.rowset.serial.SerialBlob

/**
 * A MySQL store.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class MysqlDataStoreImpl : DataStore {
    private val partitions = mutableMapOf<String, DataStorePartition<*, *>>()

    init {
        Database.connect(StoreSettings.connectionString, driver = "com.mysql.jdbc.Driver", user = StoreSettings.username, password = StoreSettings.password)
        transaction {
            logger.addLogger(Slf4jSqlLogger)
            createMissingTablesAndColumns(KeyValues)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <K, V> partitionForClass(partitionId: String, valueClass: Class<V>): DataStorePartition<K, V> =
            partitions[partitionId] as DataStorePartition<K, V>? ?:
                    MysqlDataStorePartitionImpl<K, V>(jsonMapper, valueClass, partitionId).apply { partitions[partitionId] = this }
}

object KeyValues : IntIdTable() {
    val key = varchar("key", 100).index()
    val partition = varchar("partition", 100).index()
    val value = blob("value")
}

class KeyValue(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<KeyValue>(KeyValues)

    var key by KeyValues.key
    var partition by KeyValues.partition
    var value by KeyValues.value
}

/**
 * A MySQL partition.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
private class MysqlDataStorePartitionImpl<in K, V>(private val mapper: ObjectMapper,
                                                   private val clazz: Class<V>,
                                                   private val partitionId: String) : DataStorePartition<K, V> {

    override fun set(key: K, value: V) {
        transaction {
            logger.addLogger(Slf4jSqlLogger)

            KeyValues.deleteWhere {
                (KeyValues.key eq key) and (KeyValues.partition eq partitionId)
            }

            KeyValue.new {
                this.key = key.toString()
                this.partition = partitionId
                this.value = SerialBlob(mapper.writeValueAsBytes(value))
            }
        }
    }

    override fun get(key: K): V? = transaction {
        logger.addLogger(Slf4jSqlLogger)

        KeyValue.find {
            (KeyValues.key eq key) and (KeyValues.partition eq partitionId)

        }.firstOrNull()?.let {
            mapper.readValue(it.value.binaryStream.use { it.readBytes() }, clazz)
        }
    }

    override fun remove(key: K) {
        transaction {
            logger.addLogger(Slf4jSqlLogger)

            KeyValues.deleteWhere {
                (KeyValues.key eq key) and (KeyValues.partition eq partitionId)
            }
        }
    }
}
