#! /usr/bin/python

__author__ = 'shunsuke'

from flask import Flask, render_template, Response
import pymongo
from config import MONGO_URI, AKATSUKI_HOST, AKATSUKI_PORT
import logging
import json


logger = logging.getLogger('akatsuki')
app = Flask(__name__)


def get_game_json(prob_id, seed, rev):
    client = pymongo.MongoClient(MONGO_URI)
    query = {'problemId': prob_id, 'seed': seed, 'revision': rev}
    logger.info('/game api hit.')
    logger.info('query: {}'.format(query))
    document = client.kadingel.vis.find_one(query, projection={'_id': False})
    client.close()
    if document is None:
        logger.warning('game not found.')
        return Response(json.dumps({'error': 'specified game not found.'}), content_type='application/json')
    logger.info('game found.')
    return Response(json.dumps(document), content_type='application/json')


@app.route("/game/<int:prob_id>/<int:seed>/<rev>/<int:fr>-<int:to>")
def get_partial_game(prob_id, seed, rev, fr, to):
    return Response(json.dumps({'error': 'This api is temporally stopped.'}), content_type='application/json')


@app.route("/game/<int:prob_id>/<int:seed>/<rev>/")
def get_game(prob_id, seed, rev):
    return get_game_json(prob_id, seed, rev)


@app.route("/board/<int:prob_id>/<int:seed>/<rev>/<int:turn>/")
def get_board(prob_id, seed, rev, turn):
    return Response(json.dumps({'error': 'This api is temporally stopped.'}), content_type='application/json')


@app.route("/output/<int:prob_id>/<int:seed>/<rev>/")
def get_output(prob_id, seed, rev):
    client = pymongo.MongoClient(MONGO_URI)
    query = {'problemId': prob_id, 'seed': seed, 'revision': rev}
    logger.info('/output api hit.')
    logger.info('query: {}'.format(query))
    document = client.kadingel.output.find_one(query, projection={'_id': False})
    client.close()
    if document is None:
        logger.warning('output not found.')
        return Response(json.dumps({'error': 'specified output not found.'}), content_type='application/json')
    logger.info('output found.')
    return Response(json.dumps(document), content_type='application/json')


@app.route("/")
def hello():
    return render_template('index.html')


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    app.run(host=AKATSUKI_HOST, port=AKATSUKI_PORT)