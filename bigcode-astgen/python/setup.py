import sys
from setuptools import setup, find_packages


with open("README.md", "r") as f:
    LONG_DESCRIPTION = f.read()


def compute_install_requires():
    if sys.version_info[0] == 2:
        return ["scandir"]
    elif sys.version_info[0] == 3:
        return []


setup(
    name="bigcode-astgen",
    version="0.2.0",
    description="Tool to search and fetch code from GitHub",
    long_description=LONG_DESCRIPTION,
    author="Daniel Perez",
    author_email="tuvistavie@gmail.com",
    url="https://github.com/tuvistavie/bigcode-tools/tree/master/bigcode-astgen/python",
    download_url="https://github.com/tuvistavie/bigcode-tools/archive/master.zip",
    include_package_data=True,
    zip_safe=True,
    packages=find_packages(exclude=["tests"]),
    scripts=["bin/bigcode-astgen-py"],
    install_requires=compute_install_requires(),
    extras_require={
        "test": ["tox", "nose"]
    },
    test_suite="tests",
    classifiers=[
        "Development Status :: 4 - Beta",
        "Intended Audience :: Developers",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
        "Programming Language :: Python :: 2",
        "Programming Language :: Python :: 3"
    ],
)
