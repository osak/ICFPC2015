__author__ = 'shunsuke'

from flask import Flask, render_template, jsonify
from pymongo import MongoClient
from config import MONGO_URI, AKATSUKI_HOST, AKATSUKI_PORT
import logging

logger = logging.getLogger('akatsuki')
app = Flask(__name__)


@app.route("/board/<int:prob_id>/<int:seed>/<rev>/<int:turn>/")
def get_board(prob_id, seed, rev, turn):
    client = MongoClient(MONGO_URI)
    query = {'problemId': prob_id, 'seed': seed, 'revision': rev, 'turn': turn}
    logger.info('query: {}'.format(query))
    document = client.kadingel.vis.find_one(query, projection={'_id': False})
    client.close()
    if document is None:
        logger.warning('board not found.')
        return jsonify({'error': 'specified board not found.'})
    logger.info('board found.')
    return jsonify(document)


@app.route("/")
def hello():
    return render_template('index.html')


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    app.run(host=AKATSUKI_HOST, port=AKATSUKI_PORT)