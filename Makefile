visdump: solution
	mkdir -p visdump
	python src/python/visdump.py sim/run.py problems output visdump

submit: solution
	bash allsubmit.sh

solution: solution.exe
	mkdir -p output
	mkdir -p aidebug
	python src/python/runner.py ./solution.exe problems output aidebug

CPP_SOURCE=ai/ai.cpp
solution.exe: $(CPP_SOURCE)
	c++ -std=c++11 -O3 -o $@ $<

clean:
	rm solution.exe
	rm -rf output
	rm -rf visdump
	rm -rf aidebug