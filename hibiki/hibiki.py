#! /usr/bin/python
__author__ = 'shunsuke'

from flask import Flask, render_template
import pymongo
from config import MONGO_URI, HIBIKI_HOST, HIBIKI_PORT
import logging
import time
import datetime


logger = logging.getLogger('hibiki')
app = Flask(__name__)


def get_all_output():
    client = pymongo.MongoClient(MONGO_URI)
    logger.info('collect all output.')
    cursor = client.kadingel.output.find(projection={'_id': False})
    documents = list(cursor.sort([('revision', pymongo.DESCENDING), ('problemId', pymongo.ASCENDING), ('seed', pymongo.ASCENDING)]))
    client.close()

    # filter duplicated docs
    result = []
    used_tuple = set()
    for document in documents:
        tup = (document['runDateUtc'], document['problemId'], document['seed'])
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
