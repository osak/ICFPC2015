import json
import sys


def main():
    if len(sys.argv) != 2:
        print >> sys.stderr, "[USAGE] {} [visdump json]".format(sys.argv[0])
        print >> sys.stderr, "show summary of visdump"
        exit(1)
    with open(sys.argv[1]) as reader:
        visdump = json.load(reader)
    print 'SCORE = {0}'.format(visdump['boards'][-1]['score'])


if __name__ == '__main__':
    main()