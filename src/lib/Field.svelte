<script lang="ts">
    import type { FieldSchema } from './schema'
    import Popover from 'svelte-easy-popover';
    import Help from 'svelte-material-icons/HelpCircle.svelte'
    import ChevronDown from 'svelte-material-icons/ChevronDown.svelte'
    import Check from 'svelte-material-icons/Check.svelte'

    export let defl: string|boolean|Number;
    export let schema: FieldSchema;
    export let value: any;

    let referenceElement;

    // hack to get rid of type warnings
</script>

<label class="block mb-2">
    <span class="block mb-2 lg:mb-0 lg:inline-block w-80">{schema.name}
        {#if schema.help}
            <div class="align-[-18%] inline-block text-gray-600 dark:text-gray-100" bind:this={referenceElement}>
                <Help size="20px" />
            </div>
            <Popover triggerEvents={["hover", "focus"]} {referenceElement} placement="top" spaceAway={4}>
                <div class="rounded bg-gray-200 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 px-2 py-1 mx-2">
                    {schema.help}
                </div>
            </Popover>
        {/if}
    </span>
    {#if typeof defl === "number"}
        <input class="input px-2" type="number" min={schema.min} max={schema.max} bind:value={value}/>
    {:else if typeof defl === "boolean"}
        <!-- @ts-ignore -->
        <input class="opacity-0 absolute h-0 w-0" type="checkbox" bind:checked={value}/>
        <div class="inline-block w-44 text-left px-2">
            <div class="input-basic rounded w-6 h-6 flex flex-shrink-0 justify-center items-center">
                <Check size="20px" class=" text-teal-500 pointer-events-none {value ? "" : "invisible"}" />
            </div>
        </div>
    {:else if schema.options}
        <div class="inline-block relative">
            <select class="input pl-2 pr-8" bind:value={value}>
                {#each schema.options as {value, name}}
                    <option value={value}>{name}</option>
                {/each}
            </select>
            <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center px-4 text-gray-700 dark:text-gray-100">
                <ChevronDown size="20px" />
            </div>
        </div>
    {:else}
        <input class="input px-2" bind:value={value}/>
    {/if}
</label>

<style lang="postcss">
    .input-basic {
        @apply focus:border-teal-500 border border-transparent text-gray-700 focus:outline-none;
        @apply border-gray-200 dark:border-transparent bg-gray-100 dark:bg-gray-700 dark:text-gray-100;
    }

    .input {
        @apply appearance-none w-44 rounded mx-2;
        @apply input-basic text-ellipsis;
    }

    input:focus + div>div {
        @apply border-teal-500;
    }
</style>
