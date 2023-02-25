<script lang="ts">
 import svelteLogo from './assets/svelte.svg'
 import Counter from './lib/Counter.svelte'
 import Viewer from './lib/Viewer.svelte'

 import workerUrl from '../target/dactyl_webworker.js?url'
 import wasmJSUrl from './assets/openscad.wasm.js?url'
 import wasmUrl from './assets/openscad.wasm?url'
 import manuform from './assets/manuform.json'
 import model from './assets/model.stl?url'

 let scadUrl: string;
 let stlUrl: string = model;

 let state: object = JSON.parse(JSON.stringify(manuform));
 let myWorker: Worker = null;

 $: process(state);

 function process(settings) {
     if (myWorker) myWorker.terminate();
     myWorker = new Worker(workerUrl);
     myWorker.postMessage({ type: "scripts", data: [wasmJSUrl, wasmUrl] });
     myWorker.onmessage = (e) => {
         console.log('Message received from worker', e.data);
         if (e.data.type == 'init') {
             myWorker.postMessage({ type: "mesh", data: state})
         } else if (e.data.type == 'scad') {
             const blob = new Blob([e.data.data], { type: "application/openscad" })
             scadUrl = URL.createObjectURL(blob)
         } else if (e.data.type == 'stl') {
             const blob = new Blob([e.data.data], { type: "application/octed-stream" })
             stlUrl = URL.createObjectURL(blob)
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
            {:else}
              <input bind:value={values[key]}/>
            {/if}
          </label>
        {/each}
      </div>
    {/each}
  </div>
  <div class="viewer">
    <Viewer model={stlUrl}></Viewer>
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
