visdump: solution
	mkdir -p visdump
	python src/python/visdump.py sim/run.py problems output visdump

submit: solution
	bash allsubmit.sh

solution: solution.exe
	mkdir -p output
	python src/python/runner.py ./solution.exe problems output

CPP_SOURCE=ai/ai.cpp
solution.exe: $(CPP_SOURCE)
	c++ -O3 -o $@ $<

clean:
	rm solution.exe
	rm -r output
	rm -r visdump