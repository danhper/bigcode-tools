from setuptools import setup, find_packages


setup(
    name="bigcode_fetcher",
    version="0.1.0",
    packages=find_packages(),
    install_requires=["request", "grequests"],
    scripts=["bin/bigcode-fetcher"],
    extras_require={
        "test": ["nose", "requests_mock"]
    },
    test_suite="tests"
)
