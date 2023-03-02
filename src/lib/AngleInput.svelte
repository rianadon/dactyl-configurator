<script lang="ts">
 export let value: number;

 $: degrees = Math.round(1800 * value/8100) / 10

 function onChange(e) {
     value = Math.round(8100 * e.target.value / 180)
 }
</script>

<input class="input w-36 ml-2 rounded-l" type="number" min="-179" max="180" value={degrees} step="0.1" on:change={onChange} /><span class="input w-8 mr-2 rounded-r">&deg;</span>

<style lang="postcss">
 .input {
     @apply focus:border-teal-500 border border-transparent text-gray-700 focus:outline-none;
     @apply border-gray-200 dark:border-transparent bg-gray-100 dark:bg-gray-700 dark:text-gray-100;
     @apply inline-block appearance-none px-2;
 }
</style>
