import sys

n = 1
i = 1

while i <= 5:
    n *= i
    i += 1

sys.stdout.write(str(n))
