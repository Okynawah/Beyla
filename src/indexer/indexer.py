import redis
import time
import json
import re
from pymongo import MongoClient
from datetime import datetime

def is_valid_word(word):
    return not (re.search(r'[a-zA-Z]', word) and re.search(r'[0-9]', word))

def process_data(data, weights, stopwords):
    result_dict = {}

    # update dict for headers
    for index, weight in weights.items():
        headers = data.get(index, [])
        for header in headers:
            update_dict(result_dict, header, weight)
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
    rraw = redis.Redis(host='localhost', port=26300, decode_responses=True)
    rprocessed = redis.Redis(host='localhost', port=26301, decode_responses=True)
    mData = MongoClient("mongodb://localhost:27017/")["indexation"]["windex"]
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
            mData.insert_one({
                "url": data.get("Url"),
                "title": data.get("Title"),
                "keywords": result_dict,
                "description": "",
                "indexed_at": datetime.utcnow(),
                "backlink": 0
            })
            rprocessed.sadd("queue", json.dumps({"url": data["Url"], "outlink": data["Outlink"]}))

        except json.JSONDecodeError as e:
            print("Erreur JSON :", e)

if __name__ == "__main__":
    main()
