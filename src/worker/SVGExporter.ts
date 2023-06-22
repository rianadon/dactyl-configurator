/**
 * Export a planar mesh to SVG.
 *
 * The formats are very different (a mesh is made of tesselated triangles, whereas SVG is a single face),
 * so some preprocessing needs to be done.
 *   1) The mesh is filtered so that only faces on the z plane are exported
 *   2) The triangles of the mesh are combined into polygons
 *   3) The polygons are written out to the SVG file.
 */

import type { Mesh } from 'manifold-3d';

interface Options {
    margin?: number
    color?: string
}

export default function svgExport(mesh: Mesh, options: Options) {
    const margin = options?.margin ?? 10
    const color = options?.color ?? "#8080F7"

    const flatFaces: [number, number, number][] = []
    for (let i = 0; i < mesh.triVerts.length; i+=3) {
        // a, b, and c are the points on face
        const [a, b, c] = mesh.triVerts.slice(i, i+3)
        // If the z coordinates of all points are 0, add it.
        if (mesh.vertProperties[a*3+2] == 0 &&
            mesh.vertProperties[b*3+2] == 0 &&
            mesh.vertProperties[c*3+2] == 0) {
            flatFaces.push([a, b, c])
        }
    }

    const boundary = new Set<string>()
    for (const tri of flatFaces) {
        for (const [e0, e1] of [[tri[0], tri[1]], [tri[1], tri[2]], [tri[2], tri[0]]]) {
            if (boundary.has(e1 + ',' + e0)) {
                boundary.delete(e1 + ',' + e0)
            } else {
                boundary.add(e0 + ',' + e1)
            }
        }
    }

    // To process the boundary, I create a queue of edges to their next edge (this map).
    const next = new Map<number, number>()
    for (const b of boundary) {
        const [e0, e1] = b.split(',').map(v => Number(v))
        next.set(e0, e1)
    }

    let minX = 0
    let maxX = 0
    let minY = 0
    let maxY = 0

    const paths: string[] = []
    // Extract a vertex in the boundary, follow the next edges until there is no more vertex, then repeat.
    while (next.size > 0) {
        let vertex = next.keys().next().value
        let path = ''
        while (next.has(vertex)) {
            // Flip the y to preserve how things look in the preview
            const [x, y] = [mesh.vertProperties[vertex*3], -mesh.vertProperties[vertex*3+1]]
            path += `${path.length==0 ? 'M' : 'L'}${x},${y}`
            minX = Math.min(minX, x-margin)
            maxX = Math.max(maxX, x+margin)
            minY = Math.min(minY, y-margin)
            maxY = Math.max(maxY, y+margin)
            const newVertex = next.get(vertex)
            next.delete(vertex)
            vertex = newVertex
        }
        paths.push(path + 'Z')
    }

    return `<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
viewBox="${minX} ${minY} ${maxX-minX} ${maxY-minY}" width="${maxX-minX}mm" height="${maxY-minY}mm"
>
<path fill="${color}" d="${paths.join(' ')}" />
<line x1="${minX+margin}" y1="${maxY-margin/2}" x2="${minX+margin+10}" y2="${maxY-margin/2}" stroke="#96D9D7" />
<text x="${minX+margin+12}" y="${maxY-margin/2+2}" style="font: bold 2mm sans-serif;fill:#96D9D7">= 1cm</text>
</svg>
`
}
