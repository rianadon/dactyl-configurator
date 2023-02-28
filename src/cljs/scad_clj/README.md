# SCAD Implementations

This library contains an adaptation of the Clojure SCAD library for ClojureScript.
The library is very nicely split up into an AST generation phase and an output stage.

I provide two new output options: `jscad`, which outputs JavaScript code, and `jscadjs`, which outputs javascript objets.

Both `jscadjs` and `jscad` have more bugs than `scad` due to the OpenJSCAD language being a bit different and the insufficient time I've spent testing the two libraries. That said, I've fixed a few of these bugs for `jscadjs` so that previews work, but I have not done the same for `jscad`.
