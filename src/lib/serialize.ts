import {Manuform} from '../../target/proto/manuform'
import {Original} from '../../target/proto/original'

import manuform from '../assets/manuform.json'
import original from '../assets/original.json'

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
    if (state.keyboard === "original") {
        const diff = {}
        if (!difference2(state.options, original.options, diff)) return "original"
        data = Original.toBinary(diff)
    }
    return state.keyboard + SPLIT_CHAR + btoa(String.fromCharCode(...data));
}

export function deserialize(str: string, fallback: State): State {
    if (str === "manuform") return manuform
    if (str === "original" || str === "lightcycle") return original

    const split = str.split(SPLIT_CHAR)
    if (split.length != 2) return fallback

    let [keyboard, b64] = split
    if (keyboard === "lightcycle") keyboard = "original" // For compatibility
    const data = Uint8Array.from(atob(b64), c => c.charCodeAt(0))

    let options: object = null
    if (keyboard === "manuform")
        options = recreate2(Manuform.fromBinary(data), manuform.options)
    if (keyboard === "original")
        options = recreate2(Original.fromBinary(data), original.options)
    if (!options) return fallback

    return { keyboard, options }
}
