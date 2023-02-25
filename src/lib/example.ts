import { STLLoader } from 'three/examples/jsm/loaders/STLLoader'
import model from './assets/model.stl?url'

export function exampleGeometry() {
    return new Promise((resolve, reject) => {
        (new STLLoader()).load(model, g=> {
            resolve(g)
        }, undefined, reject)
    })
}
