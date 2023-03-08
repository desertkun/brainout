
import config
import scripts
import os
import sys

platform = sys.argv[1]

print("Building for platform {0}".format(platform))

config.register_script(
    'Copy binaries',
    scripts.copy,
    'binary',
    config.DST)

config.register_script(
    'Prebuild packages',
    scripts.prebuild_packages,
    'packages',
    os.path.join(config.SRC, 'tmp', platform))

config.register_script(
    'Build packages',
    scripts.process_packages,
    os.path.join('tmp', platform), config.DST)

config.register_script(
    'Generate web.json',
    scripts.webjson,
    os.path.join('tmp', platform),
    os.path.join(config.DST, 'web'))

config.check_dir(config.DST)
config.make_data(platform)
