<script lang="ts">
 import Viewer from './lib/Viewer.svelte'
 import { fromCSG } from './lib/csg'
 import { exampleGeometry } from './lib/example'

 import workerUrl from '../target/dactyl_webworker.js?url'
 import modelingUrl from '../node_modules/@jscad/modeling/dist/jscad-modeling.min.js?url'
 import manuform from './assets/manuform.json'
 import model from './assets/model.stl?url'

 let scadUrl: string;
 let stlUrl: string = model;

 let geometries = [];

 let state: object = JSON.parse(JSON.stringify(manuform));
 let myWorker: Worker = null;

 let logs = [];

 let generating = false;

 exampleGeometry().then(g => {
     if (!geometries.length) geometries = [g]
 })

 $: process(state);

 function process(settings) {
     generating = true;
     logs = [];
     if (myWorker) myWorker.terminate();
     myWorker = new Worker(workerUrl);
     myWorker.postMessage({ type: "scripts", data: modelingUrl });
     myWorker.onmessage = (e) => {
         console.log('Message received from worker', e.data);
         if (e.data.type == 'scriptsinit') {
             myWorker.postMessage({ type: "mesh", data: settings})
         } else if (e.data.type == 'scad') {
             const blob = new Blob([e.data.data], { type: "application/openscad" })
             scadUrl = URL.createObjectURL(blob)
         } else if (e.data.type == 'stl') {
             const blob = new Blob([e.data.data], { type: "application/octed-stream" })
             generating = false;
             stlUrl = URL.createObjectURL(blob)
         } else if (e.data.type == 'csg') {
             generating = false;
             geometries = fromCSG(e.data.data);
             console.log('set geo');
         }
     }
 }
</script>

<header>
  <h1>Configurator</h1>
</header>
<main>
  <div class="config">
    <div class="card">
      <a class="button" href={scadUrl} download="model.scad">Download OpenSCAD</a>
      <a class="button" href={stlUrl} download="model.stl">Download STL</a>
    </div>

    {#each Object.entries(state) as [section, values]}
      <div class="card">
        <h2>{section}</h2>
        {#each Object.keys(values) as key}
          <label>
            {key}
            {#if typeof manuform[section][key] === "number"}
              <input type="number" bind:value={values[key]}/>
            {:else if typeof manuform[section][key] === "boolean"}
              <input type="checkbox" bind:checked={values[key]}/>
            {:else}
              <input bind:value={values[key]}/>
            {/if}
          </label>
        {/each}
      </div>
    {/each}
  </div>
  <div class="viewer">
    <div style="position: relative; flex-grow: 1; opacity: {generating ? 0.2 : 1}">
      <Viewer geometries={geometries}></Viewer>
    </div>
  </div>
</main>

<style>
  .logo {
    height: 6em;
    padding: 1.5em;
    will-change: filter;
    transition: filter 300ms;
  }
  .logo:hover {
    filter: drop-shadow(0 0 2em #646cffaa);
  }
  .logo.svelte:hover {
    filter: drop-shadow(0 0 2em #ff3e00aa);
  }
  .read-the-docs {
    color: #888;
  }
</style>
