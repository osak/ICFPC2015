__author__ = 'shunsuke'

from flask import Flask, render_template, Response
import pymongo
from config import MONGO_URI, AKATSUKI_HOST, AKATSUKI_PORT
import logging
import json


logger = logging.getLogger('akatsuki')
app = Flask(__name__)


@app.route("/game/<int:prob_id>/<int:seed>/<rev>/")
def get_game(prob_id, seed, rev):
    client = pymongo.MongoClient(MONGO_URI)
    query = {'problemId': prob_id, 'seed': seed, 'revision': rev}
    logger.info('/game api hit.')
    logger.info('query: {}'.format(query))
    cursor = client.kadingel.vis.find(query, projection={'_id': False})
    documents = list(cursor.sort('turn', pymongo.ASCENDING))
    client.close()
    if documents is None:
        logger.warning('game not found.')
        return Response(json.dumps({'error': 'specified game not found.'}), content_type='application/json')
    logger.info('game found.')

    # filter duplicated docs
    result = []
    used_turn = set()
    for document in documents:
        if document['turn'] not in used_turn:
            result.append(document)
            used_turn.add(document['turn'])
    return Response(json.dumps(result), content_type='application/json')


@app.route("/board/<int:prob_id>/<int:seed>/<rev>/<int:turn>/")
def get_board(prob_id, seed, rev, turn):
    client = pymongo.MongoClient(MONGO_URI)
    query = {'problemId': prob_id, 'seed': seed, 'revision': rev, 'turn': turn}
    logger.info('/board api hit.')
    logger.info('query: {}'.format(query))
    document = client.kadingel.vis.find_one(query, projection={'_id': False})
    client.close()
    if document is None:
        logger.warning('board not found.')
        return Response(json.dumps({'error': 'specified board not found.'}), content_type='application/json')
    logger.info('board found.')
    return Response(json.dumps(document), content_type='application/json')


@app.route("/")
def hello():
    return render_template('index.html')


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    app.run(host=AKATSUKI_HOST, port=AKATSUKI_PORT)