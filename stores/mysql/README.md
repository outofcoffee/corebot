# MySQL store

Enable the MySQL store implementation with:

    DATA_STORE_IMPL=com.gatehill.corebot.store.mysql.MysqlDataStoreImpl
    MYSQL_CONNECTION_STRING=jdbc:mysql://localhost:3306/corebot
    MYSQL_USERNAME=corebot
    MYSQL_PASSWORD=Corebot123!

## Local server

Example MySQL instance:

    docker run --name corebot-mysql -p 3306:3306 -e MYSQL_DATABASE=corebot -e MYSQL_USER=corebot -e MYSQL_PASSWORD=Corebot123! -e MYSQL_RANDOM_ROOT_PASSWORD=yes --rm -it mysql
