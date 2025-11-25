<script setup lang="ts">
import { useExerciseStore } from '../stores/exercises'
import { storeToRefs } from 'pinia'
import ExerciseCard from './ExerciseCard.vue'

const store = useExerciseStore()
const { filteredExercises, isLoading, error, hasMore } = storeToRefs(store)

function loadMore() {
  store.fetchExercises()
}
</script>

<template>
  <!-- Loading State (Initial) -->
  <div v-if="isLoading && filteredExercises.length === 0" class="flex justify-center items-center py-20">
    <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
  </div>

  <!-- Error State -->
  <div v-else-if="error && filteredExercises.length === 0" class="text-center py-20 text-red-500">
    <p class="text-xl font-bold">Error loading exercises</p>
    <p class="text-sm">{{ error }}</p>
  </div>

  <!-- Grid -->
  <div v-else>
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 p-4">
      <ExerciseCard 
        v-for="exercise in filteredExercises" 
        :key="exercise.id" 
        :exercise="exercise" 
        @open-modal="$emit('open-modal', $event)"
      />
    </div>
    
    <!-- Pagination Buttons -->
    <div class="flex justify-center mt-8 pb-8 gap-4">
      <div v-if="isLoading" class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-primary"></div>
      
      <template v-else>
        <button 
          v-if="hasMore"
          @click="loadMore" 
          class="px-8 py-3 bg-surface border border-primary text-primary rounded-full hover:bg-primary hover:text-black transition-all duration-300 font-bold uppercase tracking-wider text-sm shadow-lg hover:shadow-primary/50"
        >
          Load More
        </button>

        <button 
          v-if="filteredExercises.length > 4"
          @click="store.showLess()" 
          class="px-8 py-3 bg-surface border border-gray-500 text-gray-300 rounded-full hover:bg-gray-700 hover:text-white transition-all duration-300 font-bold uppercase tracking-wider text-sm shadow-lg"
        >
          Show Less
        </button>
      </template>
      
      <p v-if="error" class="text-red-500 text-sm w-full text-center">{{ error }}</p>
    </div>
  </div>
  
  <div v-if="!isLoading && !error && filteredExercises.length === 0" class="text-center py-20 text-gray-500">
    <p class="text-xl">No exercises found.</p>
    <p class="text-sm">Try adjusting your search terms.</p>
  </div>
</template>

