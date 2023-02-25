const dactyl = require('../target/dactyl_node.cjs')
const modeling = require('@jscad/modeling')

const config = require('../src/assets/manuform.json')

const model = dactyl.generateManuformJS(config)
console.log(model)
