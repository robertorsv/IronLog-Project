<script setup lang="ts">
import { ref, watch } from 'vue'
import { useExerciseStore } from '../stores/exercises'

const store = useExerciseStore()
const localQuery = ref('')
let debounceTimeout: number | null = null

watch(localQuery, (newValue) => {
  if (debounceTimeout) clearTimeout(debounceTimeout)
  
  debounceTimeout = setTimeout(() => {
    store.searchExercises(newValue)
  }, 500) // 500ms debounce
})

// Sync from store to local (e.g. when filters clear search)
watch(() => store.searchQuery, (newValue) => {
  if (newValue !== localQuery.value) {
    localQuery.value = newValue
  }
})
</script>

<template>
  <div class="relative w-full max-w-2xl mx-auto mb-8">
    <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
      <svg aria-hidden="true" class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
    </div>
    <input 
      v-model="localQuery"
      type="search" 
      class="block w-full p-4 pl-10 text-sm text-text bg-surface border border-gray-700 rounded-lg focus:ring-primary focus:border-primary placeholder-gray-400 transition-all duration-300 shadow-lg hover:shadow-primary/10" 
      placeholder="Search exercises (e.g. Bench Press, Chest...)" 
      required
    >
  </div>
</template>
