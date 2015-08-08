OUTPUT_ALL = output/problem_0.json output/problem_1.json output/problem_2.json output/problem_3.json output/problem_4.json output/problem_5.json output/problem_6.json output/problem_7.json output/problem_8.json output/problem_9.json output/problem_10.json output/problem_11.json output/problem_12.json output/problem_13.json output/problem_14.json output/problem_15.json output/problem_16.json output/problem_17.json output/problem_18.json output/problem_19.json output/problem_20.json output/problem_21.json output/problem_22.json output/problem_23.json output/problem_24.json
VISDUMP_ALL = visdump/problem_0.json visdump/problem_1.json visdump/problem_2.json visdump/problem_3.json visdump/problem_4.json visdump/problem_5.json visdump/problem_6.json visdump/problem_7.json visdump/problem_8.json visdump/problem_9.json visdump/problem_10.json visdump/problem_11.json visdump/problem_12.json visdump/problem_13.json visdump/problem_14.json visdump/problem_15.json visdump/problem_16.json visdump/problem_17.json visdump/problem_18.json visdump/problem_19.json visdump/problem_20.json visdump/problem_21.json visdump/problem_22.json visdump/problem_23.json visdump/problem_24.json
SUBMIT_ALL = submit/problem_0.json submit/problem_1.json submit/problem_2.json submit/problem_3.json submit/problem_4.json submit/problem_5.json submit/problem_6.json submit/problem_7.json submit/problem_8.json submit/problem_9.json submit/problem_10.json submit/problem_11.json submit/problem_12.json submit/problem_13.json submit/problem_14.json submit/problem_15.json submit/problem_16.json submit/problem_17.json submit/problem_18.json submit/problem_19.json submit/problem_20.json submit/problem_21.json submit/problem_22.json submit/problem_23.json submit/problem_24.json

default: solution.exe

visdump-all: $(VISDUMP_ALL)

solution-all: output-all
output-all: $(OUTPUT_ALL)

submit-all: $(SUBMIT_ALL)

submit/%: output/% solution.exe
	./play_icfp2015 -f problems/$* | ./submit.py

summary/%: visdump/%
	python src/python/summary.py $<

visdump/%: output/%
	mkdir -p visdump
	sim/run.py problems/$* output/$* > visdump/$* 2> /dev/null

output/%: solution.exe
	mkdir -p output
	./play_icfp2015 -f problems/$* > output/$*

CPP_SOURCE = ai/AI/lightningAI.cpp ai/evaluation/lightningeval.cpp ai/main.cpp ai/util.cpp
solution.exe: $(CPP_SOURCE)
	c++ -std=c++11 -O3 -o $@ $^

solver-source: gachi-source.tar.gz

SOLVER_DEPENDENCY = $(CPP_SOURCE) src/__init__.py src/python/__init__.py src/python/runner.py src/python/translate.py Makefile README play_icfp2015
gachi-source.tar.gz:
	tar czf gachi-source.tar.gz $(SOLVER_DEPENDENCY)

clean:
	rm -rf solution.exe output visdump aidebug gachi-source.tar.gz

.PRECIOUS: output/%

# Following targets are deprecated.
solution-with-debug: solution.exe
	mkdir -p output
	mkdir -p aidebug
	python src/python/runner.py ./solution.exe problems output aidebug
