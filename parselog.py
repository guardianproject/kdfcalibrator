#!/usr/bin/python

import csv,sys,os

def usage():
    print("%s <logcat file>" %(sys.argv[0]))
    sys.exit(1)

if len(sys.argv) != 2:
    usage()

if not os.path.exists(sys.argv[1]):
    usage()

reader = csv.reader(open(sys.argv[1], 'rb'), delimiter=' ')

version_block = ''
calculations_block = ''
for line in reader:
    if len(line) < 5:
        continue

    print line
    if line[3] == "KDFIterationCalibratorTest-sysinfo":
       version_block += " ".join(line[5:])
    if line[3] == "KDFIterationCalibratorTest-calc":
       calculations_block += " ".join(line[5:])

print(version_block)

