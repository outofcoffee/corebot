# Mirror store

A store implementation that mirrors writes to the backing store to another, separate, store implementation.

This can be helpful for updating a REST API endpoint when you write data to a store.

The mirror store supports selection of the item to mirror using a JsonPath expression.

Enable the Mirror store implementation with:

    DATA_STORE_IMPL=com.gatehill.corebot.store.mirror.WriteMirrorDataStoreImpl
