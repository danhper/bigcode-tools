import tempfile
import json
import os
from os import path
import shutil
from tests import TestCase

from bigcode_ast import ast_bulk_processor



class ASTBulkProcessorTest(TestCase):
    def setUp(self):
        self.output_dir = tempfile.mkdtemp(prefix="py-bigcode-ast")

    def tearDown(self):
        shutil.rmtree(self.output_dir)

    def test_process_files(self):
        pattern = path.join(self.fixtures_path, "sources/*.py")
        ast_bulk_processor.process_files(pattern, self.output_dir)
        expected_files = {"files.txt", "asts.json"}
        self.assertEqual(set(os.listdir(self.output_dir)), expected_files)
        with open(path.join(self.output_dir, "files.txt"), "r") as f:
            files = [filename for filename in f.read().split("\n") if filename]
        self.assertEqual(len(files), 2)

        with open(path.join(self.output_dir, "asts.json"), "r") as f:
            asts = [json.loads(ast) for ast in f.read().split("\n") if ast]
        self.assertEqual(len(asts), 2)

        for i, filename in enumerate(files):
            if filename.endswith("simple.py"):
                self.assertEqual(asts[i], self.load_expected("simple"))
