<script lang="ts">
 import Info from 'svelte-material-icons/AlertCircleOutline.svelte'
</script>
<div class="max-w-[32.3rem] text-sm mb-1 bg-purple-50 dark:bg-purple-400/10 dark:border-gray-900 mx-[-0.5rem] pl-2 pr-4 py-2 rounded flex gap-3 relative">
  <div>
    <Info size="20" class="text-purple-600" />
    <div class="absolute top-9 left-[calc(7px+0.5rem)] bottom-2 w-[6px] bg-purple-300 rounded" />
  </div>
  <div class="opacity-80 dark:opacity-100">
    <slot></slot>
  </div>
</div>
