# makefile for stal

JZ=jz


build:
	@$(JZ) -0 -o stal

pack:
	@$(JZ) -o stal

clean:
	@$(JZ) -clean
	@rm -rf stal

doc:
	@$(JZ) -doc


