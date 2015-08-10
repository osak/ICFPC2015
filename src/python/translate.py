import json
import sys
from StringIO import StringIO
import input_mapper
from phrases import known_phrases

class Rand(object):
    def __init__(self, seed):
        self.state = seed

    def get(self):
        ret = self.state >> 16 & (2 ** 15 - 1)
        self.state = (self.state * 1103515245 + 12345) % (2 ** 32)
        return ret


def translate_single(config, seed, phrases=None):
    if phrases is None:
        phrases = []
    output = StringIO()
    print >> output, config['height'], config['width']
    print >> output, len(config['units'])
    for unit in config['units']:
        print >> output, unit['pivot']['x'], unit['pivot']['y'], len(unit['members'])
        for member in unit['members']:
            print >> output, member['x'], member['y']
    print >> output, len(config['filled'])
    for cell in config['filled']:
        print >> output, cell['x'], cell['y']
    print >> output, config['sourceLength']
    rand = Rand(seed)
    for i in xrange(config['sourceLength']):
        print >> output, rand.get() % len(config['units'])
    print >> output, len(phrases)
    for phrase in phrases:
        print >> output, ''.join(map(str, input_mapper.command_to_move(phrase)))
    return output.getvalue()


def translate(config, phrases=None):
    output = StringIO()
    for seed in config['sourceSeeds']:
        output.write(translate_single(config, seed, phrases))
    return output.getvalue()


def main():
    if not (2 <= len(sys.argv) <= 3):
        print >>sys.stderr, '[USAGE] {} [input.json] [seed id]'.format(sys.argv[0])
        print >>sys.stderr, 'use seed id to specify seed. if not specified, input for all seed is generated.'
        exit(1)
    path = sys.argv[1]
    with open(path) as reader:
        config = json.load(reader)
    if len(sys.argv) == 2:
        print translate(config, known_phrases)
    else:
        print translate_single(config, config['sourceSeeds'][int(sys.argv[2])], known_phrases)


if __name__ == '__main__':
    main()