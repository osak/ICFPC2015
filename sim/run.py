#! /usr/bin/python
import subprocess
import os
import sys

SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__))
JAR_PATH = os.path.join(SCRIPT_DIR, 'sim.jar')
LIB_PATH = os.path.join(SCRIPT_DIR, 'lib')
args = ['java', '-cp', '{0}:{1}/*'.format(JAR_PATH, LIB_PATH), 'icfpc.Main'] + sys.argv[1:]
subprocess.call(args)