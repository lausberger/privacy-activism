import urllib.request
import os
from adblockparser import AdblockRules

name = "m"
file = open('folderName.txt', 'r+')
line = file.readLines()
name = name + str(line[0])
file.write(str(int(line[0]) + 1)

if not os.path.exists(name):
    os.makedirs(name)
    my_set.add(name)

raw_rules = ['https://easylist.to/easylist/easyprivacy.txt']
rules = AdblockRules(raw_rules)
file1 = open('exampleimage.txt','r') #replace exampleimage.txt with urls.txt

Lines = file1.readlines()
count = 0
for line in Lines:
    if rules.should_block(line):
        imageStr = "image" + str(count) + ".jpg"
        urllib.request.urlretrieve(line, imageStr)
        count = count + 1
