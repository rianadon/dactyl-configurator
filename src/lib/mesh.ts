/**
   Convert the solid geometries outputted by openjscad to models that threejs can load.
*/

import { BufferGeometry, BufferAttribute } from "three";

const VOLUME_MM2_TO_FILAMENT_M = 0.00042545;
const LENGTH_M_TO_WEIGHT_G = 3; // A very coarse estimate
const WEIGHT_G_TO_COST_USD = 25/1000; // Again, an estimate
export const SUPPORTS_DENSITY = 0.3;

export interface FilamentEstimate {
    length: number
    cost: number
    fractionKeyboard: number
    keyboard: {
        length: number
        cost: number
    }
}

function filamentCost(volume: number) {
    const length = volume * VOLUME_MM2_TO_FILAMENT_M;
    const cost = length * LENGTH_M_TO_WEIGHT_G * WEIGHT_G_TO_COST_USD;
    return {length, cost}
}

export function estimateFilament(volume: number, supportVolume: number): FilamentEstimate {
    const estimatedVolume = volume + supportVolume * SUPPORTS_DENSITY;

    const {length, cost} = filamentCost(estimatedVolume)
    const keyboard = filamentCost(volume)

    const fractionKeyboard = volume / (volume + supportVolume * SUPPORTS_DENSITY);
    return { length, cost, keyboard, fractionKeyboard };
}

interface Mesh {
    vertices: Float32Array,
    normals: Float32Array,
}

export function fromMesh(mesh: Mesh) {
    const geo = new BufferGeometry();
    geo.setAttribute('position', new BufferAttribute(mesh.vertices, 3))
    if (mesh.faces) geo.setIndex(new BufferAttribute(mesh.faces, 1))
    geo.setAttribute('normal', new BufferAttribute(mesh.normals, 3))

    return geo

}
