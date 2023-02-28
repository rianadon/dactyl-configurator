const dactyl = require('../target/dactyl_node.cjs')
const modeling = require('@jscad/modeling')

const config = require('../src/assets/manuform.json')
// console.log(config.options.keys)
config.options.keys.thumbCount = 'three'
config.options.misc.keycaps = true

const model = dactyl.generateJSCAD(config)
console.log(model)
