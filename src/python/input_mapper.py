
w = frozenset({"p", "'", "!", ".", "0", "3"})
e =  frozenset({"b", "c", "e", "f", "y", "2"})
sw =  frozenset({"a", "g", "h", "i", "j", "4"})
se =  frozenset({"l", "m", "n", "o", " ", "5"})
cw =  frozenset({"d", "q", "r", "v", "z", "1"})
ccw = frozenset({"k", "s", "t", "u", "w", "x"})

mapper = {w: 'w', e:'e', sw:'sw', se:'se', cw:'cw', ccw: 'ccw'}

if __name__ == '__main__':
    s = raw_input()
    for c in s:
        for cset, name in mapper.items():
            if c.lower() in cset:
                print name
