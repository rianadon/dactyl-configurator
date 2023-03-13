.PHONY: all
all: ref-tshort/left.scad ref-tshort/right.scad ref-tshort/right-plate.scad

# Checks out the last commit (Nov 5, 2017) of tshort's repo
target/tshort:
	mkdir target && \
	git clone https://github.com/tshort/dactyl-keyboard target/tshort && \
	cd target/tshort && \
	git checkout 7b26338842899833d0da75a9b0e75466d0553af5 && \
	echo '(load-file "src/dactyl_keyboard/dactyl.clj")' | lein repl

ref-tshort/left.scad: target/tshort
	cp $</things/left.scad $@

ref-tshort/right.scad: target/tshort
	cp $</things/right.scad $@

ref-tshort/right-plate.scad: target/tshort
	cp $</things/right-plate.scad $@
