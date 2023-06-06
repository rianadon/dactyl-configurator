.PHONY : all build test
build: target/dactyl.js target/proto/manuform.ts target/proto/original.ts

test:
	$(MAKE) -C test

all: build test

target/proto/manuform.ts: src/proto/manuform.proto
	npx protoc --ts_out target --proto_path src $<

target/proto/original.ts: src/proto/original.proto
	npx protoc --ts_out target --proto_path src $<

target/dactyl.js: $(shell find src/cljs -type f)
	npx shadow-cljs release dactyl
