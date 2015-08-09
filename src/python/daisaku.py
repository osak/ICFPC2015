import sys
from argparse import ArgumentParser
import input_mapper
from StringIO import StringIO

spell_list = ['ei!', 'ia! ia!', "r'lyeh", "in his house at r'lyeh dead cthulhu waits dreaming.", "yogsothoth"]


def optimize(command):
    moves = input_mapper.command_to_move(command)
    spell_moves = [input_mapper.command_to_move(spell) for spell in spell_list]

    optimized = StringIO()
    pos = 0
    while pos < len(moves):
        next_command = input_mapper.default_command(moves[pos])
        for i, spell in enumerate(spell_moves):
            npos = pos + len(spell)
            if moves[pos:npos] == spell:
                next_command = spell_list[i]
                break
        optimized.write(next_command)
        pos += len(next_command)
    return optimized.getvalue()


def main():
    parser = ArgumentParser()
    parser.add_argument('-p', metavar='phrase', nargs='*', type=str)
    args = parser.parse_args()
    command = sys.stdin.read()
    print optimize(command, args.p)


if __name__ == '__main__':
    main()