# makefile for stal

JZ=jz

build:
	@$(JZ)

stal:
	@$(JZ) -0 -o stal

pack:
	@$(JZ) -o stal

compress:
	@$(JZ) -o stal -z


clean:
	@$(JZ) -clean
	@rm -rf stal

doc:
	@$(JZ) -doc


.PHONY: build, stal, pack, clean, doc
