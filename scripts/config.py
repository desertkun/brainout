import os.path, time
import os
import sys
import getopt
import struct
import re
import shutil  # temp, for copyfile
import xml.dom.minidom
import string
import zipfile
import random

SRC = '../data/'
DST = '../bin/'
SCRIPTS = []


class Script(object):
    def __init__(self, name, method, src, dst):
        self.name = name
        self.method = method
        self.src = src
        self.dst = dst

    def call(self, platform):
        print "Executing script '{0}'...".format(self.name)
        self.method(self.src, self.dst, platform)


def register_script(name, script, input_directory, output_directory=None):
    if output_directory is None:
        output_directory = os.path.join(DST, input_directory)

    input_directory = os.path.join(SRC, input_directory)
    SCRIPTS.append(Script(name, script, input_directory, output_directory))


def make_data(platform):
    for script in SCRIPTS:
        script.call(platform)


def last_modified_file(directory):
    if not os.path.isdir(directory):
        return 0

    best_time = 0
    for f in os.listdir(directory):
        tm = 0
        if os.path.isdir(f):
            tm = last_modified_file(os.path.join(directory, f))
        else:
            tm = os.path.getmtime(os.path.join(directory, f))
        if best_time < tm:
            best_time = tm

    return best_time


def make_zipfile(output_filename, source_dir):
    relroot = os.path.abspath(os.path.join(source_dir, ".."))
    with zipfile.ZipFile(output_filename, "w", zipfile.ZIP_DEFLATED) as zip:
        for root, dirs, files in os.walk(source_dir):
            # add directory (needed for empty dirs)
            zip.write(root, os.path.relpath(root, relroot))
            for file in files:
                filename = os.path.join(root, file)
                if os.path.isfile(filename):  # regular files only
                    arcname = os.path.join(os.path.relpath(root, relroot), file)
                    zip.write(filename, arcname)


def check_dir(directory):
    if not os.path.exists(directory):
        os.makedirs(directory)
