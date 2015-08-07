
cpp_solution: solution.exe
	python src/python/runner.py ./solution.exe problems output

sample_solution:
	python src/python/runner.py src/python/sample_solution.py problems output

CPP_SOURCE=src/cpp/main.cpp
solution.exe: $(CPP_SOURCE)
	c++ -O3 -o $@ $<