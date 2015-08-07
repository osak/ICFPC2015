import time
import urllib2
import json
from pymongo import MongoClient

TEAM_ID = 59
MONGO_URI = 'mongodb://localhost:27017/'


def get_json():
    body = urllib2.urlopen('https://davar.icfpcontest.org/rankings.js').read()
    return json.loads(body[10:])


def update():
    data = get_json()
    db = MongoClient(MONGO_URI)
    leaderboarddb = db.leaderboard
    leaderboard = leaderboarddb.leaderboard
    for setting in data['data']['settings']:
        setting_id = setting['setting']
        ranking = setting['rankings']
        for submission in ranking:
            if submission['teamId'] != TEAM_ID:
                continue
            power = submission['power_score']
            score = submission['score']
            tag = submission['tags'][0]
            post = {'problem_id': setting_id, 'power': power, 'score': score, 'tag': tag}
            if not leaderboard.find_one(post):
                print post
                leaderboard.insert_one(post)
    db.close()


def main():
    while True:
        update()
        time.sleep(5)


if __name__ == '__main__':
    main()
