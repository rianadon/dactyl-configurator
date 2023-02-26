<script lang="ts">
    export let logs: string[] = [];
</script>

<div class="fixed top-0 left-0 right-0 bottom-0 bg-gray-900/80">
	<div class="mt-24 mx-auto w-[38rem] text-center p-8 rounded-md bg-white dark:bg-gray-800">
		<h3 class="text-2xl font-medium text-gray-900 dark:text-white">Generating Your Model</h3>
		<div class="mt-2 px-7 py-3">
			<p class="text-sm text-gray-500 dark:text-gray-200">
				<slot></slot>
			</p>
            {#if logs.length }
                <code class="mt-4 block text-left text-sm">
                {#each logs as log}
                    {log}<br>
                {/each}
                </code>
            {/if}
		</div>
		<!-- <div class="items-center px-4 py-3">
			<button class="px-4 py-2 bg-teal-500 text-white text-base font-medium rounded-md w-full shadow-sm hover:bg-teal-600 focus:outline-none focus:ring-2 focus:ring-green-300">
			  Cancel
			</button>
		</div> -->
	</div>
</div>
