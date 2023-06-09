<script lang="ts">
 import * as THREE from 'three';
 import * as SC from 'svelte-cubed';
 import PerspectiveCamera from './PerspectiveCamera.svelte';
 import WebGL from 'three/examples/jsm/capabilities/WebGL'

 export let geometries: THREE.Geometry[];
 export let showSupports: boolean;
 export let style: string;

 let canvas;
 let camera: THREE.PerspectiveCamera;
 let root: any;

 const middle = new THREE.Vector3()

 const cameraFOV = 45;
 const size = new THREE.Vector3(1, 1, 1);

 $: validGeometries = geometries.filter(g => !!g)
 $: loadGeometries(validGeometries)

 $: {
     // Give the camera an initial position
     if (camera && camera.position.length() == 0) {
         camera.position.set(0, 0.8, 1);
         resize();
     }
 }

 function loadGeometries(gs: THREE.BufferGeometry[]) {
     // Compute bounding boxes of the gemoetries
     const boundingBox = new THREE.Box3(new THREE.Vector3(-0.1, -0.1, -0.1), new THREE.Vector3(0.1, 0.1, 0.1));
     if (gs.length == 1) {
         gs[0].computeBoundingBox();
         boundingBox.union(gs[0].boundingBox);

         boundingBox.getCenter(middle);
         boundingBox.getSize(size);
     }

     for (const g of gs) {
         if (g.wastransformed) continue
         g.applyMatrix4(new THREE.Matrix4().makeTranslation(-middle.x, -middle.y, -middle.z));
         g.wastransformed = true
     }

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
         camera.position.normalize();
         camera.position.multiplyScalar(Math.max(dx, dy) * 1.2);
         camera.updateProjectionMatrix();
         root.invalidate();
     }
 }
</script>

<svelte:window on:resize={resize} />

{#if WebGL.isWebGLAvailable()}
  <div class="container" bind:this={canvas} style={style}>
    <SC.Canvas antialias alpha={true}>
      <SC.Group rotation={[-Math.PI/2, 0, 0]}>
        {#if validGeometries[0]}
	      <SC.Mesh geometry={validGeometries[0]} />
        {/if}
        {#if validGeometries[1] && showSupports}
          <SC.Mesh geometry={validGeometries[1]} material={new THREE.MeshStandardMaterial({ color: 0xcc80f2, transparent: true, opacity: 0.85 })} />
        {/if}
      </SC.Group>
      <PerspectiveCamera fov={cameraFOV} bind:self={camera} bind:root={root} />
      <SC.OrbitControls enableZoom={false} />
    </SC.Canvas>
  </div>
{:else}
  <div class="border border-2 border-red-400 py-2 px-4 m-2 rounded bg-white dark:bg-gray-900">
    <p>The preview could not be loaded. This is because:</p>
    <p>{@html WebGL.getWebGLErrorMessage().innerHTML.replace('<a', '<a class="underline"')}.</p>
  </div>
{/if}
<style>
 .container {
     position: absolute;
     width: 100%;
     height: 100%;
     left: 0;
     top: 0;
 }
</style>
