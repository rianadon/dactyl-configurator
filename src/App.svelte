<script lang="ts">
 import Viewer from './lib/Viewer.svelte'
 import Info from 'svelte-material-icons/Information.svelte'
 import Shimmer from 'svelte-material-icons/Shimmer.svelte'
 import Popover from 'svelte-easy-popover'
 import { SUPPORTS_DENSITY, estimateFilament, fromMesh } from './lib/mesh'
 import { exampleGeometry } from './lib/example'
 import DactylWorker from './worker?worker'

 import manuform from './assets/manuform.json'
 import original from './assets/original.json'
 import type { Schema } from './lib/schema'
 import { ManuformSchema, OriginalSchema } from './lib/schema'
 import Field from './lib/Field.svelte';
 import RenderDialog from './lib/RenderDialog.svelte';
 import SupportDialog from './lib/SupportDialog.svelte';
 import Footer from './lib/Footer.svelte';
 import ShapingSection from './lib/ShapingSection.svelte';
 import Instructions from './lib/Instructions.svelte';
 import FilamentChart from './lib/FilamentChart.svelte';
 import { serialize, deserialize } from './lib/serialize';

 import presetOrigOrig from './assets/presets/original.original.json'
 import presetLight from './assets/presets/original.lightcycle.json'
 import presetDefault from './assets/presets/manuform.default.json'
 import presetOriginal from './assets/presets/manuform.tshort.json'
 import presetCorne from './assets/presets/manuform.corne.json'
 import presetSmallest from './assets/presets/manuform.smallest.json'

 let keyboardGeometry = null;
 let keyboardVolume = 0;
 let supportGeometry = null;
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
 let instructionsOpen = !(localStorage['instructions'] === 'false')
 let showSupports = false

 $: localStorage['instructions'] = JSON.stringify(instructionsOpen)

 exampleGeometry().then(g => {
     // Load the example gemoetry if nothing has been rendered yet
     if (!keyboardGeometry) keyboardGeometry = g
 })

 $: try {
     window.location.hash = serialize(state);
 } catch (e) {
     console.error(e);
 }
 $: process(state);

 let schema: Schema
 $: schema = (state.keyboard == "manuform" ? ManuformSchema : OriginalSchema);
 $: defaults = (state.keyboard == "manuform" ? manuform : original);

 function loadPreset(preset: any) {
     const defaults = preset.keyboard == "manuform" ? manuform : original;
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
     filament = undefined;
     supportGeometry = null;
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
             keyboardGeometry = fromMesh(e.data.data);
             keyboardVolume = e.data.data.volume
         } else if (e.data.type == 'supports') {
             supportGeometry = fromMesh(e.data.data);
             filament = estimateFilament(keyboardVolume, e.data.data.volume);
         } else if (e.data.type == 'log') {
             // Logging from OpenSACD
             logs = [...logs, e.data.data]
         }
     }
 }
</script>

<header class="px-8 pb-4 pt-12 sm:flex items-center mb-4">
  <h1 class="dark:text-slate-100 text-4xl font-semibold flex-1">Dactyl Keyboard Configurator</h1>
  <button class="flex items-center gap-2 bg-yellow-400/10 border-2 border-yellow-400 px-3 py-1.5 rounded hover:bg-yellow-400/60 hover:shadow-md hover:shadow-yellow-400/30 transition-shadow mt-6 sm:mt-0 sm:ml-2" on:click={() => sponsorOpen = true}>
    <Shimmer size="24" class="text-yellow-500 dark:text-yellow-300" />Support My Work
  </button>
</header>
{#if instructionsOpen}
  <Instructions on:close={() => instructionsOpen = false} />
{/if}
<main class="mt-6 mb-16 px-8 dark:text-slate-100 flex flex-col-reverse xs:flex-row">
  <div class="xs:w-80 md:w-auto">
    <div class="mb-8">
      <button class="help-button" on:click={() => instructionsOpen = !instructionsOpen}>{#if instructionsOpen}Hide Instructions{:else}Show Instructions{/if}</button>
      <button class="button" on:click={downloadSTL}>Download Model</button>
    </div>

    <h2 class="text-2xl text-teal-500 dark:text-teal-300 font-semibold mb-2">Presets</h2>
    <div class="lg:flex justify-between items-baseline w-64 md:w-auto">
      <div class="mb-2 mr-4">Manuform</div>
      <div>
        <button class="preset" on:click={() => loadPreset(presetCorne)}>Corne</button>
        <button class="preset" on:click={() => loadPreset(presetSmallest)}>Smallest</button>
        <button class="preset" on:click={() => loadPreset(presetDefault)}>Default</button>
        <button class="preset" on:click={() => loadPreset(presetOriginal)}>Original</button>
      </div>
    </div>
    <div class="lg:flex justify-between items-baseline">
      <div class="mb-2 mr-4">Original Dactyl</div>
      <div>
        <button class="preset" on:click={() => loadPreset(presetOrigOrig)}>Original</button>
        <button class="preset" on:click={() => loadPreset(presetLight)}>Lightcycle</button>
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
    {#if state.keyboard == "original"}
      <div class="border-2 border-yellow-400 py-2 px-4 m-2 rounded bg-white dark:bg-gray-900">
        Generating the Original Dactyl case takes an extremeley long time, so it is disabled by default. Turn on <span class="whitespace-nowrap bg-gray-200 dark:bg-gray-800 px-2 rounded">Include Case</span> to generate it.
      </div>
    {/if}
    <div class="viewer relative xs:sticky h-[100vh] top-0">
      <Viewer geometries={[keyboardGeometry, supportGeometry]} showSupports={showSupports} style="opacity: {generatingCSG ? 0.2 : 1}"></Viewer>
      {#if filament}
        <div class="absolute bottom-0 right-0 mb-2">
          {filament.length.toFixed(1)}m <span class="text-gray-600 dark:text-gray-100">of filament</span>
          <button class="align-[-18%] inline-block text-gray-600 dark:text-gray-100" bind:this={referenceElement}>
            <Info size="20px" />
          </button>
          <Popover triggerEvents={["hover", "focus"]} {referenceElement} placement="top" spaceAway={4} on:change={({ detail: { isOpen }}) => showSupports = isOpen}>
            <div class="flex gap-4 items-end rounded bg-gray-100 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 px-2 py-1 mx-4 text-gray-600 dark:text-gray-100">
              <FilamentChart fractionKeyboard={filament.fractionKeyboard} />
              <div>
                <p class="whitespace-nowrap mb-2">Estimated using <span class="font-semibold text-teal-500 dark:text-teal-400">100% infill</span>,<br><span class="font-semibold text-purple-500 dark:text-purple-400">{SUPPORTS_DENSITY*100}% supports density</span>.</p>
                <p class="whitespace-nowrap mb-1">This will cost about <span class="font-semibold text-black dark:text-white">${filament.cost.toFixed(2)}</span>.</p>
                <p class="whitespace-nowrap text-sm">The keyboard itself uses {filament.keyboard.length.toFixed(1)}m.</p>
              </div>
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
<p class="cosmos mx-2 mb-8 px-2 py-2 relative mb-2 from-purple-100 to-teal-100 dark:from-purple-900 dark:to-teal-800 rounded flex items-center justify-between">
  <span class="bg-[#eaecfc]/80 dark:bg-[#353c70]/50 px-4 py-2 rounded mix-blend-luminosity"><span class="font-semibold hidden sm:block md:inline">Enjoying the configurator?</span> I'm building Cosmos, a new configurable keyboard.</span>
  <a class="rounded bg-gradient-to-br from-[#3c0d61] to-gray-700 text-white dark:text-black dark:from-purple-200 dark:to-purple-100 py-2 px-4 mx-4 font-semibold flex-none hover:from-purple-700 hover:to-teal-800 hover:dark:from-white hover:dark:to-white hover:shadow-md hover:shadow-teal-900/30 hover:dark:shadow-teal-200/30 transition-shadow" href="https://ryanis.cool/cosmos?utm_source=dactyl">Check it out</a>
</p>
<footer class="px-8 pb-8">
  <Footer></Footer>
</footer>
{#if sponsorOpen}
  <SupportDialog on:close={() => sponsorOpen =  false} />
{/if}
{#if generatingSCAD}
  <RenderDialog>
    This may take a few seconds.
  </RenderDialog>
{:else if stlDialogOpen}
  <RenderDialog logs={logs} closeable={!generatingSCADSTL} on:close={() => stlDialogOpen= false} generating={generatingSTL}>
    <p class="mb-1">An STL file for 3D printing should download in a few seconds.</p>
    <p class="mb-1">Want to edit the model in OpenSCAD? Download the source file <button class="underline text-teal-500" on:click={downloadSCAD}>here</button>.</p>

    <div class="bg-gray-100 dark:bg-gray-900 px-4 py-2 my-4 rounded text-left">
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
     @apply bg-purple-300 dark:bg-gray-900 hover:bg-purple-400 dark:hover:bg-teal-700 dark:text-white font-semibold px-4 h-[42px] rounded focus:outline-none border border-transparent focus:border-teal-500 mb-2;
 }
 .help-button {
     @apply border-2 border-purple-300 dark:border-gray-700 hover:bg-purple-100 dark:hover:bg-gray-800 dark:hover:border-teal-500 dark:text-white h-[42px] px-4 rounded focus:outline-none focus:border-teal-500;
 }

 .preset {
     @apply bg-[#99F0DC] dark:bg-gray-900 hover:bg-teal-500 dark:hover:bg-teal-700 dark:text-white py-1 px-4 rounded focus:outline-none border border-transparent focus:border-teal-500 mb-2;
 }

 .cosmos {
     background-image: url('/stars.png'), linear-gradient(to right, var(--tw-gradient-stops));
     background-repeat: repeat, no-repeat;
     background-size: auto 100%, auto auto;
     background-position: center, 0% 0%;
 }
 @media (prefers-color-scheme: dark) {
     .cosmos {
         background-image: url('/starsdark.png'), linear-gradient(to right, var(--tw-gradient-stops));
     }
 }
</style>
