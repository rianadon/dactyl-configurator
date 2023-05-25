<script lang="ts">
 import Viewer from './lib/Viewer.svelte'
 import Github from 'svelte-material-icons/Github.svelte'
 import Book from 'svelte-material-icons/BookOpenVariant.svelte'
 import Info from 'svelte-material-icons/Information.svelte'
 import Shimmer from 'svelte-material-icons/Shimmer.svelte'
 import Popover from 'svelte-easy-popover'
 import { estimateFilament, fromCSG } from './lib/csg'
 import { exampleGeometry } from './lib/example'
 import DactylWorker from './worker?worker'

 import manuform from './assets/manuform.json'
 import lightcycle from './assets/lightcycle.json'
 import type { Schema } from './lib/schema'
 import { ManuformSchema, LightcycleSchema } from './lib/schema'
 import Field from './lib/Field.svelte';
 import RenderDialog from './lib/RenderDialog.svelte';
 import SupportDialog from './lib/SupportDialog.svelte';
 import Footer from './lib/Footer.svelte';
 import ShapingSection from './lib/ShapingSection.svelte';
 import Instructions from './lib/Instructions.svelte';
 import { serialize, deserialize } from './lib/serialize';

 import presetLight from './assets/presets/lightcycle.default.json'
 import presetErgodox from './assets/presets/manuform.ergodox.json'
 import presetOriginal from './assets/presets/manuform.tshort.json'
 import presetCorne from './assets/presets/manuform.corne.json'
 import presetSmallest from './assets/presets/manuform.smallest.json'

 let geometries = [];
 let filament;
 let referenceElement;

 let state: typeof manuform = JSON.parse(JSON.stringify(deserialize(location.hash.substring(1), manuform)));
 let myWorker: Worker = null;

 let logs = [];

 let csgError: Error | undefined;
 let generatingCSG = false;
 let generatingSCAD = false;
 let generatingSTL = false;
 let generatingSCADSTL = false;
 let stlDialogOpen = false;
 let sponsorOpen = false;
 let instructionsOpen = true;

 exampleGeometry().then(g => {
     // Load the example gemoetry if nothing has been rendered yet
     if (!geometries.length) geometries = [g]
 })

 $: try {
     window.location.hash = serialize(state);
 } catch (e) {
     console.error(e);
 }
 $: process(state);

 let schema: Schema
 $: schema = (state.keyboard == "manuform" ? ManuformSchema : LightcycleSchema);
 $: defaults = (state.keyboard == "manuform" ? manuform : lightcycle);

 function loadPreset(preset: any) {
     const defaults = preset.keyboard == "manuform" ? manuform : lightcycle;
     state = JSON.parse(JSON.stringify({
         keyboard: preset.keyboard,
         options: { ...defaults.options, ...preset.options }
     }))
 }

 /** Downloads a blob using a given filename */
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
     stlDialogOpen = true;
     myWorker.postMessage({type: "stl", data: state });
 }

 function downloadSCADSTL() {
     generatingSCADSTL = true;
     logs = ['Loading OpenSCAD...']
     myWorker.postMessage({type: "scadstl", data: state });
 }

 function process(settings: typeof manuform) {
     // Reset the state
     generatingCSG = true;
     csgError = undefined;
     logs = [];
     if (myWorker) myWorker.terminate();
     myWorker = new DactylWorker()
     myWorker.onmessage = (e) => {
         console.log('Message received from worker', e.data);
         if (e.data.type == 'scriptsinit') {
             // Now that the worker has finished loading Manifold, generate the preview.
             myWorker.postMessage({ type: "csg", data: settings})
         } else if (e.data.type == 'csgerror') {
             // There was an error generating the preview. Show it!
             csgError = e.data.data;
         } else if (e.data.type == 'scad') {
             // SCAD generation finished. Download it!
             generatingSCAD = false;
             const blob = new Blob([e.data.data], { type: "application/x-openscad" })
             download(blob, "model.scad")
         } else if (e.data.type == 'stl') {
             // STL generation finished. Download it!
             generatingSTL = false;
             generatingSCADSTL = false;
             const blob = new Blob([e.data.data], { type: "application/octet-stream" })
             download(blob, "model.stl")
         } else if (e.data.type == 'csg') {
             // Preview finished. Show it!
             generatingCSG = false;
             geometries = fromCSG(e.data.data);
             filament = estimateFilament(e.data.data[0]?.volume);
         } else if (e.data.type == 'log') {
             // Logging from OpenSACD
             logs = [...logs, e.data.data]
         }
     }
 }
</script>

<header class="px-8 pb-4 pt-12 md:flex items-center mb-4">
  <h1 class="dark:text-slate-100 text-4xl font-semibold flex-1">Dactyl Keyboard Configurator</h1>
  <div class="flex gap-4 mt-6 md:mt-0 md:ml-2">
    {#if !instructionsOpen}<button class="border-2 border-gray-400/40 hover:bg-gray-400/20 px-3 py-1.5 rounded" on:click={() => instructionsOpen = true}>Instructions</button>{/if}
    <button class="flex items-center gap-2 bg-yellow-400/10 border-2 border-yellow-400 px-3 py-1.5 rounded hover:bg-yellow-400/60 hover:shadow-md hover:shadow-yellow-400/30 transition-shadow" on:click={() => sponsorOpen = true}>
      <Shimmer size="24" class="text-yellow-500 dark:text-yellow-300" />Support My Work
    </button>
  </div>
  <!--<a class="text-gray-800 dark:text-gray-100 mx-2 md:mx-4" href="/docs">
    <Book size="2em" />
  </a>
  <a class="text-gray-800 dark:text-gray-100 mx-2 md:mx-4" href="/">
    <Github size="2em" />
  </a>-->
</header>
{#if instructionsOpen}
  <Instructions on:close={() => instructionsOpen = false} />
{/if}
<main class="mt-4 px-8 dark:text-slate-100 flex flex-col-reverse xs:flex-row">
  <div class="xs:w-80 md:w-auto">
    <div class="mb-8">
      <button class="button" on:click={downloadSCAD}>Download OpenSCAD</button>
      <button class="button" on:click={downloadSTL}>Download STL</button>
    </div>

    <h2 class="text-2xl text-teal-500 dark:text-teal-300 font-semibold mb-2">Presets</h2>
    <div class="lg:flex justify-between items-baseline w-64 md:w-auto">
      <div class="mb-2 mr-4">Manuform</div>
      <div>
        <button class="preset" on:click={() => loadPreset(presetCorne)}>Corne</button>
        <button class="preset" on:click={() => loadPreset(presetSmallest)}>Smallest</button>
        <button class="preset" on:click={() => loadPreset(presetErgodox)}>Ergodox</button>
        <button class="preset" on:click={() => loadPreset(presetOriginal)}>Original</button>
      </div>
    </div>
    <div class="lg:flex justify-between items-baseline">
      <div class="mb-2 mr-4">Lightcycle</div>
      <div>
        <button class="preset" on:click={() => loadPreset(presetLight)}>Basic</button>
      </div>
    </div>

    {#each schema as section}
      <div class="mt-8">
        <h2 class="text-2xl text-teal-500 dark:text-teal-300 font-semibold mb-2 capitalize">{section.name}</h2>
        {#if section.var == "shaping"}
          <ShapingSection state={state} schema={section} bind:states={state.options[section.var]} />
        {:else}
          {#each section.fields as key}
            <Field defl={defaults.options[section.var][key.var]} schema={key} bind:value={state.options[section.var][key.var]} />
          {/each}
        {/if}
      </div>
    {/each}
  </div>
  <div class="flex-1">
    {#if state.keyboard == "lightcycle"}
      <div class="border-2 border-yellow-400 py-2 px-4 m-2 rounded bg-white dark:bg-gray-900">
        Generating the Lightcycle case takes an extremeley long time, so it is disabled by default. Turn on <span class="whitespace-nowrap bg-gray-200 dark:bg-gray-800 px-2 rounded">Include Case</span> to generate it.
      </div>
    {/if}
    <div class="viewer relative xs:sticky h-[100vh] top-0">
      <Viewer geometries={geometries} style="opacity: {generatingCSG ? 0.2 : 1}"></Viewer>
      {#if filament}
        <div class="absolute bottom-0 right-0 text-right mb-2">
          {filament.length.toFixed(1)}m <span class="text-gray-600 dark:text-gray-100">of filament</span>
          <div class="align-[-18%] inline-block text-gray-600 dark:text-gray-100" bind:this={referenceElement}>
            <Info size="20px" />
          </div>
          <Popover triggerEvents={["hover", "focus"]} {referenceElement} placement="top" spaceAway={4}>
            <div class="rounded bg-gray-100 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 px-2 py-1 mx-4 w-80">
              <p>Estimate using 100% infill, no supports.</p>
              <p>This will set you back at least ${filament.cost.toFixed(2)}.</p>
            </div>
          </Popover>
        </div>
      {/if}
      {#if csgError}
        <div class="absolute text-white m-4 left-0 right-0 rounded p-4 top-[10%] bg-red-700">
          <p>There are some rough edges in this tool, and you've found one of them.</p>
          <p class="mb-2">The set of options you've chosen cannot be previewed.</p>
          <p class="mb-2">Even though there is no preview, you can still download the STL or the OpenSCAD model.</p>
          <p class="mb-2">Here's some technical information:</p>
          <p class="text-sm whitespace-pre-line"><code>{csgError.name}: {csgError.message}<br>{csgError.stack.split('\n').slice(0, 8).join('\n')}</code></p>
        </div>
      {/if}
    </div>
  </div>
</main>
<footer class="px-8 pb-8 pt-16">
  <Footer></Footer>
</footer>
{#if sponsorOpen}
  <SupportDialog on:close={() => sponsorOpen =  false} />
{/if}
{#if generatingSCAD}
  <RenderDialog>
    This may take a few seconds.
  </RenderDialog>
{/if}
{#if stlDialogOpen}
  <RenderDialog logs={logs} closeable={!generatingSCADSTL} on:close={() => stlDialogOpen= false} generating={generatingSTL}>
    <p>Your model should download in a few seconds.</p>

    <div class="bg-gray-100 dark:bg-gray-900 px-4 py-2 my-4 mx-2 rounded text-left">
      <p class="font-bold mb-1">A few seconds? Why so fast?</p>
      <p>Model generation uses <a class="underline text-teal-500" href="github.com/elalish/Manifold">Manifold</a>, an extremely fast geometry kernel.</p>
      <p>Recent versions of OpenSCAD use this kernel too for faster renders.</p>
    </div>

    <p class="mb-1">If you think the STL is wrong, you can regenerate it with OpenSCAD.</p>
    <p class="mb-1">Click <button class="underline text-teal-500" on:click={downloadSCADSTL}>here</button> to generate an STL using OpenSCAD through the browser.</p>
    <p class="mb-1 text-gray-500 dark:text-gray-400">→ It's not worth it. This gives the same results but takes minutes. ←</p>
    {#if logs.length}
      <hr class="my-2 text-gray-500 dark:border-gray-400">
      <p>Your model is being generated with the OpenSCAD renderer.</p>
      <p>This takes a few minutes. I suggest you stop staring at this dialog box.</p>
    {/if}
  </RenderDialog>
{/if}

<style lang="postcss">
 :global(html) {
     @apply dark:bg-gray-800 dark:text-white;
 }

 @media (min-height: 480px) {
     .viewer { height: calc(100vh - 136px); top: 68px }
 }

 @media (max-width: theme('screens.xs')) {
     .viewer { @apply max-h-[50vh] mb-4 top-0; }
 }

 .button {
     @apply bg-purple-300 dark:bg-gray-900 hover:bg-purple-400 dark:hover:bg-teal-700 dark:text-white font-bold py-2 px-4 rounded focus:outline-none border border-transparent focus:border-teal-500 mb-2;
 }
 .help-button {
     @apply border-2 border-gray-200 dark:bg-gray-900 hover:bg-gray-300 dark:hover:bg-gray-700 dark:text-white py-2 px-4 rounded focus:outline-none focus:border-teal-500;
 }

 .preset {
     @apply bg-[#99F0DC] dark:bg-gray-900 hover:bg-teal-500 dark:hover:bg-teal-700 dark:text-white py-1 px-4 rounded focus:outline-none border border-transparent focus:border-teal-500 mb-2;
 }
</style>
