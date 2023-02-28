/** Helper to help me quickly make the proto files */

const manuformDef = require('../src/assets/manuform.json').options
const lightDef = require('../src/assets/lightcycle.json').options

let manuform = {
    keys: {
        columns: {min: 4, max: 7, name: "Number of Columns (4–7)"},
        rows: {min: 3, max: 6, name: "Number of Rows (3–6)"},
        'thumb-count': {options: {six: "6 (Default)", five: "5 (Mini)", four: "4", three: "3 (1.5u)", 'three-mini': "3 (Minidox)", two: "2"}, name: "Thumb Key Count"},
        'last-row': {options: {two: "Two (Default Dactyl Manuform)", zero: "No Last Row", full: "Use All Keys"}, name: "Last Row Key Count"},
        'switch-type': {options: {box: "Box and MX", mx: "MX", 'mx-snap-in': "MX snap-in (one way)", alps: "Alps", choc: "Choc (Experimental)", kailh: "Kailh (Experimental"}, name: "Key Hole"},
        'inner-column': {options: {normie: "Normal", outie: "Use inner column", innie: "Without"}, name: "Inner Index Finger's Column"},
        'hide-last-pinky': {name: "Hide Bottom Pinky?", help: "See Kinesis Advantage's Layout"}
    },
    curve: {
        alpha: {min: 4, max: 21, name: "Column's Curvature"},
        'pinky-alpha': {min: 4, max: 21, name: "Pinky Column's Curvature"},
        beta: {min: 18, max: 64, name: "Row's Curvature"},
        centercol: {name: "Center of the curvature for the column"},
        tenting: {name: "Tenting Angle"},
        'rotate-x': {name: "Rotation around row axis"}
    },
    connector: {
        external: {name: "Use External Holder"},
        type: {options: {rj9: "RJ9", ttrs: "TTRS", usb: "USB only", none: "None"}, name: "USB Hole"},
        'micro-usb': {name: "Micro USB Hole"}
    },
    form: {
        'custom-thumb-cluster': {name: "Custom Thumb Cluster (Experimental)"},
        'height-offset': {min: 4, max: 50, name: "Height Offset"},
        hotswap: {name: "Hotswap Socket"},
        'screw-inserts': {name: "Screw Inserts"},
        stagger: {name: "Stagger"},
        // 'stagger-index-y': 0.0,
        // 'stagger-index-z': 0.0,
        // 'stagger-middle-y': 2.8,
        // 'stagger-middle-z': -6.5,
        // 'stagger-pinky-y': -13.0,
        // 'stagger-pinky-z': 6.0,
        // 'stagger-ring-y': 0.0,
        // 'stagger-ring-z': 0.0,
        // 'thumb-cluster-offset-x': 6.0,
        // 'thumb-cluster-offset-y': -3.0,
        // 'thumb-cluster-offset-z': 7.0,
        // 'thumb-middle-left-offset-x': -53.0,
        // 'thumb-middle-left-offset-y': -26.0,
        // 'thumb-middle-left-offset-z': -12.0,
        // 'thumb-middle-left-tenting-x': 69.0,
        // 'thumb-middle-left-tenting-y': 69.0,
        // 'thumb-middle-left-tenting-z': 69.0,
        // 'thumb-top-left-offset-x': -35.0,
        // 'thumb-top-left-offset-y': -16.0,
        // 'thumb-top-left-offset-z': -2.0,
        // 'thumb-top-left-tenting-x': 69.0,
        // 'thumb-top-left-tenting-y': 69.0,
        // 'thumb-top-left-tenting-z': 69.0,
        // 'thumb-top-right-offset-x': -15.0,
        // 'thumb-top-right-offset-y': -10.0,
        // 'thumb-top-right-offset-z': -5.0,
        // 'thumb-top-right-tenting-x': 69.0,
        // 'thumb-top-right-tenting-y': 69.0,
        // 'thumb-top-right-tenting-z': 69.0,
        'web-thickness': {name: "Web Thickness", help: "Top Part of Keyboard Around Keyholes"},
        'wall-thickness': {name: "Wall Thickness", help: "Around the Keyboard"},
        'wide-pinky': {name: "Wide Pinky"},
        'wire-post': {name: "Wire Posts"}
    },
    misc: {
        keycaps: {name: "Show Keycaps"},
        'right-side': {name: "Right Side"},
        plate: {name: "Generate Plate Instead"},
    }
}

const lightcycle = {
    keys: {
        columns: {min: 4, max: 7, name: "Number of Columns (4–7)"},
        'num_row': {name: "Use Number Row"},
        'last_row': {name: "Use Bottom Row"},
        'switch_type': {options: {box: "Box and MX", mx: "MX", 'mx-snap-in': "MX snap-in (one way)", alps: "Alps", choc: "Choc (Experimental)"}, name: "Key Hole"},
        'thumb_count': {options: {two: "2", three: "3", five: "5", six: "6", eight: "8"}, name: "Thumb Key Count"},
        'hide_last_pinky': {name: "Hide Bottom Pinky?", help: "See Kinesis Advantage's Layout"}
    },
    curve: {
        alpha: {min: 4, max: 21, name: "Column Curvature"},
        beta: {min: 18, max: 64, name: "Row Curvature"},
        tenting: {name: "Tenting Angle"},
        'thumb_alpha': {name: "Thumb's Column Curvature"},
        'thumb_beta': {name: "Thumb's Row Curvature"},
        'thumb_tenting': {name: "Thumb Tenting Angle"},
    },
    connector: {
        external: {name: "Use External Holder"},
    },
    form: {
        hotswap: {name: "Hotswap Socket"},
        'thick_wall': {name: "Thick walls"},
        'manuform_offset': {name: "Dactyl Manuform Columnar Stager"},
        'web_thickness': {name: "Web Thickness", help: "Top Part of Keyboard Around Keyholes"},
        // thumb_offset_x: 52,
        'wide_pinky': {name: "Wide Pinky"},
        // thumb_offset_z: 27,
        border: {name: "Border"},
        'z_offset': {min: 10, max: 34, name: "Height Offset"},
        // thumb_offset_y: 45
    },
    misc: {
        'screw_inserts': {name: "Screw Inserts"},
        'right_side': {name: "Right Side"},
        'use_case': {name: "Include case"},
        plate: {name: "Generate Plate Instead"}
    }
}

manuform = lightcycle
const def = lightDef

for (const [section, vals] of Object.entries(manuform)) {
    const sec = section[0].toUpperCase() + section.substring(1)
    console.log(`message ${sec} {`)
    let i = 1;
    for (const [name, v] of Object.entries(vals)) {
        let t = {
            'string': 'string',
            'boolean': 'bool',
            'number': 'int32'
        }[typeof def[section][name]];
        const n = name.replace(/-/g, '_')
        const opts = [];
        if (v.min) opts.push(`(min) = ${v.min}`)
        if (v.max) opts.push(`(max) = ${v.max}`)
        opts.push(`(name) = "${v.name}"`)
        if (v.help) opts.push(`(help) = "${v.help}"`)
        if (v.options) {
            const options = [];
            for (const [val, name] of Object.entries(v.options)) {
                options.push(`\n(dropdown) = { value: "${val}", name: "${name}" }`)
            }
            opts.push(options.join(','))
        }
        console.log(`  ${t} ${n} = ${i} [${opts.join(', ')}];`)
        i++;
    }
    console.log('}')
}

let i = 1;
for (const [section, vals] of Object.entries(manuform)) {
    const sec = section[0].toUpperCase() + section.substring(1)
    console.log(`${sec} ${section} = ${i} [(name) = "${sec}"];`)
    i++
}
