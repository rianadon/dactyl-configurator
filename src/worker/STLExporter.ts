/**
 * STL Exporter, adapted from three.js
 * https://github.com/mrdoob/three.js/blob/master/examples/jsm/exporters/STLExporter.js
 * 
 * I've adapted it to handle meshes from the Manifold library.
 * The meshes are stored very similar to indexed geometry in three.js,
 * so the code looks pretty similar. It's actually simpler since I only deal with
 * a single mesh instaed of a full scene.
 */

import type { Mesh } from 'manifold-3d';
import { Vector3 } from 'three/src/math/Vector3'

interface Options {
    binary?: boolean
}

export default function parse(mesh: Mesh, options: Options = {}) {
    options = Object.assign({
        binary: false
    }, options);

    const binary = options.binary;

    let output: any;
    let offset = 80; // skip header

    if (binary === true) {
        const triangles = mesh.numTri;
        const bufferLength = triangles * 2 + triangles * 3 * 4 * 4 + 80 + 4;
        const arrayBuffer = new ArrayBuffer(bufferLength);
        output = new DataView(arrayBuffer);
        output.setUint32(offset, triangles, true); offset += 4;
    } else {
        output = '';
        output += 'solid exported\n';
    }

    const vA = new Vector3();
    const vB = new Vector3();
    const vC = new Vector3();
    const cb = new Vector3();
    const ab = new Vector3();
    const normal = new Vector3();

    // indexed geometry
    for (let j = 0; j < mesh.triVerts.length; j += 3) {
        const a = mesh.triVerts[j + 0]; 
        const b = mesh.triVerts[j + 1];
        const c = mesh.triVerts[j + 2];

        writeFace(a, b, c, mesh.vertProperties);
    }

    if (binary === false) {
        output += 'endsolid exported\n';
    }

    return output;

    function writeFace(a: number, b: number, c: number, positionAttribute: Float32Array) {
        vA.fromArray(positionAttribute, a*3);
        vB.fromArray(positionAttribute, b*3);
        vC.fromArray(positionAttribute, c*3);

        writeNormal(vA, vB, vC);

        writeVertex(vA);
        writeVertex(vB);
        writeVertex(vC);

        if (binary === true) {
            output.setUint16(offset, 0, true); offset += 2;
        } else {
            output += '\t\tendloop\n';
            output += '\tendfacet\n';
        }
    }

    function writeNormal(vA: Vector3, vB: Vector3, vC: Vector3) {
        cb.subVectors(vC, vB);
        ab.subVectors(vA, vB);
        cb.cross(ab).normalize();

        normal.copy(cb).normalize();

        if (binary === true) {
            output.setFloat32(offset, normal.x, true); offset += 4;
            output.setFloat32(offset, normal.y, true); offset += 4;
            output.setFloat32(offset, normal.z, true); offset += 4;
        } else {
            output += '\tfacet normal ' + normal.x + ' ' + normal.y + ' ' + normal.z + '\n';
            output += '\t\touter loop\n';
        }
    }

    function writeVertex(vertex: Vector3) {
        if (binary === true) {
            output.setFloat32(offset, vertex.x, true); offset += 4;
            output.setFloat32(offset, vertex.y, true); offset += 4;
            output.setFloat32(offset, vertex.z, true); offset += 4;
        } else {
            output += '\t\t\tvertex ' + vertex.x + ' ' + vertex.y + ' ' + vertex.z + '\n';
        }
    }
}