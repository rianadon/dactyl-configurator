<script lang="ts">
 import * as THREE from 'three';
 import { STLLoader } from 'three/examples/jsm/loaders/STLLoader'
 import * as SC from 'svelte-cubed';

 export let model: string

 let geometry: THREE.Geometry
 let cameraZ = 3

 $: (new STLLoader()).load(model, g => {
     const middle = new THREE.Vector3();
     g.computeBoundingBox();
     g.boundingBox.getCenter(middle);
     // g.applyMatrix(new THREE.Matrix4().makeTranslation(
     // -middle.x, -middle.y, -middle.z ) );

     const largestDimension = Math.max(g.boundingBox.max.x,
                                       g.boundingBox.max.y,
                                       g.boundingBox.max.z)
     cameraZ = largestDimension * 4;

     geometry = g
 })


</script>
<SC.Canvas antialias alpha={true}>
	<SC.Mesh geometry={geometry} />
	<SC.PerspectiveCamera position={[0, 0, cameraZ]} />
    <SC.OrbitControls enableZoom={false} />
</SC.Canvas>
