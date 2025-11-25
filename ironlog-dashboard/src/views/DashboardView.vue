<script setup lang="ts">
import { onMounted, ref } from 'vue'
import SearchBar from '../components/SearchBar.vue'
import ExerciseGrid from '../components/ExerciseGrid.vue'
import CategorySelector from '../components/CategorySelector.vue'
import EquipmentSelector from '../components/EquipmentSelector.vue'
import ExerciseModal from '../components/ExerciseModal.vue'
import { useExerciseStore, type Exercise } from '../stores/exercises'
import { useAuthStore } from '../stores/auth'

const store = useExerciseStore()
const authStore = useAuthStore()
const showModal = ref(false)
const showFilters = ref(false)
const selectedExercise = ref<Exercise | null>(null)
const selectedImage = ref<string | null>(null)

onMounted(() => {
  store.fetchExercises()
})

function openModal(payload: { exercise: Exercise, image: string | null }) {
  selectedExercise.value = payload.exercise
  selectedImage.value = payload.image
  showModal.value = true
}
</script>

<template>
  <div class="min-h-screen bg-background text-text">
    <div class="container mx-auto px-4 py-8">
      <!-- Header -->
      <div class="text-center mb-12">
        <h1 class="text-4xl md:text-5xl font-black text-primary mb-4 tracking-tighter uppercase drop-shadow-[0_0_10px_rgba(224,93,38,0.3)]">
          IronLog Catalog
        </h1>
        <p class="text-gray-400 max-w-2xl mx-auto mb-4">
          Browse and manage your exercise library. Search for exercises and add them to your personal collection.
        </p>
        
        <div class="flex flex-col items-center gap-2">
          <span v-if="store.allExercises.length > 0" class="text-xs text-primary font-mono">
            Loaded {{ store.allExercises.length }} exercises
          </span>
        </div>
      </div>
      
      <!-- Top Right Controls -->
      <div class="absolute top-4 right-4 flex items-center gap-4">
        <!-- Dev Mode Button -->
        <button 
          @click="store.toggleDevMode()" 
          class="text-[10px] font-mono border border-gray-700 px-2 py-1 rounded hover:border-primary transition-colors backdrop-blur-sm bg-background/80"
          :class="store.devMode ? 'text-primary bg-primary/10' : 'text-gray-600'"
        >
          {{ store.devMode ? 'DEV MODE: ON' : 'DEV MODE: OFF' }}
        </button>
      </div>


      <!-- Search & Filters -->
      <div class="max-w-4xl mx-auto mb-8">
        <SearchBar />
        
        <div class="flex justify-center mb-6">
          <button 
            @click="showFilters = !showFilters"
            class="flex items-center gap-2 px-6 py-2 rounded-full border border-gray-700 bg-surface text-gray-300 hover:border-primary hover:text-primary transition-all duration-300 text-sm font-bold uppercase tracking-wider group"
          >
            <span>Filters</span>
            <svg 
              class="w-4 h-4 transition-transform duration-300"
              :class="{ 'rotate-180': showFilters }"
              fill="none" 
              stroke="currentColor" 
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
          </button>
        </div>

        <div 
          class="overflow-hidden transition-all duration-500 ease-in-out"
          :class="showFilters ? 'max-h-[500px] opacity-100' : 'max-h-0 opacity-0'"
        >
          <div class="flex flex-col lg:flex-row gap-8">
            <div class="flex-1 min-w-0">
              <div class="mb-2 text-sm font-bold text-gray-500 uppercase tracking-wider pl-2">Target Muscle</div>
              <CategorySelector />
            </div>
            
            <div class="flex-1 min-w-0">
              <div class="mb-2 text-sm font-bold text-gray-500 uppercase tracking-wider pl-2">Equipment</div>
              <EquipmentSelector />
            </div>
          </div>
        </div>
      </div>
      <ExerciseGrid @open-modal="openModal" />
      
      <ExerciseModal 
        v-if="selectedExercise" 
        :exercise="selectedExercise" 
        :initial-image="selectedImage"
        :is-open="showModal" 
        @close="showModal = false" 
      />
    </div>
  </div>
</template>
