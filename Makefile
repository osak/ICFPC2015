default: solution.exe

visdump-all: solution-all
	mkdir -p visdump
	python src/python/visdump.py sim/run.py problems output visdump

submit-all: solution.exe
	./play_icfp2015 -f problems/* | ./submit.py

solution-with-debug: solution.exe
	mkdir -p output
	mkdir -p aidebug
	python src/python/runner.py ./solution.exe problems output aidebug

solution-all: solution.exe
	mkdir -p output
	mkdir -p aidebug
	python src/python/runner.py ./solution.exe problems output

summary/%: visdump/%
	python src/python/summary.py $<

visdump/%: output/%
	sim/run.py problems/$* output/$* > visdump/$* 2> /dev/null

output/%: solution.exe
	./play_icfp2015 -f problems/$* > output/$*

CPP_SOURCE = ai/ai.cpp
solution.exe: $(CPP_SOURCE)
	c++ -std=c++11 -O3 -o $@ $<

solver-source: gachi-source.tar.gz

SOLVER_DEPENDENCY = $(CPP_SOURCE) src/__init__.py src/python/__init__.py src/python/runner.py src/python/translate.py Makefile README play_icfp2015
gachi-source.tar.gz:
	tar czf gachi-source.tar.gz $(SOLVER_DEPENDENCY)

clean:
	rm -rf solution.exe output visdump aidebug gachi-source.tar.gz
