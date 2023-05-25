<script lang="ts">
 import Close from 'svelte-material-icons/Close.svelte'
 import { createEventDispatcher } from 'svelte';

 const dispatch = createEventDispatcher()
 const close = () => dispatch('close')
</script>

<article class="mx-4 mb-4 pl-4 pr-12 py-2 rounded dark:bg-gray-900/50 relative">
  <p>The Dactyl keyboard is a configurable ergonomic keyboard. You can not only change the switch types you are using, but the key layout and shape of the keyboard as well, so that it fits to your typing style and needs.</p>
  <p>I recommend you begin with the <span>Original</span> preset. It has better defaults for the key holes and connector.</p>
  <p>Before 3D printing your first Dactyl Keyboard case, I recommend you:</p>
  <ul class="list-disc pl-4">
    <li>Copy down the current site URL. It's unique to the keyboard you've configured.</li>
    <li>Take a look at a few build logs online and decide whether you want to use PCBs or solder the wires directly to the switches.</li>
    <li>Find where to buy the parts you'll need to create the keyboard (switches, keycaps, diodes, wires, microcontrollers, cables, and soldering equipment).</li>
  </ul>
  <p>There's a lot to making your own keyboard, but it's a fun process. I promise!</p>
  <button class="absolute right-2 top-2 border rounded-full w-10 h-10 flex items-center justify-center" on:click={close}>
    <Close size="24" class="text-gray-800 dark:text-gray-100" />
  </button>
</article>
<style lang="postcss">
 p { @apply mb-1; }
 span { @apply rounded px-1 py-0.5 dark:bg-gray-900 border border-gray-700; }
</style>
