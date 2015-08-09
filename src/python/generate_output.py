from runner import create_single_output
import sys
import json

def main():
    print json.dumps([create_single_output({'id': 0}, 0, sys.stdin.read().replace('\n', ''), 0)])


if __name__ == '__main__':
    main()