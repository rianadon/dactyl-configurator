import {Manuform} from '../../target/proto/manuform'
import {Lightcycle} from '../../target/proto/lightcycle'

import manuform from '../assets/manuform.json'
import lightcycle from '../assets/lightcycle.json'

interface State {
    keyboard: string
    options: object
}

const SPLIT_CHAR = ":"

/** Write the difference between data and reference to output.
    Return true if a difference was found */
function difference(data, reference, output) {
    return Object.keys(data).reduce((diff, key) => {
        if (data[key] != reference[key]) {
            output[key] = data[key]
            return true
        }
        return diff
    }, false)
}

/** difference, but operates on objects of objects */
function difference2(data, reference, output) {
    return Object.keys(data).reduce((diff, key) => {
        output[key] = {}
        return difference(data[key], reference[key], output[key]) || diff
    }, false)
}

function recreate2(data, reference) {
    return Object.keys(data).reduce((diff, key) => {
        diff[key] = {...reference[key], ...data[key]}
        return diff
    }, {})
}

export function serialize(state: State) {
    let data;
    if (state.keyboard === "manuform") {
        const diff = {}
        if (!difference2(state.options, manuform.options, diff)) return "manuform"
        data = Manuform.toBinary(state.options, manuform.options)
    }
    if (state.keyboard === "lightcycle") {
        const diff = {}
        if (!difference2(state.options, lightcycle.options, diff)) return "lightcycle"
        data = Lightcycle.toBinary(state.options, lightcycle.options)
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
