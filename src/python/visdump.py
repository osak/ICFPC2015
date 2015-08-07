import sys
import subprocess
import os


def get_path(input_path, problem_id):
    file_name = 'problem_{}.json'.format(problem_id)
    return os.path.join(input_path, file_name)


def solve(exe_path, input_path, output_path, visdump_path, problem_id):
    config_path = get_path(input_path, problem_id)
    output_path = get_path(output_path, problem_id)
    visdump_path = get_path(visdump_path, problem_id)
    stdout = subprocess.check_output([exe_path, config_path, output_path], stderr=subprocess.PIPE)
    with open(visdump_path, 'w') as writer:
        writer.write(stdout)


def solve_all(exe_path, input_path, output_path, visdump_path):
    for i in xrange(24):
        solve(exe_path, input_path, output_path, visdump_path,i)


def main():
    if not (5 <= len(sys.argv) <= 6):
        print >>sys.stderr, '[USAGE] {} [vis exec] [problem dir] [output dir] [visdump dir] [problem id]'.format(sys.argv[0])
        print >>sys.stderr, 'problem id to specify problem. if absent, all problems are solved.'
        exit(1)
    exe_path = sys.argv[1]
    input_path = sys.argv[2]
    output_path = sys.argv[3]
    visdump_path = sys.argv[4]
    if len(sys.argv) == 5:
        solve_all(exe_path, input_path, output_path, visdump_path)
    else:
        solve(exe_path, input_path, output_path, visdump_path, int(sys.argv[2]))


if __name__ == '__main__':
    main()