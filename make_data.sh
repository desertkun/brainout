#!/usr/bin/env bash
source /opt/venv/dev/bin/activate

cd data
find . -name '.DS_Store' -type f -delete
cd ../scripts
python make_data.py client
python make_data.py server
cd ../
