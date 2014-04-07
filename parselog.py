#!/usr/bin/python
"""
Parses AppThwack logcat csv files for KDF iteration data
"""

import csv,sys,os

def usage():
    print("%s <logcat file>" %(sys.argv[0]))
    sys.exit(1)

if len(sys.argv) == 1:
    usage()
version_block = ''
calculations_block = ''
for arg in sys.argv[1:]:
    if not os.path.exists(arg):
        usage()

    reader = csv.reader(open(arg, 'rb'), delimiter=' ')

    for line in reader:
        if len(line) < 5:
            continue

        if line[3] == "KDFIterationCalibratorTest-sysinfo":
            version_block += line[4] + '\n'

        if line[3] == "KDFIterationCalibratorTest-calc":
            calculations_block += line[4] + '\n'

print(version_block)
print(calculations_block)

