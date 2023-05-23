/** A reimplementation of the JSCAD api, but using the Manifold library. */
import Module, { type ManifoldStatic } from 'manifold-3d'

// @ts-ignore
const globl = (window ?? this ?? global ?? self)

globl.Module = async function(args: any) {
    // @ts-ignore Module can take arguments!
    const mod = await Module(args)
    mod.setup()

    globl.jscadModeling = createModeling(mod)
}

const createModeling = (manifold: ManifoldStatic) => ({

})
