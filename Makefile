.PHONY : all
all: target/dactyl_webworker.js target/dactyl_node.cjs target/proto/manuform.ts target/proto/lightcycle.ts

target/proto/manuform.ts: src/proto/manuform.proto
	npx protoc --ts_out target --proto_path src $<

target/proto/lightcycle.ts: src/proto/lightcycle.proto
	npx protoc --ts_out target --proto_path src $<

target/dactyl_webworker.js: $(shell find src/cljs -type f)
	lein cljsbuild once worker

target/dactyl_node.cjs: $(shell find src/cljs -type f)
	lein cljsbuild once node
