# Beyla
A dumb search engine but it works.

## Crawler
Made in Golang
Robot.txt cache using LRU https://github.com/bluele/gcache (it's totally useless because i'm only referencing wikipedia)

```bash
go run .
```

## Indexer
Made in Python
Stop words: https://raw.githubusercontent.com/stopwords-iso/stopwords-fr/refs/heads/master/stopwords-fr.txt
// TODO trouver une solution pour qrimprimerexportercréer
```bash
python3 indexer.py
```

## Outlink
Made in Golang
// TODO mettre les whitelist et blacklist dans des fichiers txt
```bash
go run .
```

## Client
Made in Angular

```bash
pnpm start
```

## Query server
Made in JAVA / Spring
```bash
set -a
. ../../.local.env
set +a
mvn clean package spring-boot:repackage
java -jar target/query-1.0-SNAPSHOT.jar
```
Docker (just use docker compose)

```bash
docker build -t beyla-query:latest . -f src/query/Dockerfile
```

## Local
start docker-compose.local.yaml then individually each applications

## Server
I recommend not to use docker-compose-active.yaml. start the crawler, outlink and indexer just for indexing. During indexing or after, start docker-compose-passive.yaml to get the interface. Eventually stop indexing, you are not google, and I haven't design this program for this.

## Redis
Note for debug
```bash
redis-cli -p 26300 --scan --pattern '*' | xargs -L 1 redis-cli -p 26300 DUMP
redis-cli -p 26305 --scan --pattern '*' | xargs -L 1 redis-cli -p 26305 DUMP
docker exec -it 9 /bin/bash
redis-cli -p 26305 SADD queue "https://fr.wikipedia.org/wiki/Brunissage"
```

List all documents in mongo db

```bash
mongosh
use indexation
var collections = db.getCollectionNames();
for(var i = 0; i< collections.length; i++){    
   print('Collection: ' + collections[i]);
   db.getCollection(collections[i]).find().forEach(printjson);
}
```