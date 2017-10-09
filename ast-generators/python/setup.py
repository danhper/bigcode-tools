import sys
from setuptools import setup, find_packages


def compute_install_requires():
    if sys.version_info[0] == 2:
        return ["scandir"]
    elif sys.version_info[0] == 3:
        return []


setup(
    name="bigcode_ast",
    version="0.1.0",
    packages=find_packages(),
    install_requires=compute_install_requires(),
    extras_require={
        "test": ["tox", "nose"]
    },
    test_suite="tests"
)
