import os
from os import path
from unittest.case import TestCase
from unittest.mock import patch
import tempfile

from tests import fixtures

from bigcode_fetcher import downloader


class DownlaoderTest(TestCase):
    @classmethod
    def setUpClass(cls):
        cls.subprocess_patch = patch("bigcode_fetcher.downloader.subprocess")

    def setUp(self):
        self.subprocess = self.subprocess_patch.start()
        self.project = fixtures.PROJECTS[0]

    def tearDown(self):
        self.subprocess_patch.stop()

    def test_download_git_project(self):
        downloader.download_git_project(self.project, "output_dir")
        expected_args = ["git", "clone", "--depth", "1", self.project.clone_url, "output_dir"]
        self.subprocess.run.assert_called_once_with(expected_args)

    def test_download_project(self):
        expected_dir = path.join("output_dir", self.project.full_name)
        expected_args = ["git", "clone", "--depth", "1", self.project.clone_url, expected_dir]

        self.assertTrue(downloader.download_project(self.project, "output_dir"))
        self.subprocess.run.assert_called_once_with(expected_args)

    def test_download_existing_project(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            os.makedirs(path.join(tmpdir, self.project.full_name))
            self.assertFalse(downloader.download_project(self.project, tmpdir))
            self.subprocess.run.assert_not_called()

    def test_load_project_from_file(self):
        projects = downloader.load_projects_from_file(fixtures.PROJECTS_PATH)
        self.assertEqual(fixtures.PROJECTS, projects)

    def test_download_projects(self):
        expected_arg1 = ["git", "clone", "--depth", "1", self.project.clone_url,
                         path.join("output_dir", self.project.full_name)]
        expected_arg2 = ["git", "clone", "--depth", "1", fixtures.PROJECTS[1].clone_url,
                         path.join("output_dir", fixtures.PROJECTS[1].full_name)]

        downloader.download_projects(fixtures.PROJECTS, "output_dir")
        self.assertEqual(2, self.subprocess.run.call_count)
        self.subprocess.run.assert_any_call(expected_arg1)
        self.subprocess.run.assert_any_call(expected_arg2)

    def test_full_download_projects(self):
        expected_arg = ["git", "clone", self.project.clone_url,
                        path.join("output_dir", self.project.full_name)]

        downloader.download_projects(fixtures.PROJECTS, "output_dir", full_fetch=True)
        self.assertEqual(2, self.subprocess.run.call_count)
        self.subprocess.run.assert_any_call(expected_arg)
