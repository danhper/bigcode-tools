from setuptools import setup, find_packages

with open("README.md", "r") as f:
    LONG_DESCRIPTION = f.read()


setup(
    name="bigcode-fetcher",
    version="0.1.1",
    description="Tool to search and fetch code from GitHub",
    long_description=LONG_DESCRIPTION,
    author="Daniel Perez",
    author_email="tuvistavie@gmail.com",
    url="https://github.com/tuvistavie/bigcode-tools/tree/master/code-fetcher",
    download_url="https://github.com/tuvistavie/bigcode-tools/archive/master.zip",
    include_package_data=True,
    zip_safe=True,
    packages=find_packages(exclude=["tests"]),
    install_requires=["requests", "grequests"],
    scripts=["bin/bigcode-fetcher"],
    extras_require={
        "test": ["nose", "requests_mock"]
    },
    test_suite="tests",
    classifiers=[
        "Development Status :: 4 - Beta",
        "Intended Audience :: Developers",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
        "Programming Language :: Python :: 3"
    ],
)
