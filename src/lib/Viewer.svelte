<script lang="ts">
 import * as THREE from 'three';
 import * as SC from 'svelte-cubed';

 export let geometries: THREE.Geometry[];

 const cameraFOV = 45;

 let canvas;
 let cameraZ = 3

 $: loadGeometries(geometries)

 function loadGeometries(gs: THREE.Geometry[]) {
     console.log(canvas)
     if (gs.length) fit(gs[0]);
 }

 function fit(g: THREE.BufferGeometry) {
     g.computeBoundingBox();

     const middle = new THREE.Vector3();
     g.boundingBox.getCenter(middle);
     g.applyMatrix4(new THREE.Matrix4().makeTranslation(
          -middle.x, -middle.y, -middle.z ) );


     const size = new THREE.Vector3();
     g.boundingBox.getSize(size);

     // https://wejn.org/2020/12/cracking-the-threejs-object-fitting-nut/
     const aspect = canvas ? (canvas.clientWidth / canvas.clientHeight) : 1;
     const fov = cameraFOV * ( Math.PI / 180 );
     const fovh = 2*Math.atan(Math.tan(fov/2) * aspect);
     let dx = size.z / 2 + Math.abs( size.x / 2 / Math.tan( fovh / 2 ) );
     let dy = size.z / 2 + Math.abs( size.y / 2 / Math.tan( fov / 2 ) );
     cameraZ = Math.max(dx, dy) * 1.2;
 }


</script>
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
