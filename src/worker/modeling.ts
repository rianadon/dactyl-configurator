import type { Manifold, ManifoldStatic, Mat4, Mesh, Polygons, Vec2, Vec3 } from 'manifold-3d'
import quickhull from 'quickhull3d'
import { Matrix4 } from 'three/src/math/Matrix4'
import { Vector3 } from 'three/src/math/Vector3'
import { Vector2 } from 'three/src/math/Vector2'

function d(m: Manifold|Manifold[]): Manifold {
    if (Array.isArray(m)) {
        if (m.length > 1) throw new Error('To many items to unpack')
        return d(m[0])
    }
    return m
}

function dd(m: Manifold[]): Manifold[] {
    const objs: Manifold[] = []
    for (const obj of m) {
        if (Array.isArray(obj)) objs.push(...dd(obj))
        else if (typeof m !== 'undefined') objs.push(obj)
    }
    return objs
}

function ensureCCW(p: Vec2[]) {
    const ba = new Vector2(p[0][0] - p[1][0], p[0][1] - p[1][1])
    const bc = new Vector2(p[2][0] - p[1][0], p[2][1] - p[1][1])
    if (ba.cross(bc) > 0) p.reverse()
    return p
}

function addVertices(arr: number[][], mesh: Mesh) {
    if (mesh.numProp != 3) throw new Error('Only numProp==3 is supported')
    for (let i = 0; i < mesh.vertProperties.length; i += 3) {
        arr.push([mesh.vertProperties[i],
                  mesh.vertProperties[i+1],
                  mesh.vertProperties[i+2]])
    }
    return arr
}

function toPolygons(mesh: Mesh) {
    const vertices = addVertices([], mesh)
    const tris = mesh.triVerts
    const polygons: { vertices: number[][] }[] = []
    for (let i = 0; i < tris.length; i+=3) {
        polygons.push({
            "vertices": [vertices[tris[i]], vertices[tris[i+1]], vertices[tris[i+2]]]
        })
    }
    return polygons
}

function toDegrees(rad: number) {
    return rad * (180/Math.PI);
}

/** For a set of triangles, fill the interior region by turning each triangle into a
 * quadrilateral that touches the center.
 */
function fillInsides(faces: number[][][]) {
    const vectors = faces.flat().map(v => new Vector2(v[0], v[1]));
    const center = vectors.reduce((prev, current) => prev.add(current), new Vector2(0, 0))
        .divideScalar(vectors.length);
    const centerVert = center.toArray();
    faces.forEach((verts, i) => {
        const distances = verts.map(v => -new Vector2(v[0], v[1]).sub(center).length())
        const furthest = distances.indexOf([...distances].sort()[0])
        if (furthest == 0) faces[i] = [verts[0], verts[1], centerVert, verts[2]]
        else if (furthest == 1) faces[i] = [verts[0], verts[1], verts[2], centerVert]
        else if (furthest == 2) faces[i] = [verts[0], centerVert, verts[1], verts[2]]
    })
}

export const createModeling = (manifold: ManifoldStatic) => ({
    booleans: {
        subtract(...objs: Manifold[]) {
            return manifold.difference(dd(objs))
        },
        union(...objs: Manifold[]) {
            return manifold.union(dd(objs))
        },
        intersection(...objs: Manifold[]) {
            return manifold.intersection(dd(objs))
        },
    },
    colors: {
        colorize(_color, obj: Manifold) {
            return d(obj)
        }
    },
    extrusions: {
        project(opts: any, objs: Manifold[]) {
            const vectors: number[][] = []
            const meshes = dd(objs).map(obj => {
                const epsilon = 0.01
                // @ts-ignore
                if (opts.cut) obj = obj.trimByPlane([0,0,1], -epsilon).trimByPlane([0,0,-1], -epsilon)
                const mesh = obj.getMesh()
                addVertices(vectors, mesh)
                return mesh
            })
            const vertices: Vec2[] = vectors.map(v => [v[0], v[1]])

            if (opts.cut) {
                const faces = meshes.flatMap(m => {
                    const f = []
                    const t = m.triVerts
                    for (let i = 0; i < t.length; i+=3) {
                        f.push([vertices[t[i]], vertices[t[i+1]], vertices[t[i+2]]])
                    }
                    return f
                })
                fillInsides(faces) // NOTE: This goes against the exepcted behavior of cut(). In JSCAD, the inside is not filled.
                                   // However, it is nice to do this for the dactyl bottom plate.
                const flatFaces = faces.flat()
                // @ts-ignore
                const tris = manifold.triangulate(faces) as number[][]
                return tris.map(v => ensureCCW(v.map(i => flatFaces[i])))
            }

            // @ts-ignore
            const tris = manifold.triangulate(vertices) as number[][]
            return tris.map(v => ensureCCW(v.map(i => vertices[i])))
        },
        extrudeLinear(opts: any, polys: Polygons[]) {
            return manifold.extrude(polys[0], opts.height)
        }
    },
    hulls: {
        hull(objs: Manifold[]) {
            // Collect the meshes of the hulled objects
            // And compute the total number of vertices*3
            let arrLen = 0
            const meshes = dd(objs).map(obj => {
                const mesh = obj.getMesh()
                arrLen += mesh.vertProperties.length
                return mesh
            })

            // Collect all vertices so they can be used in the convex hull.
            // Also collect them in the Float32Array vertProperties, so that
            // the generated mesh has access to all of the vertices.
            const vectors: number[][] = []
            const vertProperties = new Float32Array(arrLen)
            let pos = 0
            for (const mesh of meshes) {
                addVertices(vectors, mesh)
                vertProperties.set(mesh.vertProperties, pos)
                pos += mesh.vertProperties.length
            }

            // Perform the Convex Hull!
            const faces = quickhull(vectors)

            // Set the mesh's faces from the faces given by the hull operation.
            const triVerts = new Uint32Array(faces.length * 3)
            faces.forEach((f, i) => triVerts.set(f, i*3))

            return new manifold.Manifold(new manifold.Mesh({
                numProp: 3, vertProperties, triVerts
            }))
        }
    },
    geometries: {
        geom3: {
            toPolygons(obj: Manifold) {
                return toPolygons(d(obj).getMesh())
            }
        }
    },
    maths: {
        mat4: {
            create() {
                return new Matrix4()
            },
            fromRotation(out: Matrix4, rad: number, axis: Vec3) {
                return out.makeRotationAxis(new Vector3(...axis), rad)
            }
        }
    },
    measurements: {
        measureVolume(m: Manifold) {
            return d(m).getProperties().volume
        }
    },
    primitives: {
        cuboid(opts: any) {
            return manifold.cube(opts.size, true)
        },
        cylinder(opts: any) {
            return manifold.cylinder(opts.height, opts.radius, -1, 0, true)
        },
        cylinderElliptic(opts: any) {
            return manifold.cylinder(opts.height, opts.startRadius[0], opts.endRadius[0], 0, true)
        },
        sphere(opts: any) {
            return manifold.sphere(opts.radius, 0)
        },
        polygon(opts: any) {
            // Assume the polygon is convex! The first three points will be a,b,c respectively.
            const p = [...opts.points]
            return ensureCCW(p)
        }
    },
    transforms: {
        translate(translation: Vec3, obj: Manifold) {
            return d(obj).translate(translation)
        },
        mirror({ normal }, obj: Manifold) {
            // @ts-ignore
            return d(obj).mirror(normal)
        },
        rotateX(rad: number, obj: Manifold) {
            return d(obj).rotate([toDegrees(rad), 0, 0])
        },
        rotateY(rad: number, obj: Manifold) {
            return d(obj).rotate([0, toDegrees(rad), 0])
        },
        rotateZ(rad: number, obj: Manifold) {
            return d(obj).rotate([0, 0, toDegrees(rad)])
        },
        transform(mat: Matrix4, obj: Manifold) {
            return d(obj).transform(mat.elements as any)
        }
    }
})

export type Modeling = ReturnType<typeof createModeling>

export function serializeMesh(m: Manifold) {
    const mesh = m.getMesh()

    const cb = new Vector3();
    const ab = new Vector3();
    const normal = []

    const pos = []
    for (let i = 0; i < mesh.vertProperties.length; i += 3) {
        pos.push(new Vector3().fromArray(mesh.vertProperties, i))
    }

    const normals = new Float32Array(mesh.triVerts.length*3)
    const vertices = new Float32Array(mesh.triVerts.length*3)

    let i = 0;
    for (let tri = 0; tri < mesh.triVerts.length; tri += 3) {
        const vA = pos[mesh.triVerts[tri+0]],
              vB = pos[mesh.triVerts[tri+1]],
              vC = pos[mesh.triVerts[tri+2]];
        cb.subVectors(vC, vB);
        ab.subVectors(vA, vB);
        cb.cross(ab).normalize().toArray(normal)

        vertices.set(vA.toArray(), i)
        vertices.set(vB.toArray(), i+3)
        vertices.set(vC.toArray(), i+6)
        normals.set(normal, i)
        normals.set(normal, i+3)
        normals.set(normal, i+6)
        i += 9;
    }

    return { vertices, normals }
}
