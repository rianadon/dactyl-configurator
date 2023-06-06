import test from 'ava';
import * as dactyl from '../target/dactyl.js';
import { readFile, writeFile } from 'fs/promises';

function dedent(str) {
    return str.split('\n').map(x => x.substring(2)).join('\n');
}

function sortSCAD(file) {
    const lines = file.split('\n');

    let sectionStack = [{top: []}]; // Build a stack for recursion
    for (let line of lines) {
        line = line.replace(/\.0\b/g, '');
        line = line.replace(/(\d+)\/(\d+)/g, (_, a, b) => a/b)
        line = line.replace(/\d+\.\d+/g, (x => Math.round(x*1000)/1000))
        const {prefix, top} = sectionStack[sectionStack.length - 1];
        if (line.endsWith('{')) {
            sectionStack.push({prefix: line, top: []});
        } else if (line.endsWith('}')) {
            // Sort the lines!
            let sorted;
            if (prefix.trim().startsWith('rotate (a=0,')
                || prefix.trim().startsWith('color')
                || (prefix.trim().startsWith('union') && top.length == 1)) {
                sorted = top.map(dedent).join('\n');
            } else {
                sorted = [prefix, ...top, line].join('\n');
            }
            sectionStack.splice(sectionStack.length - 1, 1);
            if (sectionStack.length == 0) throw new Error('Did not expect "}"');
            if (!(prefix.trim().startsWith('difference') && top.length > 3)) {
                // Ignore the case for now...
                sectionStack[sectionStack.length - 1].top.push(sorted);
            }
        } else {
            top.push(line);
        }
    }
    if (sectionStack.length > 1) throw new Error('Uncloded brackets in file');
    return sectionStack[0].top.join('\n');
}

const readText = async (filename) => await readFile(filename, {encoding: 'utf-8'});
const readSCAD = async (filename) => sortSCAD(await readText(filename));
const generateSCAD = (options) => sortSCAD(dactyl.generateSCAD(options));

test('tshort-right', async t => {
    const manuform = JSON.parse(await readText('test/ref-tshort/right.json'));
    const reference = await readSCAD('test/ref-tshort/right.scad');

    t.is(generateSCAD(manuform), reference, 'Configurations do not match')
});

test('tshort-left', async t => {
    const manuform = JSON.parse(await readText('test/ref-tshort/right.json'));
    manuform.options.misc.rightSide = false;
    const reference = await readSCAD('test/ref-tshort/left.scad');

    t.is(generateSCAD(manuform), reference, 'Configurations do not match')
});

// test('adereth-right', async t => {
//     const original = JSON.parse(await readText('test/ref-adereth/right.json'));
//     const reference = await readSCAD('test/ref-adereth/dactyl-top-right.scad');

//     await writeFile('test/ref-adereth/mydright.scad', generateSCAD(original));
//     await writeFile('test/ref-adereth/sdright.scad', reference);

//     t.is(generateSCAD(original), reference, 'Configurations do not match')
// });

// test('adereth-left', async t => {
//     const original = JSON.parse(await readText('test/ref-adereth/right.json'));
//     const reference = await readSCAD('test/ref-adereth/right.scad');

//     t.is(generateSCAD(original), reference, 'Configurations do not match')
// });
