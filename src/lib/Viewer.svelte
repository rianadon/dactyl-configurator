<script lang="ts">
 import * as THREE from 'three';
 import * as SC from 'svelte-cubed';
 import PerspectiveCamera from './PerspectiveCamera.svelte';

 export let geometries: THREE.Geometry[];
 export let style: string;

 let canvas;
 let camera: THREE.PerspectiveCamera;
 let root: any;


 const cameraFOV = 45;
 const size = new THREE.Vector3();

 $: loadGeometries(geometries)

 $: {
     // Give the camera an initial position
     if (camera && camera.position.length() == 0) {
         camera.position.set(0, -1, 0.8);
         resize();
     }
 }

 function loadGeometries(gs: THREE.BufferGeometry[]) {
     // Compute bounding boxes of the gemoetries
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
     let aspect = canvas ? (canvas.clientWidth / canvas.clientHeight) : 1;
     if (aspect == 0 || aspect == Infinity) aspect = 1;
     const fov = cameraFOV * ( Math.PI / 180 );
     const fovh = 2*Math.atan(Math.tan(fov/2) * aspect);
     let dx = size.z / 2 + Math.abs( size.x / 2 / Math.tan( fovh / 2 ) );
     let dy = size.z / 2 + Math.abs( size.y / 2 / Math.tan( fov / 2 ) );
     if (camera) {
         console.log(aspect, dx, dy);
         camera.position.normalize();
         console.log(camera.position);
         camera.position.multiplyScalar(Math.max(dx, dy) * 1.2);
         camera.updateProjectionMatrix();
         root.invalidate();
     }
 }
</script>

<svelte:window on:resize={resize} />

<div class="container" bind:this={canvas} style={style}>
    <SC.Canvas antialias alpha={true}>
        {#each geometries as geometry}
	        <SC.Mesh geometry={geometry} />
        {/each}
	    <PerspectiveCamera fov={cameraFOV} bind:self={camera} bind:root={root} />
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
