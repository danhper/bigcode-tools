SEARCH_URL = "https://api.github.com/search/repositories"
REPO_URL = "https://api.github.com/repos/{full_name}"
DEFAULT_HEADERS = {"Accept": "application/vnd.github.drax-preview+json"}
DEFAULT_LICENSES = ",".join([
    "MIT",
    "Apache-2.0",
    "MPL-2.0",
    "BSD-2-Clause",
    "BSD-3-Clause",
    "BSD-4-Clause",
    "MS-PL"
])
DEFAULT_SIZE = "1000..100000"
DEFAULT_STARS = ">=10"
