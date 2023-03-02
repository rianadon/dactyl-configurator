import {Manuform} from '../../target/proto/manuform'
import {Lightcycle} from '../../target/proto/lightcycle'

export interface FieldSchema {
    var: string
    name: string
    help?: string
    min?: number
    max?: number
    options?: {
        name: string
        value: string
    }[]
    fields?: FieldSchema[]
}

export type Schema = FieldSchema[]

interface FieldInfo {
    T: (() => { fields: FieldInfo[] }) | number;
    name: string
    jsonName: string
    options: any[]
}

function fieldToSchema(ns: string, field: FieldInfo) {
    const schema: FieldSchema = {
        var: field.jsonName,
        name: field.options[ns + ".name"],
        help: field.options[ns + ".help"],
        min: field.options[ns + ".min"],
        max: field.options[ns + ".max"],
        angle: field.options[ns + ".angle"],
        options: field.options[ns + ".dropdown"],
    }
    if (typeof field.T === "function") {
        schema.fields = fieldsToSchema(ns, field.T().fields)
    }
    return schema
}

function fieldsToSchema(ns: string, fields: readonly any[]) {
    return fields.map(f => fieldToSchema(ns, f))
}

export const ManuformSchema = fieldsToSchema("dactyl", Manuform.fields)

export const LightcycleSchema: Schema = fieldsToSchema("dactyl", Lightcycle.fields)
