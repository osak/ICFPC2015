import sys
from argparse import ArgumentParser
import input_mapper
from StringIO import StringIO


def optimize(command, spell_list):
    moves = input_mapper.command_to_move(command)
    spell_moves = [filter(lambda move: move != input_mapper.IGNORED, input_mapper.command_to_move(spell)) for spell in spell_list]

    optimized = StringIO()
    pos = 0
    while pos < len(moves):
        next_command = input_mapper.default_command(moves[pos])
        progress = 1
        for i, spell in enumerate(spell_moves):
            npos = pos + len(spell)
            if moves[pos:npos] == spell:
                next_command = spell_list[i]
                progress = len(spell)
                break
        optimized.write(next_command)
        pos += progress
    return optimized.getvalue()


def main():
    parser = ArgumentParser()
    parser.add_argument('-p', metavar='phrase', nargs='*', type=str)
    args = parser.parse_args()
    command = sys.stdin.read()
    print optimize(command, args.p)


if __name__ == '__main__':
    main()