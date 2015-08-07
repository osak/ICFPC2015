visdump: solution
	mkdir visdump
	python src/python/visdump.py sim/run.py problems output visdump

submit: solution
	bash allsubmit.sh

solution: solution.exe
	mkdir output
	python src/python/runner.py ./solution.exe problems output

CPP_SOURCE=ai/ai.cpp
solution.exe: $(CPP_SOURCE)
	c++ -o $@ $<

clean:
	rm solution.exe
	rm -r output
	rm -r visdump