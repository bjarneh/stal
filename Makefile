# makefile for stal

JZ=jz


build: javaf
	@$(JZ)

null: javaf
	@$(JZ) -0 -o stal

stal: javaf
	@$(JZ) -o stal

compress: javaf
	@$(JZ) -o stal -z

install: compress
	mv ./stal ./bin

javaf: $(shell find src -type f)

clean:
	@$(JZ) -clean
	@rm -rf stal

doc:
	@$(JZ) -doc


.PHONY: build, javaf, null, stal, compress, install, clean, doc
