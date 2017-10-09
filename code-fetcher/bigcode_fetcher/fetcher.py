import json
from urllib.parse import urlencode
import logging

from bigcode_fetcher.project import Project
from bigcode_fetcher import constants


def request_exception_handler(request, exception):
    logging.warning("failed to fetch %s: %s", request.url, exception)


def filter_projects_by_license(projects, headers, licenses):
    import grequests

    reqs = [grequests.get(constants.REPO_URL.format(full_name=p.full_name), headers=headers)
            for p in projects]
    resps = grequests.map(reqs, exception_handler=request_exception_handler)

    filtered_projects = []
    for i, project in enumerate(projects):
        resp = resps[i]
        if not resp or resp.status_code != 200:
            logging.warning("ignoring %s because no info could be fetched", project.full_name)
            continue

        project_license = resp.json().get("license")
        if not project_license or not project_license.get("spdx_id"):
            continue
        license_id = project_license.get("spdx_id")
        if license_id in licenses:
            project.license = license_id
            filtered_projects.append(project)
    return filtered_projects


def run_search(args):
    # NOTE: must be imported after gevent.monkey
    import requests

    query = {"per_page": 100, "q": create_search_query(args), "sort": args.sort}
    headers = constants.DEFAULT_HEADERS.copy()
    licenses = getattr(args, "licenses", constants.DEFAULT_LICENSES).split(",")
    if getattr(args, "token", None):
        headers["Authorization"] = "token {0}".format(args.token)
    else:
        logging.warning("you did not provide a GitHub authentication token, " +
                        "you may run in a rate limit issue." +
                        "go to https://github.com/settings/tokens to generate a token")

    url = "{0}?{1}".format(constants.SEARCH_URL, urlencode(query))

    fetched_repos = []
    while url and len(fetched_repos) < args.max_repos:
        logging.info("progress: %s/%s", len(fetched_repos), args.max_repos)

        logging.debug("querying GitHub API: %s", url)
        response = requests.get(url, headers=headers)
        if response.status_code != 200:
            logging.error("failed to fetch %s: %s", url, response.text)
            break
        projects = [Project(repo) for repo in response.json()["items"]]
        filtered_projects = filter_projects_by_license(projects, headers, licenses)
        for project in filtered_projects:
            if len(fetched_repos) < args.max_repos:
                fetched_repos.append(project)
        url = response.links.get("next", {}).get("url")

    return fetched_repos


def create_search_query(args):
    query = []
    if getattr(args, "keyword", None):
        query.append(args.keyword)
        if not getattr(args, "in", None):
            query.append("in:name")

    search_fields = ["user", "language", "stars", "fork", "in", "size"]
    for field in search_fields:
        value = getattr(args, field, None)
        if value:
            value = value if isinstance(value, str) else json.dumps(value)
            query.append("{0}:{1}".format(field, value))
    return " ".join(query)


def save_projects_list(projects, output_file):
    project_dicts = [vars(project) for project in projects]
    with open(output_file, "w") as f:
        json.dump(project_dicts, f)


def search_projects_command(args):
    # NOTE: gevent seems to have issues with Python 3.6
    import gevent.monkey
    gevent.monkey.patch_ssl()

    projects = run_search(args)
    save_projects_list(projects, args.output)
