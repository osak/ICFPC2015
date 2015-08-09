#! /usr/bin/python
__author__ = 'shunsuke'

from flask import Flask, render_template, request
import pymongo
from config import MONGO_URI, HIBIKI_HOST, HIBIKI_PORT
import logging
import datetime
import urllib2
import json

logger = logging.getLogger('hibiki')
app = Flask(__name__)


def get_poyo_score():
    body = urllib2.urlopen('https://davar.icfpcontest.org/rankings.js').read()
    score_json = json.loads(body[10:])
    result = dict()
    for setting in score_json['data']['settings']:
        setting_id = setting['setting']
        ranking = setting['rankings']
        for submission in ranking:
            if submission['teamId'] != 9:
                continue
            result[setting_id] = submission['score']
    return result


def get_submission_scores(revs, poyo):
    client = pymongo.MongoClient(MONGO_URI)
    logger.info('collect score info.')
    query = {'score': {'$exists': True}, 'revision': {'$in': revs}}
    cursor = client.kadingel.output.find(query, projection={'_id': False})
    documents = list(cursor)
    client.close()

    used_tuple = set()
    revset = set(revs)
    summary, names = dict(), dict()
    for document in documents:
        tup = (document['revision'], document['problemId'], document['seed'])
        if tup not in used_tuple:
            used_tuple.add(tup)
            rev, prob_id, seed = tup
            if prob_id not in summary:
                summary[prob_id] = dict()
            if rev not in summary[prob_id]:
                summary[prob_id][rev] = []
            summary[prob_id][rev].append(document['score'])
            names[rev] = document['comment']
    if poyo:
        revset.add('poyo')
        names['poyo'] = '7th_building'
        for prob_id, score in get_poyo_score().items():
            if prob_id not in summary:
                summary[prob_id] = dict()
            summary[prob_id]['poyo'] = [score]

    best = dict()
    for prob_id, row in summary.items():
        for rev, scores in row.items():
            row[rev] = sum(scores) / len(scores)
        for rev in revs:
            if rev not in row:
                row[rev] = 0
        best[prob_id] = max(row.values())

    return summary, names, best


@app.route("/compare")
def compare():
    revs = request.args.get('revs')
    if revs:
        revs = revs.split(',')
    else:
        revs = []
    poyo = bool(request.args.get('poyo'))
    summary, names, best = get_submission_scores(revs, poyo)
    return render_template('compare.html', revs=names.keys(), names=names, problems=summary.keys(), summary=summary, best=best)


def get_all_output():
    client = pymongo.MongoClient(MONGO_URI)
    logger.info('collect all output.')
    cursor = client.kadingel.output.find(projection={'_id': False})
    documents = list(cursor.sort([('runDateUtc', pymongo.DESCENDING), ('problemId', pymongo.ASCENDING), ('seed', pymongo.ASCENDING)]))
    client.close()
    # filter duplicated docs
    result = []
    used_tuple = set()
    for document in documents:
        tup = (document['revision'], document['problemId'], document['seed'])
        if tup not in used_tuple:
            result.append(document)
            used_tuple.add(tup)
    return result


@app.route("/")
def index():
    outputs = get_all_output()
    output_base_url = 'http://icfpc.osak.jp/akatsuki/{api}/{problemId}/{seed}/{revision}/'
    visualiser_base_url = 'http://icfpc.osak.jp/nastasja/index.html?revision={revision}&problemId={problemId}&seed={seed}'
    for output in outputs:
        output['outputUrl'] = output_base_url.format(api='output', **output)
        output['visualizerUrl'] = visualiser_base_url.format(api='game', **output)
        datetime_obj = datetime.datetime.fromtimestamp(int(output['runDateUtc']))
        output['runDateString'] = datetime_obj.strftime("%Y-%m-%d %H:%M:%S")
        output['elapsedTime'] = '{:0.3f}'.format(output['elapsedTime'])
    return render_template('index.html',
                           outputs=outputs)


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    app.run(host=HIBIKI_HOST, port=HIBIKI_PORT)
