class Project:
    def __init__(self, json_repo):
        self.id = json_repo["id"] # pylint: disable=invalid-name
        self.full_name = json_repo["full_name"]
        self.name = json_repo["name"]
        self.html_url = json_repo["html_url"]
        self.clone_url = json_repo["clone_url"]
        self.language = json_repo["language"]
        self.stargazers_count = json_repo["stargazers_count"]
        self.size = json_repo["size"]
        self.fork = json_repo["fork"]
        self.created_at = json_repo["created_at"]
        self.updated_at = json_repo["updated_at"]
        self.license = json_repo.get("license")

    def __eq__(self, other):
        return self.full_name == other.full_name

    def __hash__(self):
        return hash(self.full_name)
