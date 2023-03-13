<script>
 import Code from 'svelte-material-icons/CodeTags.svelte'
 import Keyboard from 'svelte-material-icons/KeyboardOutline.svelte'
 import Love from 'svelte-material-icons/HeartOutline.svelte'
</script>

<p class="mb-2">
    Made with <Code class="inline" size="20px" />, <Keyboard class="inline" size="20px" /> and <Love class="inline" size="20px" /> by <a class="hover:text-teal-500 dark:hover:text-teal-300" href="https://github.com/rianadon">@rianadon</a>.
</p>
<p class="mb-2 text-gray-500 dark:text-gray-400">
    Special thanks to everyone who has made the Dactyl keyboard possible, including <a class="darka" href="https://github.com/adereth">@adereth</a> (original Dactyl Design), <a class="darka"  href="https://github.com/tshort">@tshort</a> (Dactyl Manuform) and <a class="darka" href="https://github.com/ibnuda">@ibnuda</a> (Dactyl Generator).
</p>
<p class="mb-2 text-gray-500 dark:text-gray-400">
    For trackballs and more advanced settings, I recommend <a href="https://github.com/joshreve/dactyl-keyboard">@joshreve's</a> and <a href="https://github.com/bullwinkle3000/dactyl-keyboard">@bullwinkle3000's</a> CadQuery generators, which can also output STEP files.
</p>
<p class="mb-2 text-gray-500 dark:text-gray-400">
    Do not fear if you see holes in your model preview or if your keyboard looks holy (sorry). Downloading the STL file may give you a complete, hole-free model.
</p>
<p class="mb-2 text-gray-500 dark:text-gray-400">
    You can find this website's source on <a href="https://github.com/rianadon/dactyl-configurator">GitHub</a>. It's licensed under the <a href="https://github.com/rianadon/dactyl-configurator/blob/main/LICENSE.md">AGPL</a>.
</p>

<style lang="postcss">
 a {
     @apply hover:text-teal-300 dark:hover:text-teal-500 underline;
 }
</style>
