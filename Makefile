jar:
	sbt -mem 4000 assembly

python_deps:
	pip install -r requirements.txt
