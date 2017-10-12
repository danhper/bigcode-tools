from setuptools import setup, find_packages

with open("README.md", "r") as f:
    LONG_DESCRIPTION = f.read()


setup(
    name="bigcode-embeddings",
    version="0.1.1",
    description="Tool generate and visualize embeddings from bigcode",
    long_description=LONG_DESCRIPTION,
    author="Daniel Perez",
    author_email="tuvistavie@gmail.com",
    url="https://github.com/tuvistavie/bigcode-tools/tree/master/bigcode-embeddings",
    download_url="https://github.com/tuvistavie/bigcode-tools/archive/master.zip",
    include_package_data=True,
    zip_safe=True,
    packages=find_packages(),
    install_requires=["pandas", "scipy", "numpy", "scikit-learn", "matplotlib", "plotly"],
    scripts=["bin/bigcode-embeddings"],
    extras_require={},
    classifiers=[
        "Development Status :: 4 - Beta",
        "Intended Audience :: Developers",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
        "Programming Language :: Python :: 3"
    ],
)
