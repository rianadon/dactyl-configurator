/**
 * Garbage collection for manifold operations.
 * Based on https://github.com/elalish/manifold/blob/72935213e8411c0158374b7c4c54c281c3070fc1/bindings/wasm/examples/worker.ts
 */

import type { Manifold, ManifoldStatic } from "manifold-3d";

// manifold member functions that returns a new manifold
const memberFunctions = [
    'add', 'subtract', 'intersect', 'trimByPlane', 'refine', 'warp',
    'setProperties', 'transform', 'translate', 'rotate', 'scale', 'mirror',
    'asOriginal', 'decompose'
];
// top level functions that constructs a new manifold
const constructors = [
    'cube', 'cylinder', 'sphere', 'tetrahedron', 'extrude', 'revolve', 'union',
    'difference', 'intersection', 'compose', 'levelSet', 'smooth', 'show', 'only',
    'setMaterial', 'Manifold'
];

export type GC = () => void

/** Keeps track of Manifold objects created.
    When the returned method is called, delete all objects. */
export default function createGC(module: ManifoldStatic): GC {
    // Setup memory management, such that users don't have to care about
    // calling `delete` manually.
    // Note that this only fixes memory leak across different runs: the memory
    // will only be freed when the compilation finishes.

    const manifoldRegistry = new Array<Manifold>();
    for (const name of memberFunctions) {
        //@ts-ignore
        const originalFn = module.Manifold.prototype[name];
        //@ts-ignore
        module.Manifold.prototype['_' + name] = originalFn;
        //@ts-ignore
        module.Manifold.prototype[name] = function(...args: any) {
            //@ts-ignore
            const result = this['_' + name](...args);
            manifoldRegistry.push(result);
            return result;
        };
    }

    for (const name of constructors) {
        //@ts-ignore
        const originalFn = module[name];
        //@ts-ignore
        module[name] = function(...args: any) {
            const result = originalFn(...args);
            manifoldRegistry.push(result);
            return result;
        };
    }

    return function() {
        for (const obj of manifoldRegistry) {
            // decompose result is an array of manifolds
            if (obj instanceof Array)
                for (const elem of obj) elem.delete();
            else
                // @ts-ignore
                obj.delete();
        }
        console.debug('Disposed of', manifoldRegistry.length, 'objects')
        manifoldRegistry.length = 0;
    };
}
