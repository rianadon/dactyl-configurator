import type { ManifoldStatic, Mesh } from "manifold-3d"
import * as Dactyl from '../../target/dactyl.js'
import Module from '../assets/manifold'
import manifoldWasmUrl from '../assets/manifold.wasm?url'
import { createModeling, type Modeling } from "./modeling.js"
import scadWasmUrl from '../assets/openscad.wasm?url'
import stlExport from "./STLExporter.js"

(Module as (a: any) => Promise<ManifoldStatic>)({
    locateFile: () => manifoldWasmUrl,
    print: console.log,
    printErr: console.error
}).then(main).catch(e => console.error(e))

const message = (type: string, data: any) => postMessage({ type, data })

function main(manifold: ManifoldStatic) {
    manifold.setup()
    manifold.setCircularSegments(100) // Make the circles look pretty and precise!
    const modeling = createModeling(manifold)
    message("scriptsinit", null)

    onmessage = (event) => {
        const { type, data } = event.data
        console.log('Worker received', type, data)
        switch (type) {
            case 'csg': return generateCSG(data, modeling)
            case 'stl': return generateSTL(data, modeling)
            case 'scad': return generateSCAD(data)
            case 'scadstl': return generateSCAD_STL(data)
        }
    }
}

function generateCSG(config: any, modeling: Modeling) {
    try {
        message('csg', Dactyl.generate(config, modeling))
    } catch (e) {
        console.error(e)
        message("csgerror", serializeErr(e))
    }
}

function generateSTL(config: any, modeling: Modeling) {
    try {
        const mesh: Mesh = Dactyl.generateManifold(config, modeling).getMesh()
        message('stl', stlExport(mesh, { binary: true }))
    } catch (e) {
        console.error(e)
        message('log', 'Error generating model') 
    }
}

function generateSCAD(config: any) {
    message('scad', Dactyl.generateSCAD(config))
}

function generateSCAD_STL(config: any) {
    (self as any).OpenSCAD = {
        noInitialRun: true,
        locateFile: () => scadWasmUrl,
        onRuntimeInitialized: () => onSCADInit(config),
        print: logSCAD,
        printErr: logSCAD,
    }
    import('../assets/openscad.wasm.js').catch(e => {
        message('log', 'Error loading OpenSCAD JS library')
    })
}

function logSCAD(content: string) {
    if (content == 'Could not initialize localization.') {
        // Replace the confusing localization message with something nicer.
        content = 'Starting render...'
    }
    message('log', content)
}

function onSCADInit(config: any) {
    const source = Dactyl.generateSCAD(config)
    const OpenSCAD = (self as any).OpenSCAD
    OpenSCAD.FS.writeFile('/source.scad', source)
    OpenSCAD.callMain(['/source.scad', '-o', 'out.stl'])
    message('stl', OpenSCAD.FS.readFile('/out.stl'))
}

function serializeErr(err: Error) {
    return {
        name: err.name,
        message: err.message,
        stack: err.stack
    }
}