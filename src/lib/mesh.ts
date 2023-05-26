/**
   Convert the solid geometries outputted by openjscad to models that threejs can load.
*/

import { BufferGeometry, BufferAttribute } from "three";

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

interface Mesh {
    vertices: Float32Array,
    normals: Float32Array,
}

export function fromMesh(mesh: Mesh) {
    const geo = new BufferGeometry();
    geo.setAttribute('position', new BufferAttribute(mesh.vertices, 3))
    geo.setAttribute('normal', new BufferAttribute(mesh.normals, 3))

    return [geo]

}
