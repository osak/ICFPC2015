
w = frozenset({"p", "'", "!", ".", "0", "3"})
e = frozenset({"b", "c", "e", "f", "y", "2"})
sw = frozenset({"a", "g", "h", "i", "j", "4"})
se = frozenset({"l", "m", "n", "o", " ", "5"})
cw = frozenset({"d", "q", "r", "v", "z", "1"})
ccw = frozenset({"k", "s", "t", "u", "w", "x"})
ignored = frozenset({"\r", "\n", "\t"})

W = 0
E = 1
SW = 2
SE = 3
CW = 4
CCW = 5
IGNORED = 6

mapper = {w: W, e: E, sw: SW, se: SE, cw: CW, ccw: CCW, ignored: IGNORED}
move_name = ['w', 'e', 'sw', 'se', 'cw', 'ccw', 'ignored']

valid = frozenset()
for cs in mapper:
    valid = valid | cs

com2move = dict()
move2com = dict()
for char in valid:
    for cset, idx in mapper.items():
        if char in cset:
            com2move[char] = idx
            move2com[idx] = char


def command_to_move(string):
    return [com2move[char] for char in string]


def default_command(move):
    return move2com[move]


if __name__ == '__main__':
    s = raw_input()
    for c in s:
        c = c.lower()
        if c not in valid:
            print '!!! invalid character:', c
            exit(1)
        idx = command_to_move(c)
        if idx == IGNORED:
            continue
        print move_name[idx]
