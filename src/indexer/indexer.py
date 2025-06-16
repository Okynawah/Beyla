import redis
import time
import json
import re
from pymongo import MongoClient
from datetime import datetime
from dotenv import dotenv_values, find_dotenv

def is_valid_word(word):
    return not (re.search(r'[a-zA-Z]', word) and re.search(r'[0-9]', word))

def process_data(data, weights, stopwords):
    result_dict = {}

    # update dict for headers
    for index, weight in weights.items():
        headers = data.get(index, [])
        for header in headers:
            headerWords = header.split(' ')
            if len(headerWords) > 1:
                for word in headerWords:
                    update_dict(result_dict, word, weight)

    # update dict for text including standard header
    spaced_text = data.get("Text").replace("-", " ").replace(".", " ").replace("!", " ").replace(",", " ").replace("\'", " ").replace("_", " ")
    clean_text = re.sub(r"[^\w\ ]+", "", spaced_text, flags=re.UNICODE)
    words_array = re.split(' ', clean_text)
    useful_array = [word for word in words_array if word.lower() not in stopwords]
    valid_words = [word for word in useful_array if is_valid_word(word)]

    for word in valid_words:
        update_dict(result_dict, word, 1)
        
        
    return result_dict

def update_dict(dictionary, word, weight):
    key = word.lower()
    if key in dictionary:
        dictionary[key] += weight
    else:
        dictionary[key] = weight

def main():
    if find_dotenv("../../.local.env"):
        dotenv = dotenv_values("../../.local.env")
    else: 
        dotenv = dotenv_values(".env")
        
    rraw = redis.Redis(host=dotenv.get("BEYLA_RAW"), port=dotenv.get("BEYLA_RAW_PORT"), decode_responses=True)
    rprocessed = redis.Redis(host=dotenv.get("BEYLA_PROCESSED"), port=dotenv.get("BEYLA_PROCESSED_PORT"), decode_responses=True)
    mData = MongoClient("mongodb://" + dotenv.get("BEYLA_DATA") + ":" + dotenv.get("BEYLA_DATA_PORT") + "/")["indexation"]["windex"]
    stopwords = open("stopwords-fr.txt").read().split()

    weights = {"H1": 5, "H2": 3, "H3": 2, "H4": 1, "H5": 1, "H6": 1}

    print("Indexer start...", end="", flush=True)

    while True:
        value = rraw.rpop("queue")
        if value is None:
            print(".", end="", flush=True)
            time.sleep(1)
            continue

        try:
            data = json.loads(value)
            print("Indexing:" + data.get("Url"))
            result_dict = process_data(data, weights, stopwords)
            mSiteData = {
                "title": data.get("Title"),
                "keywords": result_dict,
                "description": "",
                "indexed_at": datetime.utcnow(),
            }
            result = mData.update_one(
                {"url": data.get("Url")}, 
                {
                    "$set": mSiteData,
                },
                upsert=True
            )
            if result.upserted_id:
                mData.update_one(
                    {"_id": result.upserted_id},
                    {"$set": {"backlink": 1}}
                )
            rprocessed.sadd("queue", json.dumps({"url": data["Url"], "outlink": data["Outlink"]}))

        except json.JSONDecodeError as e:
            print("Erreur JSON :", e)

if __name__ == "__main__":
    main()
