import translate
from runner import get_path
import json
import phrases

def main():
    for i in xrange(25):
        problem_path = get_path('../../problems', i)
        with open(problem_path) as reader:
            config = json.load(reader)
        for seed in config['sourceSeeds']:
            dest_path = '../../cpp_input/problem_{0}_{1}.txt'.format(i, seed)
            with open(dest_path, 'w') as writer:
                writer.write(translate.translate_single(config, seed, phrases.known_phrases))


if __name__ == '__main__':
    main()
