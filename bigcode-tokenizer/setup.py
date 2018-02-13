from setuptools import setup, find_packages


with open("README.md", "r") as f:
    LONG_DESCRIPTION = f.read()


setup(
    name="bigcode-tokenizer",
    version="0.1.0",
    description="Tool to tokenize source code",
    long_description=LONG_DESCRIPTION,
    author="Daniel Perez",
    author_email="tuvistavie@gmail.com",
    packages=find_packages(),
    url="https://github.com/tuvistavie/bigcode-tools/tree/master/bigcode-tokenizer",
    download_url="https://github.com/tuvistavie/bigcode-tools/archive/master.zip",
    scripts=["bin/bigcode-tokenizer"],
    install_requires=["pygments"],
    extras_require={"test": ["nose"]},
    test_suite="tests",
    classifiers=[
        "Development Status :: 4 - Beta",
        "Intended Audience :: Developers",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
        "Programming Language :: Python :: 3"
    ],
)
