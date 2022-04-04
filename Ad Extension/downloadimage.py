import urllib.request
import os
import shutil
from adblockparser import AdblockRules

DIRECTORY = 'measurements/'
FILTER_LIST = ['https://easylist.to/easylist/easyprivacy.txt']
ADBLOCK_RULES = AdblockRules(FILTER_LIST)

def process_file(filename, dir):
    f = open(filename, 'r')
    count = 0
    for url in f.readlines():
        print(url)
        if ADBLOCK_RULES.should_block(url):
            img_name = os.path.join(dir, f"ad{count}.jpg")
            urllib.request.urlretrieve(url, img_name)
            count = count + 1

def main():
    measurement_number = 0
    for file in sorted(os.listdir(DIRECTORY)):
        filepath = DIRECTORY + file
        if os.path.isfile(filepath) and filepath[-4:] == ".txt":
            dirname = os.path.join(DIRECTORY, f"meas{measurement_number}")
            os.makedirs(dirname)
            newfilepath = os.path.join(dirname, file)
            shutil.copyfile(filepath, newfilepath)
            process_file(newfilepath, dirname)
            measurement_number += 1

if __name__ == "__main__":
    main()
