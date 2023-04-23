<script lang="ts">
 import Close from 'svelte-material-icons/Close.svelte'
 import { createEventDispatcher } from 'svelte';

 const dispatch = createEventDispatcher()

 function close() {
     dispatch('close')
 }
</script>

<div class="fixed inset-0 bg-gray-900/80" on:click={close}>
  <div class="absolute left-0 right-0 top-24 bottom-0">
	<div class="mx-auto w-[40rem] max-w-full text-center px-4 md:px-8 py-8 rounded-md bg-white dark:bg-gray-800 max-h-full overflow-auto" on:click|stopPropagation>
      <div class="relative">
		<h3 class="text-2xl font-medium text-gray-900 dark:text-white"><slot name="title"></slot></h3>
        <button class="absolute right-0 top-1" on:click={close}>
          <Close size="24" class="text-gray-800 dark:text-gray-100" />
        </button>
      </div>
	  <div class="mt-2 py-3 text-start">
		<slot name="content"></slot>
	  </div>
	</div>
  </div>
</div>
