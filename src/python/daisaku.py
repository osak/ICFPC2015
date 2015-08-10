import sys
from argparse import ArgumentParser
import input_mapper
from StringIO import StringIO

HASH_MOD = 2 ** 50
HASH_MASK = HASH_MOD - 1
HASH_BASE = 1009


def rolling_hash(int_list):
    ret = [0]
    for x in int_list:
        ret.append((ret[-1] * HASH_BASE + x) & HASH_MASK)
    return ret


def optimize(command, spell_list):
    moves = input_mapper.command_to_move(command)
    spell_moves = [filter(lambda move: move != input_mapper.IGNORED, input_mapper.command_to_move(spell)) for spell in spell_list]

    hashed_moves = rolling_hash(moves)
    hashed_spells = [rolling_hash(spell)[-1] for spell in spell_moves]
    spell_pow_memo = [pow(HASH_BASE, len(spell), HASH_MOD) for spell in spell_moves]

    optimized = StringIO()
    pos = 0
    while pos < len(moves):
        next_command = input_mapper.default_command(moves[pos])
        progress = 1
        for i, spell in enumerate(spell_moves):
            npos = pos + len(spell)
            if npos > len(moves): continue
            command_hash = (hashed_moves[npos] - hashed_moves[pos] * spell_pow_memo[i] % HASH_MOD + HASH_MOD) % HASH_MOD
            spell_hash = hashed_spells[i]
            if command_hash == spell_hash and moves[pos:npos] == spell:
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