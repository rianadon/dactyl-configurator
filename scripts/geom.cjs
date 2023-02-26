const dactyl = require('../target/dactyl_node.cjs')
const modeling = require('@jscad/modeling')

const config = require('../src/assets/lightcycle.json')

const model = dactyl.generateJSCAD(config)
console.log(model)
