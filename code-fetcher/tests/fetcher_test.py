from unittest.case import TestCase
from unittest.mock import patch

from argparse import Namespace

import requests_mock

from bigcode_fetcher import fetcher, constants

from tests import fixtures

import gevent.monkey
gevent.monkey.patch_ssl()


class FetcherTest(TestCase):
    def test_create_search_query(self):
        args = Namespace(keyword="foo")
        self.assertEqual("foo in:name", fetcher.create_search_query(args))

        args = Namespace(**{"keyword": "foo", "in": "description"})
        self.assertEqual("foo in:description", fetcher.create_search_query(args))

        args = Namespace(user="tuvistavie", language="Elixir", stars=">10")
        self.assertEqual("user:tuvistavie language:Elixir stars:>10",
                         fetcher.create_search_query(args))

        args = Namespace(keyword="test", size="1000..10000")
        self.assertEqual("test in:name size:1000..10000", fetcher.create_search_query(args))

    @requests_mock.Mocker()
    def test_run_search(self, mreq):
        mreq.get(constants.SEARCH_URL + "?q=user:tuvistavie&sort=stars",
                 json={"items": [fixtures.JSON_PROJECTS[1]]},
                 request_headers={"Authorization": "token secret-token"},
                 headers={"Link": "<https://api.github.com/search/repositories/2>; rel=\"next\""})
        mreq.get(constants.SEARCH_URL + "/2", json={"items": [fixtures.JSON_PROJECTS[0]]})
        with patch.object(fetcher,
                          "filter_projects_by_license",
                          side_effect=lambda x, _h, _l: x) as mock_filter:
            args = Namespace(user="tuvistavie", sort="stars", max_repos=5, token="secret-token")
            projects = fetcher.run_search(args)
            self.assertEqual(list(reversed(fixtures.PROJECTS)), projects)
            self.assertEqual(2, mock_filter.call_count)

    @requests_mock.Mocker()
    def test_filter_by_license(self, mreq):
        mreq.get(constants.REPO_URL.format(full_name=fixtures.PROJECTS[0].full_name),
                 json={"license": {"spdx_id": "MIT"}})
        mreq.get(constants.REPO_URL.format(full_name=fixtures.PROJECTS[1].full_name),
                 json={"license": {"spdx_id": "LGPL-3.0"}})
        filtered_projects = fetcher.filter_projects_by_license(
            fixtures.PROJECTS,
            constants.DEFAULT_HEADERS,
            constants.DEFAULT_LICENSES)
        self.assertEqual([fixtures.PROJECTS[0]], filtered_projects)
