/**
   Convert the solid geometries outputted by openjscad to models that threejs can load.
*/

import { BufferGeometry, BufferAttribute } from "three";
import { entitiesFromSolids } from "@jscad/regl-renderer"

const VOLUME_MM2_TO_FILAMENT_M = 0.00042545;
const LENGTH_M_TO_WEIGHT_G = 3; // A very coarse estimate
const WEIGHT_G_TO_COST_USD = 25/1000; // Again, an estimate

export interface FilamentEstimate {
    length: number
    cost: number
}

export function estimateFilament(volume?: number): FilamentEstimate {
    if (!volume) return;
    const length = volume * VOLUME_MM2_TO_FILAMENT_M;
    const cost = length * LENGTH_M_TO_WEIGHT_G * WEIGHT_G_TO_COST_USD;
    return { length, cost };
}

function toFlatTyped(array: any[], typed: Float32Array|Uint16Array) {
    let i = 0;
    for (const position of array) {
        typed[i++] = position[0];
        typed[i++] = position[1];
        typed[i++] = position[2];
    }
}

function toFlatFloat32(array: any[]) {
    const flat = new Float32Array(array.length * 3);
    toFlatTyped(array, flat);
    return flat;
}

function toFlatUint16(array: any[]) {
    const flat = new Uint16Array(array.length * 3);
    toFlatTyped(array, flat);
    return flat;
}

export function fromCSG(csg: any) {
    const entities = entitiesFromSolids({ smoothNormals: false }, csg)
    return entities.map((e: any) => {
        const geo = new BufferGeometry();

        const positions = toFlatFloat32(e.geometry.positions)
        const normals = toFlatFloat32(e.geometry.normals)
        const indices = toFlatUint16(e.geometry.indices)

        geo.setAttribute('position', new BufferAttribute(positions, 3))
        geo.setAttribute('normal', new BufferAttribute(normals, 3))
        geo.setIndex(new BufferAttribute(indices, 1))
        return geo;
    })
}
