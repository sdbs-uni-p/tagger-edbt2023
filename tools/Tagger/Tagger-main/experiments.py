import json
import sys
import os
from pathlib import Path

import ite
from extract import extract_from_files


def edit_config(threshold, path, ignore_unique, ignore_constant, use_union):
    with open('../default_config.json', "r+", encoding='utf-8') as conf_file:
        exp_config = json.load(conf_file)

        files = []
        directory = os.fsencode(path)
        i = 0
        for file in os.listdir(directory):
            filename = os.fsdecode(file)
            if filename.endswith(".json") and 'spoth' not in filename and 'frozza' not in filename:
                files.append(path + str(i) + '.json')
                i = i + 1

        exp_config['json']['input'] = []
        for f in files:
            exp_config['json']['input'].append(f)

        #exp_config['json']['out_dir'] = path.parent.absolute()
        exp_config['json']['config'][0]['min_sample'] = json.loads(threshold)
        exp_config['json']['config'][0]['ignore_constant_attributes'] = json.loads(ignore_constant)
        exp_config['json']['config'][0]['filter_candidate_list'] = json.loads(ignore_unique) # not sure about this one
        exp_config['json']['config'][0]['only_primitive_if'] = json.loads(use_union)
        conf_file.seek(0)
        conf_file.truncate()
        json.dump(exp_config, conf_file, indent=4)


if __name__ == '__main__':
    threshold = sys.argv[3]
    ignore_constant = sys.argv[5]
    ignore_unique = sys.argv[6]
    use_union = sys.argv[7]

    if sys.argv[1].lower() == 'klettke':
        path = sys.argv[2]
    else:
        path = sys.argv[4]
    edit_config(threshold, path, ignore_unique, ignore_constant, use_union)

    ite.run_experiments('../default_config.json')



def get_schema():
    path = sys.argv[2]
    if sys.argv[1].lower() == 'klettke':
        files = []
        directory = os.fsencode(path)
        i = 0
        for file in os.listdir(directory):
            filename = os.fsdecode(file)
            if filename.endswith(".json") and 'spoth' not in filename and 'frozza' not in filename:
                files.append(path + str(i) + '.json')
                i = i + 1

        return extract_from_files(files)
    if sys.argv[1].lower() == 'spoth':
        with open(path) as schema:
            next(schema)  # skip first line
            s = json.load(schema)
            os.remove(path)
        return s
    if sys.argv[1].lower() == 'frozza':
        with open(path) as schema:
            s = json.load(schema)
            os.remove(path)
        return s
