import { defineConfig } from 'vite'
import { svelte } from '@sveltejs/vite-plugin-svelte'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [svelte()],
  worker: {
    plugins: []
  },
  build: {
    sourcemap: true
  },
  optimizeDeps: {
    include: ['target/dactyl_node.cjs']
  }
})
