import distutils.core
import json
import sys
import zipfile
import zlib
import hjson
import collections

import codecs
import os
import os.path
import re
import hashlib
import urllib2
import shutil
from base64 import b64encode, b64decode 

from Crypto.PublicKey import RSA 
from Crypto.Signature import PKCS1_v1_5 
from Crypto.Hash import SHA256

try:
    with open('../keys/brainout.pem') as f:
        RSA_KEY = RSA.importKey(f.read())
        KEY_SIGNER = PKCS1_v1_5.new(RSA_KEY)
except IOError:
    print "Warning: no private key, making data without signing!"
    KEY_SIGNER = None

def copy(input_dir, output_dir, platform):
    if not os.path.exists(input_dir):
        os.makedirs(input_dir)

    distutils.dir_util.copy_tree(input_dir, output_dir, update=1)


def prebuild_packages(input_dir, output_dir, platform):
    if not os.path.exists(input_dir):
        os.makedirs(input_dir)

    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    for package in os.listdir(input_dir):
        in_dir = os.path.join(input_dir, package).replace('\\', '/')
        if not os.path.isdir(in_dir):
            continue

        with open(os.path.join(in_dir, "meta.json")) as f:
            meta = json.load(f)

        export = meta.get("export")

        if not export:
            raise RuntimeError("No export defined!")

        exclude_dirs = []

        default_export_rules = export.get("default")
        if default_export_rules is not None:
            default_exclude_dirs = default_export_rules.get("exclude")
            if default_exclude_dirs:
                exclude_dirs.extend(default_exclude_dirs)

        platform_export_rules = export.get(platform)

        if platform_export_rules:
            platform_filter_rules = platform_export_rules.get("filter")
            platform_exclude_dirs = platform_export_rules.get("exclude")
            if platform_exclude_dirs:
                exclude_dirs.extend(platform_exclude_dirs)

            platform_build = platform_export_rules.get("build", True)
            if not platform_build:
                print "Skipped package '{0}'".format(package)
                continue
        else:
            platform_filter_rules = None

        if not os.path.exists(os.path.join(output_dir, package)):
            os.makedirs(os.path.join(output_dir, package))

        print "Copying package '{0}' for platform '{1}'".format(package, platform)

        for address, dirs, files in os.walk(in_dir):
            trimmed_path = unicode(address.replace('\\', '/').replace(in_dir, "", 1))

            if trimmed_path.startswith("/"):
                trimmed_path = trimmed_path[1:]

            if exclude_dirs:
                dirs[:] = [d for d in dirs if d.replace('\\', '/') not in exclude_dirs]

                if trimmed_path in exclude_dirs:
                    continue

            dst_dir = os.path.join(output_dir, package, trimmed_path)

            if not os.path.exists(dst_dir):
                os.makedirs(dst_dir)

            for file_name in files:
                skip = False
                if platform_filter_rules:
                    for rule in platform_filter_rules:
                        if re.match(rule, file_name):
                            skip = True
                            break

                if skip:
                    print "File skipped: {0}".format(file_name)
                    continue

                src = os.path.join(address, file_name).replace('\\', '/')
                dst = os.path.join(dst_dir, file_name).replace('\\', '/')
                if (not os.path.isfile(dst)) or (os.stat(src).st_mtime > os.stat(dst).st_mtime):
                    shutil.copy2(src, dst)

        if platform_export_rules:
            platform_override = platform_export_rules.get("override", None)
            if platform_override:
                for override_with, override_to in platform_override.iteritems():

                    if not os.path.exists(os.path.join(output_dir, package, override_to)):
                        os.makedirs(os.path.join(output_dir, package, override_to))

                    print "Overriding directory {0} with {1}".format(override_to, override_with)

                    distutils.dir_util.copy_tree(
                        os.path.join(input_dir, package, override_with),
                        os.path.join(output_dir, package, override_to))

        smart = meta.get("smart")

        if smart:
            base = smart["base"]
            path = os.path.join(input_dir, package, smart["path"])
            output = os.path.join(output_dir, package, "contents", smart["output"])

            contents = base.get("content", {})
            base["content"] = contents

            process_smart(path, contents, platform_filter_rules)

            if not os.path.exists(os.path.dirname(output)):
                os.makedirs(os.path.dirname(output))

            with open(output, "w") as f:
                json.dump(base, f, indent=4, sort_keys=True, ensure_ascii=False)


def crc(fileName):
    prev = 0
    for eachLine in open(fileName,"rb"):
        prev = zlib.crc32(eachLine, prev)
    return prev & 0xFFFFFFFF


def last_modified_file(directory):
    best_time = 0
    for f in os.listdir(directory):

        if os.path.isdir(f):
            tm = last_modified_file(os.path.join(directory, f))
        else:
            tm = os.path.getmtime(os.path.join(directory, f))
        if best_time < tm:
            best_time = tm

    return best_time


def pack_localization(input_dir, output):
    languages_data = open(os.path.join(input_dir, "languages.json")).read()
    try:
        languages = json.loads(languages_data)
    except ValueError as e:
        print "Error parsing languages " + os.path.join(input_dir, "languages.json")
        print "  *** " + str(e)
        sys.exit(-2)

    if os.path.isfile(os.path.join(input_dir, "external.txt")):
        print "Found external localization"
        with open(os.path.join(input_dir, "external.txt"), "r") as f:
            loc_url = f.readline()

        try:
            loc_contents = urllib2.urlopen(loc_url).read()
        except Exception as e:
            print "####### Failed to download external loc: " + str(e)
        else:
            try:
                loc_body = hjson.loads(loc_contents)
            except Exception as e:
                print "####### Failed to load external loc: " + str(e)
            else:
                for language_name, contents in loc_body.iteritems():
                    f_name = os.path.join(input_dir, language_name + ".json")

                    if os.path.isfile(f_name):
                        try:
                            with open(f_name, "r") as f:
                                old_file = hjson.load(f)
                        except Exception as e:
                            print "####### Failed to open old file: " + str(e)
                        else:
                            diff = set(contents.keys()) - set(old_file.keys())
                            if diff:
                                print "New {0} localizations: {1}".format(language_name, ", ".join(diff))

                    with open(f_name, "w") as f:
                        json.dump(contents, f)
                print "Saved externan localization"

    data = {}
    res = {'data': data}

    for l in languages:
        l_filename = os.path.join(input_dir, l.lower() + ".json")

        if not os.path.isfile(l_filename):
            print "####### Warning: File " + l_filename + " is missing."
            continue

        try:
            with open(l_filename, 'r') as f:
                l_contents = hjson.load(f, encoding='utf-8')
        except Exception as e:
            print "####### Failed to process {0}: {1}".format(l_filename, str(e))
            sys.exit(-1)

        for string_id, string_value in l_contents.iteritems():
            if not string_value:
                continue
            record = data.get(string_id, None)
            if record is None:
                record = {}
                data[string_id] = record

            record[l] = string_value

    with open(output, 'w') as outfile:
        json.dump(res, outfile, indent=4, sort_keys=True, encoding='utf-8', ensure_ascii=True)


def enumerate_textures(input_dir, output_dir):
    textures = []
    for path in os.listdir(input_dir):
        if os.path.isdir(os.path.join(input_dir, path)):
            textures.append(path)

    with open(output_dir, 'w') as outfile:
        json.dump(textures, outfile, indent=4, sort_keys=True, ensure_ascii=False)


def pack_textures(input_dir, output_dir, platform):
    divider = ';' if (os.name == 'nt') else ':'

    if not os.path.isdir(output_dir):
        os.makedirs(output_dir)

    if not os.path.isdir(input_dir):
        return

    for f in os.listdir(input_dir):
        if not f.startswith("."):
            out_file = os.path.join(output_dir, f + ".png")

            if os.path.isfile(out_file):
                if os.path.getmtime(out_file) > last_modified_file(os.path.join(input_dir, f)):
                    continue

            print("Package " + f + " outdated")

            os.system(
                "java -cp \"../tools/gdx.jar" + divider +
                "../tools/gdx-tools-old.jar\" com.badlogic.gdx.tools.imagepacker.TexturePacker2 \"" +
                os.path.join(input_dir, f) + "\" \"" + output_dir + "\" " + f)


def zip_dir(dir_path, zip_file_path):
    if not os.path.isdir(dir_path):
        raise OSError("dirPath argument must point to a directory. '%s' does not." % dir_path)

    parent_dir, dir_to_zip = os.path.split(dir_path)

    def trim_path(path):
        archive_path = path.replace(parent_dir, "", 1)
        if parent_dir:
            archive_path = archive_path.replace(os.path.sep, "", 1)

        archive_path = archive_path.replace(dir_to_zip + os.path.sep, "", 1)
        return archive_path.replace('\\', '/')

    print "Writing file {0}".format(zip_file_path)

    out_file = zipfile.ZipFile(zip_file_path, "w",
                               compression=zipfile.ZIP_STORED)

    hashes = {}

    for (archiveDirPath, dirNames, fileNames) in os.walk(dir_path):
        for fileName in fileNames:
            file_path = os.path.join(archiveDirPath, fileName)
            try:
                res_path = trim_path(file_path)
                hashes[res_path] = crc(file_path)
                out_file.write(file_path, res_path)
            except:
                print "####### Failed to process {0}".format(file_path)
                sys.exit(-1)
        if not fileNames and not dirNames:
            zipInfo = zipfile.ZipInfo(trim_path(archiveDirPath) + "/")
            out_file.writestr(zipInfo, "")

    hashes_file = json.dumps(hashes)
    out_file.writestr('__H', hashes_file)

    digest = SHA256.new()
    digest.update(hashes_file)

    if KEY_SIGNER:
        signature = KEY_SIGNER.sign(digest)
        out_file.writestr('__S', b64encode(signature))

    out_file.close()


def export_package(input_dir, output_file):
    dir_name = os.path.dirname(output_file)
    if not os.path.isdir:
        os.mkdir(dir_name)

    zip_dir(input_dir, output_file)


class CustomHjsonDecoder(hjson.HjsonDecoder):
    def __init__(self, *args, **kwargs):
        super(CustomHjsonDecoder, self).__init__(*args, tfnns_hook=self._parse_tfnns, **kwargs)

    def parse_quoteless_string(self, s):

        if "," in s:
            split = [l.strip() for l in s.split(",")]
            return split

        return s

    def _parse_tfnns(self, context, s, end):
        """Scan s until eol. return string, True, False or None"""

        chf, begin = hjson.decoder.getNext(s, end)
        end = begin

        if chf in hjson.decoder.PUNCTUATOR:
            raise hjson.decoder.HjsonDecodeError(
                "Found a punctuator character when expecting a quoteless string (check your syntax)", s, end);

        while 1:
            ch = s[end:end + 1]

            isEol = ch == '\r' or ch == '\n' or ch == ''
            if isEol or ch == ',' or \
                            ch == '}' or ch == ']' or \
                            ch == '#' or \
                                    ch == '/' and (s[end + 1:end + 2] == '/' or s[end + 1:end + 2] == '*'):

                m = None
                mend = end
                if next: mend -= 1

                if chf == 'n' and s[begin:end].strip() == 'null':
                    return None, end
                elif chf == 't' and s[begin:end].strip() == 'true':
                    return True, end
                elif chf == 'f' and s[begin:end].strip() == 'false':
                    return False, end
                elif chf == '-' or chf >= '0' and chf <= '9':
                    m = hjson.decoder.NUMBER_RE.match(s, begin)

                if m is not None and m.end() == end:
                    integer, frac, exp = m.groups()
                    if frac or exp:
                        res = context.parse_float(integer + (frac or '') + (exp or ''))
                        if int(res) == res and abs(res) < 1e10: res = int(res)
                    else:
                        res = context.parse_int(integer)
                    return res, end

                if isEol:
                    return self.parse_quoteless_string(s[begin:end].strip()), end

            end += 1


def update(d, u):
    for k, v in u.iteritems():
        if isinstance(v, collections.Mapping):
            r = update(d.get(k, {}), v)
            d[k] = r
        elif isinstance(v, list) and isinstance(d.get(k, None), list):
            d[k] = d[k] + u[k]
        else:
            d[k] = u[k]
    return d


def process_smart(input_path, contents, platform_filter_rules):
    if not os.path.isdir(input_path):
        return

    print("Processing smart package '{0}'".format(input_path))

    files_to_process = []

    for path, subdirs, files in os.walk(input_path):
        for name in files:
            if not name.endswith(".txt"):
                continue

            skip = False
            if platform_filter_rules:
                for rule in platform_filter_rules:
                    if re.match(rule, name):
                        skip = True
                        break

            if skip:
                print "File skipped: {0}".format(os.path.join(path, name))
                continue

            files_to_process.append(os.path.join(path, name))

    def process_piece(file_name, text):
        try:
            return hjson.loads(text, cls=CustomHjsonDecoder)
        except hjson.scanner.HjsonDecodeError:
            raise RuntimeError("Failed to process file {0}".format(file_name))

    for file in files_to_process:

        try:
            with codecs.open(file, 'r', encoding="utf-8") as f:
                text = f.read()
        except:
            print "####### Failed to open file " + os.path.join(input_path, file)
            exit(-1)

        pieces = [process_piece(file, piece) for piece in text.split(u"--------")]

        main_content = pieces.pop(0)
        main_id = main_content.pop("id")

        if main_id in contents:
            update(contents[main_id], main_content)
        else:
            contents[main_id] = main_content

        def fix_this(e):
            if isinstance(e, dict):
                if '*THIS*' in e:
                    e[main_id] = e.pop("*THIS*")
                for k, v in e.iteritems():
                    if v == '*THIS*':
                        e[k] = main_id
                    else:
                        fix_this(v)
            if isinstance(e, list):
                if len(e) == 1 and e[0] == '*THIS*':
                    e[0] = main_id
                else:
                    for v in e:
                        fix_this(v)

        for extra in pieces:
            fix_this(extra)
            id = extra.pop("id")
            if id in contents:
                update(contents[id], extra)
            else:
                contents[id] = extra


def process_package(meta, package_name, input_dir, output_dir, platform):
    print("---- Processing package '{0}' for platform '{1}'".format(package_name, platform))

    textures = meta.get("textures")

    if textures:
        output = textures.get("output")
        if not output:
            raise RuntimeError("No textures output defined")

        pack_textures(
            os.path.join(input_dir, "textures"),
            os.path.join(input_dir, "contents", output),
            platform)

    localization = meta.get("localization")

    if localization:
        output = localization.get("output")
        if not output:
            raise RuntimeError("No localization output defined")

        pack_localization(
            os.path.join(input_dir, "localization"),
            os.path.join(input_dir, "contents", output))

    destination_ex = os.path.join(output_dir, platform)

    if not os.path.isdir(destination_ex):
        os.mkdir(destination_ex)

    destination_ex = os.path.join(output_dir, platform, "packages")

    if not os.path.isdir(destination_ex):
        os.mkdir(destination_ex)

    export_package(
        os.path.join(input_dir, "contents"),
        os.path.join(output_dir, platform, "packages", package_name + ".zip"))


def process_packages(input_dir, output_dir, platform):
    print "Processing packages for platform {0}".format(platform)

    for package in os.listdir(input_dir):
        in_dir = os.path.join(input_dir, package)
        if os.path.isdir(in_dir):
            with open(os.path.join(in_dir, "meta.json")) as f:
                meta = json.load(f)

            process_package(meta, package, in_dir, output_dir, platform)


def webjson(inputDir, outputDir, platform):
    with open(os.path.join(inputDir, "base", "contents", "content.json"), "r") as f:
        root = json.load(f)

    with open(os.path.join(inputDir, "base", "contents", "data", "texts.json"), "r") as f:
        texts = json.load(f)

    content_root = root["content"]
    res_content = {}
    res = {
        "content": res_content,
        "text": texts
    }

    for content_id, c in content_root.iteritems():
        if "class" in c:
            clazz = c["class"]

            if clazz in ["content.Medal", "content.Achievement", "content.ContentLockTree"]:
                res_content[content_id] = c

    with open(os.path.join(outputDir, "web.json"), "w") as f:
        f.write(json.dumps(res, f, indent=4, ensure_ascii=False).encode('utf8'))
