
w = frozenset({"p", "'", "!", ".", "0", "3"})
e = frozenset({"b", "c", "e", "f", "y", "2"})
sw = frozenset({"a", "g", "h", "i", "j", "4"})
se = frozenset({"l", "m", "n", "o", " ", "5"})
cw = frozenset({"d", "q", "r", "v", "z", "1"})
ccw = frozenset({"k", "s", "t", "u", "w", "x"})
ignored = frozenset({"\r", "\n", "\t"})

E = 0
W = 1
SE = 2
SW = 3
CCW = 4
IGNORED = 5
CW = 6

mapper = {w: W, e: E, sw: SW, se: SE, cw: CW, ccw: CCW, ignored: IGNORED}
move_name = ['e', 'w', 'se', 'sw', 'ccw', 'ignored', 'cw']

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
        idx = com2move[c]
        if idx == IGNORED:
            continue
        print move_name[idx]
