visdump: solution
	mkdir -p visdump
	python src/python/visdump.py sim/run.py problems output visdump

submit: solution
	bash allsubmit.sh

solution-with-debug: solution.exe
	mkdir -p output
	mkdir -p aidebug
	python src/python/runner.py ./solution.exe problems output aidebug

solution: solution.exe
	mkdir -p output
	mkdir -p aidebug
	python src/python/runner.py ./solution.exe problems output

CPP_SOURCE = ai/ai.cpp
solution.exe: $(CPP_SOURCE)
	c++ -std=c++11 -O3 -o $@ $<

solver-source: gachi-source.tar.gz

SOLVER_DEPENDENCY = $(CPP_SOURCE) src/python/runner.py src/python/translate.py Makefile README
gachi-source.tar.gz:
	tar czf gachi-source.tar.gz $(SOLVER_DEPENDENCY)

clean:
	rm -rf solution.exe output visdump aidebug gachi-source.tar.gz
