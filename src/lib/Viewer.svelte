<script lang="ts">
 import * as THREE from 'three';
 import * as SC from 'svelte-cubed';

 export let geometries: THREE.Geometry[];

 let canvas;
 let cameraZ = 3

 const cameraFOV = 45;
 const size = new THREE.Vector3();

 $: loadGeometries(geometries)

 function loadGeometries(gs: THREE.BufferGeometry[]) {
     const boundingBox = new THREE.Box3(new THREE.Vector3(-0.1, -0.1, -0.1), new THREE.Vector3(0.1, 0.1, 0.1));
     for (const g of gs) {
         g.computeBoundingBox();
         boundingBox.union(g.boundingBox);
     }

     const middle = new THREE.Vector3();
     boundingBox.getCenter(middle);
     for (const g of gs) {
         g.applyMatrix4(new THREE.Matrix4().makeTranslation(-middle.x, -middle.y, -middle.z));
     }

     boundingBox.getSize(size);
     resize();
 }

 function resize() {
     // https://wejn.org/2020/12/cracking-the-threejs-object-fitting-nut/
     const aspect = canvas ? (canvas.clientWidth / canvas.clientHeight) : 1;
     const fov = cameraFOV * ( Math.PI / 180 );
     const fovh = 2*Math.atan(Math.tan(fov/2) * aspect);
     let dx = size.z / 2 + Math.abs( size.x / 2 / Math.tan( fovh / 2 ) );
     let dy = size.z / 2 + Math.abs( size.y / 2 / Math.tan( fov / 2 ) );
     cameraZ = Math.max(dx, dy) * 1.2;
 }
</script>

<svelte:window on:resize={resize} />

<div class="container" bind:this={canvas}>
    <SC.Canvas antialias alpha={true}>
        {#each geometries as geometry}
	        <SC.Mesh geometry={geometry} />
        {/each}
	    <SC.PerspectiveCamera fov={cameraFOV} position={[0, 0, cameraZ]} />
        <SC.OrbitControls enableZoom={false} />
    </SC.Canvas>
</div>
<style>
 .container {
     position: absolute;
     width: 100%;
     height: 100%;
     left: 0;
     top: 0;
 }
</style>
