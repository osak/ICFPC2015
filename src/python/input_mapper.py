
w = frozenset({"p", "'", "!", ".", "0", "3"})
e =  frozenset({"b", "c", "e", "f", "y", "2"})
sw =  frozenset({"a", "g", "h", "i", "j", "4"})
se =  frozenset({"l", "m", "n", "o", " ", "5"})
cw =  frozenset({"d", "q", "r", "v", "z", "1"})
ccw = frozenset({"k", "s", "t", "u", "w", "x"})
ignored = frozenset({"\r", "\n", "\t"})

mapper = {w: 'w', e:'e', sw:'sw', se:'se', cw:'cw', ccw: 'ccw', ignored: 'ignored'}
valid = frozenset()
for cs in mapper:
    valid = valid | cs

if __name__ == '__main__':
    s = raw_input()
    for c in s:
        if c not in valid:
            print '!!! invalid character:', c
            exit(1)
        for cset, name in mapper.items():
            if name == 'ignored':
                continue
            if c.lower() in cset:
                print name

