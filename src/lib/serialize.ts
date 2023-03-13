import {Manuform} from '../../target/proto/manuform'
import {Lightcycle} from '../../target/proto/lightcycle'

import manuform from '../assets/manuform.json'
import lightcycle from '../assets/lightcycle.json'

interface State {
    keyboard: string
    options: object
}

const SPLIT_CHAR = ":"

/** Return true if there is a difference between the two objects */
function areDifferent(data, reference) {
    return Object.keys(data).reduce((diff, key) => {
        return (data[key] != reference[key]) || diff;
    }, false)
}

/** Discard sub-dicts that are the same between two objects of objects. */
function difference2(data, reference, output) {
    return Object.keys(data).reduce((diff, key) => {
        if (areDifferent(data[key], reference[key])) {
            output[key] = data[key];
            console.log(key, 'differs');
            return true;
        }
        return diff;
    }, false)
}

/** Fill in missing sections from a reference dictionary. */
function recreate2(data, reference) {
    return Object.keys(reference).reduce((diff, key) => {
        diff[key] = {...reference[key], ...data[key]}
        return diff
    }, {})
}

export function serialize(state: State) {
    let data;
    if (state.keyboard === "manuform") {
        const diff = {}
        if (!difference2(state.options, manuform.options, diff)) return "manuform"
        data = Manuform.toBinary(diff)
    }
    if (state.keyboard === "lightcycle") {
        const diff = {}
        if (!difference2(state.options, lightcycle.options, diff)) return "lightcycle"
        data = Lightcycle.toBinary(diff)
    }
    return state.keyboard + SPLIT_CHAR + btoa(String.fromCharCode(...data));
}

export function deserialize(str: string, fallback: State): State {
    if (str === "manuform") return manuform
    if (str === "lightcycle") return lightcycle

    const split = str.split(SPLIT_CHAR)
    if (split.length != 2) return fallback

    const [keyboard, b64] = split
    const data = Uint8Array.from(atob(b64), c => c.charCodeAt(0))

    let options: object = null
    if (keyboard === "manuform")
        options = recreate2(Manuform.fromBinary(data), manuform.options)
    if (keyboard === "lightcycle")
        options = recreate2(Lightcycle.fromBinary(data), lightcycle.options)
    if (!options) return fallback

    return { keyboard, options }
}
