<script lang="ts">
 import Dialog from './Dialog.svelte'
 import Heart from 'svelte-material-icons/HeartOutline.svelte'

 let noGH = false
 let suggested: string|null = null

 function iUse(suggestion: string) {
   fetch('https://pageviews.ryanis.cool/by/suggest-' + suggestion.replace(' ', '-'))
   suggested = suggestion
   noGH = false
 }
</script>
<Dialog on:close>
  <span slot="title">Support My Work</span>
  <div slot="content">
    <p class="mb-1 ">Thanks for your interest! Programming takes lots of time, and I'm looking for ways to keep this project sustainable.</p>
    <p class="mb-1">What's next for this project? I've been building a <a class="underline text-teal-500" target="_blank" href="https://github.com/rianadon/dactyl-configurator/issues/4">second version of this tool</a> over the past month. It's even more capable, with trackball support, custom key layouts, rounded edges, form-fitting wrist rests, and more.</p>
    <p class="mb-1">It would be a dream to work on keyboard design & ergonomics part time, continuing to build new generators and seeing what the community makes with them. Your support will get me one step closer to this goal.</p>
    <p class="text-center">
      <a href="https://github.com/sponsors/rianadon" target="_blank" class="mt-6 mb-3 inline-flex items-center gap-2 bg-yellow-400/10 border-2 border-yellow-400 px-3 py-1.5 rounded hover:bg-yellow-400/60 hover:shadow-md hover:shadow-yellow-400/30 transition-shadow">
        <Heart size="24" class="text-yellow-500 dark:text-yellow-300" />Sponsor Me on GitHub
      </a>
      <br />
      <button class="underline text-gray-500 dark:text-gray-200 hover:text-gray-700 hover:dark:text-gray-300" on:click={() => noGH = true}>I don't have a GitHub account.</button>
      {#if noGH}
        <div class="mt-4">
          <p>I'm using only GitHub Sponsors for now.</p>
          <p>That said, I'm looking into additionally using other platforms.</p>
          <h3 class="font-bold my-2">Would you prefer one of these?</h3>
          <button class="preset" on:click={() => iUse('Open Collective')}>Open Collective</button>
          <button class="preset" on:click={() => iUse('Patreon')}>Patreon</button>
          <button class="preset" on:click={() => iUse('Ko-fi')}>Ko-fi</button>
          <button class="preset" on:click={() => iUse('crypto')}>Cryptocurrencies</button>
        </div>
        <img class="absolute w-0 h-0" src="https://pageviews.ryanis.cool/by/nogh" />
      {:else if suggested}
        <p class="mt-2 italic">Thanks! I've recorded your suggestion of a {suggested} account.</p>
      {/if}
    </p>
    <img class="absolute w-0 h-0" src="https://pageviews.ryanis.cool/by/sponsor" />
  </div>
</Dialog>
<style lang="postcss">
 .preset {
   @apply bg-[#99F0DC] dark:bg-gray-900 hover:bg-teal-500 dark:hover:bg-teal-700 dark:text-white py-1 px-4 rounded focus:outline-none border border-transparent focus:border-teal-500 mb-2;
 }
</style>
