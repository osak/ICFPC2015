import sys
import subprocess
import os
import json
from translate import translate_single

aidebug_dir = None


def get_path(input_path, problem_id):
    file_name = 'problem_{}.json'.format(problem_id)
    return os.path.join(input_path, file_name)


def create_single_output(config, seed ,command):
    return {
        'problemId': config['id'],
        'seed': seed,
        'solution': command
    }


def run(exe_path, config, seed):
    input_string = translate_single(config, seed)
    proc = subprocess.Popen([exe_path], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    command, stderr = proc.communicate(input_string)
    if aidebug_dir:
        with open(get_path(aidebug_dir, config['id']) + '.{}.txt'.format(seed), 'w') as writer:
            writer.write(stderr)
    return create_single_output(config, seed, command)


def solve(exe_path, input_path, output_path, problem_id):
    config_path = get_path(input_path, problem_id)
    with open(config_path) as reader:
        config = json.load(reader)
    output = [run(exe_path, config, seed) for seed in config['sourceSeeds']]
    output_path = get_path(output_path, problem_id)
    with open(output_path, 'w') as writer:
        json.dump(output, writer)


def solve_all(exe_path, input_path, output_path):
    for i in xrange(24):
        solve(exe_path, input_path, output_path, i)


def main():
    if not (4 <= len(sys.argv) <= 5):
        print >>sys.stderr, '[USAGE] {} [solver binary] [input dir] [output dir] [aidebug dir]'.format(sys.argv[0])
        print >>sys.stderr, 'aidebug dir to specify debug dump dest. if absent, debug outputs are omitted.'
        exit(1)
    exe_path = sys.argv[1]
    input_path = sys.argv[2]
    output_path = sys.argv[3]
    if len(sys.argv) == 5:
        global aidebug_dir
        aidebug_dir = sys.argv[4]
    solve_all(exe_path, input_path, output_path)


if __name__ == '__main__':
    main()