#! /usr/bin/python

import sys
import urllib2
import urllib


def main():
    solution = sys.stdin.read()
    datadic = {'solution': solution}
    if len(sys.argv) == 2:
        datadic['comment'] = sys.argv[1]
    data = urllib.urlencode(datadic)

    handler = urllib2.HTTPHandler()
    opener = urllib2.build_opener(handler)
    request = urllib2.Request('http://icfpc.osak.jp/ogawa/solution', data=data)
    request.get_method = lambda: "POST"

    try:
        connection = opener.open(request)
    except urllib2.HTTPError,e:
        connection = e
        print e

    # check. Substitute with appropriate HTTP code.
    if connection.code == 200:
        data = connection.read()
        print data

if __name__ == '__main__':
    main()