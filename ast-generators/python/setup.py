from setuptools import setup, find_packages


setup(
    name="bigcode_ast",
    version="0.1.0",
    packages=find_packages(),
    install_requires=[],
    extras_require={
        "test": ["tox"]
    },
    test_suite="tests"
)
