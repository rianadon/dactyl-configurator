.PHONY : all build test
build: target/dactyl.js target/proto/manuform.ts target/proto/lightcycle.ts

test:
	$(MAKE) -C test

all: build test

target/proto/manuform.ts: src/proto/manuform.proto
	npx protoc --ts_out target --proto_path src $<

target/proto/lightcycle.ts: src/proto/lightcycle.proto
	npx protoc --ts_out target --proto_path src $<

target/dactyl.js: $(shell find src/cljs -type f)
	npx shadow-cljs release dactyl
