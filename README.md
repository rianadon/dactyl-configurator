# Dactyl Web Configurator

A web configurator for the Dactyl mechanical keyboard.
It's based upon the wonderful work of @ibnuda's [web configurator](https://github.com/ibnuda/dactyl-keyboard), which reworks the organizes the configuration options in various commuity edits to the Dactyl keyboard into a JSON structure.

The site uses the wonderful technologies of ClojureScript and WebAssembly to run all computations in the browser. This means you don't need a server to deploy it!

The new developments of this work are:
- All Clojure source files have been translated to ClojureScript, which can easily be run in the browser. The two languages are extremely similar, but there are a few key differences (lack of ratios, macro handling) that need to be taken into account.
- Using @DSchroer's [WebAssembly build of OpenSCAD](https://github.com/DSchroer/openscad-wasm), STL generation can happen entirely within the browser.
- Fast CSG previews are generated using the OpenJSCAD project. It has a different API than OpenSCAD (most significantly, it's based on creating JS objects rather than creating a source file). I've added new back end to the scad-clj library to generate the OpenJSCAD objects.
- Preview the CSG with Three.JS, using the Svelte-cubed bindings.
- The ClojureScript source files are compiled into a web worker, which is run from Svelte/TypeScript frontend.
- Configurations are compressed with protobuf and saved to the URL. This makes it easy to share configurations with others. I've also switched to using camelcase for the json configuration, which makes integrating with protobuf easier.

## Building and running

ClojureScript and protobuf files can be built with the Makefile. Run `make` to build them. They are placed in the `target` directory.
To develop the website, run `npm run dev`.

## Useful Resources
- [Clojurescript interop with javascript](https://lwhorton.github.io/2018/10/20/clojurescript-interop-with-javascript.html)
- [JSCAD user guide](https://openjscad.xyz/dokuwiki/doku.php)
- [The Noble Effort To Put OpenSCAD In The Browser](https://hackaday.com/2022/03/14/the-noble-effort-to-put-openscad-in-the-browser/)
