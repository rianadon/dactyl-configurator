<script lang="ts">
 import Viewer from './lib/Viewer.svelte'
 import { fromCSG } from './lib/csg'
 import { exampleGeometry } from './lib/example'

 import workerUrl from '../target/dactyl_webworker.js?url'
 import modelingUrl from '../node_modules/@jscad/modeling/dist/jscad-modeling.min.js?url'
 import scadJSUrl from './assets/openscad.wasm.js?url'
 import scadWasmUrl from './assets/openscad.wasm?url'

 import manuform from './assets/manuform.json'
 import { ManuformSchema } from './schema/manuform.schema'
 import model from './assets/model.stl?url'
 import Field from './lib/Field.svelte';
 import RenderDialog from './lib/RenderDialog.svelte';
 import Footer from './lib/Footer.svelte';

 let scadUrl: string;
 let stlUrl: string = model;

 let geometries = [];

 let state: typeof manuform = JSON.parse(JSON.stringify(manuform));
 let myWorker: Worker = null;

 let logs = [];

 let csgError: Error | undefined;
 let generatingCSG = false;
 let generatingSCAD = false;
 let generatingSTL = false;

 exampleGeometry().then(g => {
     if (!geometries.length) geometries = [g]
 })

 $: process(state);

 function download(blob: Blob, filename: string) {
     const a = document.createElement('a')
     a.href = URL.createObjectURL(blob)
     a.download = filename
     document.body.appendChild(a)
     a.click()
     document.body.removeChild(a)
 }

 function downloadSCAD() {
     generatingSCAD = true;
     myWorker.postMessage({type: "scad", data: state });
 }

 function downloadSTL() {
     generatingSTL = true;
     logs = ['Loading OpenSCAD...']
     myWorker.postMessage({type: "wasm", data: [scadJSUrl, scadWasmUrl]})
 }

 function process(settings: typeof manuform) {
     generatingCSG = true;
     csgError = undefined;
     logs = [];
     if (myWorker) myWorker.terminate();
     myWorker = new Worker(workerUrl);
     myWorker.postMessage({ type: "scripts", data: modelingUrl });
     myWorker.onmessage = (e) => {
         console.log('Message received from worker', e.data);
         if (e.data.type == 'scriptsinit') {
             myWorker.postMessage({ type: "csg", data: settings})
         } else if (e.data.type == 'csgerror') {
             console.error(e.data.data);
             csgError = e.data.data;
         } else if (e.data.type == 'wasminit') {
             myWorker.postMessage({type: "stl", data: settings });
         } else if (e.data.type == 'scad') {
             generatingSCAD = false;
             const blob = new Blob([e.data.data], { type: "application/x-openscad" })
             download(blob, "model.scad")
         } else if (e.data.type == 'stl') {
             generatingSTL = false;
             const blob = new Blob([e.data.data], { type: "application/octet-stream" })
             download(blob, "model.stl")
         } else if (e.data.type == 'csg') {
             generatingCSG = false;
             geometries = fromCSG(e.data.data);
         } else if (e.data.type == 'log') {
             if (e.data.data == 'Could not initialize localization.') {
                e.data.data = 'Starting render...'
             }
             logs = [...logs, e.data.data]
         }
     }
 }
</script>

<header class="px-8 pb-8 pt-12">
  <h1 class="dark:text-slate-100 text-4xl font-semibold mb-4">Dactyl Configurator</h1>
</header>
<main class="px-8 dark:text-slate-100 flex">
  <div>
    <div class="mb-8">
      <button class="button" on:click={downloadSCAD}>Download OpenSCAD</button>
      <button class="button" on:click={downloadSTL}>Download STL</button>
    </div>

    {#each Object.keys(ManuformSchema) as section}
      <div class="mt-8">
        <h2 class="text-2xl text-teal-300 font-semibold mb-2 capitalize">{section}</h2>
        {#each Object.keys(ManuformSchema[section]) as key}
          <Field defl={manuform[section][key]} schema={ManuformSchema[section][key]} bind:value={state[section][key]} />
        {/each}
      </div>
    {/each}
  </div>
  <div class="flex-1">
    <div class="viewer sticky top-[68px]">
      <Viewer geometries={geometries} style="opacity: {generatingCSG ? 0.2 : 1}"></Viewer>
      {#if csgError}
        <div class="absolute text-white m-4 left-0 right-0 rounded p-4 top-[30%] bg-red-700">
          <p>There are some rough edges in this tool, and you've found one of them.</p>
          <p class="mb-2">The set of options you've chosen cannot be previewed.</p>
          <p class="mb-2">Here's some technical information:</p>
          <p class="text-sm"><code>{csgError}<br>{csgError.stack.split('\n').slice(0, 5).join('\n')}</code></p>
        </div>
      {/if}
    </div>
  </div>
</main>
<footer class="px-8 pb-8 pt-16">
  <Footer></Footer>
</footer>
{#if generatingSCAD}
  <RenderDialog>
    This may take a few seconds.
  </RenderDialog>
{/if}
{#if generatingSTL}
  <RenderDialog logs={logs}>
    This may take a few minutes.
  </RenderDialog>
{/if}

<style lang="postcss">
 :global(html) {
     @apply dark:bg-gray-800 dark:text-white;
 }

 .viewer { height: calc(100vh - 136px) }

 .button {
  @apply bg-gray-900 hover:bg-teal-700 text-white font-bold py-2 px-4 rounded focus:outline-none border border-transparent focus:border-teal-500;
 }
</style>
