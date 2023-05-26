import { defineConfig } from 'vite'
import { svelte } from '@sveltejs/vite-plugin-svelte'
import MagicString from "magic-string";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [svelte()],
  worker: {
    plugins: [transformImports()]
  },
  build: {
    sourcemap: true
  },
})

function transformImports() {
  return {
    name: 'transform-imports',

    transform(src, id) {
      // Resolves some issues that come from developing with web worker modules
      // But building without them.
      let s: MagicString = new MagicString(src);
      s.replace(/import\("(.*?)"\)/, (_, library) => {
        if (library == 'module') return 'self'
        s.prepend(`import libUrl from "${library}?url";\n`)
        return 'importScripts(libUrl);Promise.resolve()'
      })
      s.replace('import.meta.url', '""')
      return {
        code: s.toString(),
        map: s.generateMap(),
      }
    }
  }
}
