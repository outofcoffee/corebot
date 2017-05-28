# Redis store

Enable the Redis store implementation with:

    DATA_STORE_IMPL=com.gatehill.corebot.store.redis.RedisDataStoreImpl

## Local server

Example Redis:

    docker run --rm -it -p 6379:6379 -v /tmp/redis:/data --name redis redis

Use the CLI:

    docker run -it --rm --link redis:redis redis bash -c 'redis-cli -h redis'
