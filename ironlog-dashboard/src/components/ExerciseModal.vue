<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import type { Exercise } from '../stores/exercises'
import { useExerciseStore } from '../stores/exercises'
import { computed } from 'vue'

const store = useExerciseStore()

const isInLibrary = computed(() => {
  return store.library.some(e => e.id === props.exercise.id)
})

function toggleLibrary() {
  if (isInLibrary.value) {
    store.removeFromLibrary(props.exercise.id)
  } else {
    store.addToLibrary(props.exercise)
  }
}

const props = defineProps<{
  exercise: Exercise
  isOpen: boolean
  initialImage?: string | null
}>()

const emit = defineEmits(['close'])

const imageUrl = ref<string | null>(null)
const isLoadingImage = ref(false)

async function fetchImage() {
  if (!props.exercise.id) return
  isLoadingImage.value = true
  try {
    const response = await fetch(`/api/image?exerciseId=${props.exercise.id}&resolution=360`, {
      headers: {
        'x-rapidapi-key': import.meta.env.VITE_RAPIDAPI_KEY,
        'x-rapidapi-host': import.meta.env.VITE_RAPIDAPI_HOST
      }
    })
    if (response.ok) {
      const blob = await response.blob()
      imageUrl.value = URL.createObjectURL(blob)
    }
  } catch (e) {
    console.error('Failed to load modal image', e)
  } finally {
    isLoadingImage.value = false
  }
}

function updateImage() {
  if (props.initialImage) {
    imageUrl.value = props.initialImage
    isLoadingImage.value = false
  } else {
    imageUrl.value = null
    fetchImage()
  }
}

onMounted(() => {
  if (props.isOpen) updateImage()
  document.body.style.overflow = 'hidden'
})

onUnmounted(() => {
  if (imageUrl.value && !props.initialImage) URL.revokeObjectURL(imageUrl.value)
  document.body.style.overflow = 'auto'
})

watch(() => props.exercise, () => {
  if (props.isOpen) updateImage()
})

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    updateImage()
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = 'auto'
  }
})

function close() {
  emit('close')
}
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
    <!-- Backdrop -->
    <div @click="close" class="absolute inset-0 bg-black/80 backdrop-blur-sm transition-opacity"></div>
    
    <!-- Modal Content -->
    <div class="relative bg-surface border border-gray-700 rounded-2xl w-full max-w-4xl max-h-[90vh] overflow-y-auto shadow-2xl flex flex-col md:flex-row">
      
      <!-- Close Button -->
      <button @click="close" class="absolute top-4 right-4 text-gray-400 hover:text-white z-10 bg-black/50 rounded-full p-2">
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
      </button>

      <!-- Image Section -->
      <div class="w-full md:w-1/2 bg-black flex items-center justify-center p-8 min-h-[300px]">
        <div v-if="isLoadingImage" class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
        <img v-else-if="imageUrl" :src="imageUrl" :alt="exercise.name" class="max-w-full max-h-[400px] object-contain rounded-lg shadow-lg">
        <div v-else class="text-gray-500">No Image Available</div>
      </div>

      <!-- Details Section -->
      <div class="w-full md:w-1/2 p-8 flex flex-col">
        <div class="flex justify-between items-start mb-2">
          <h2 class="text-3xl font-black text-white uppercase">{{ exercise.name }}</h2>
          <button 
            @click="toggleLibrary"
            class="shrink-0 ml-4 px-4 py-2 rounded-lg text-xs font-bold uppercase tracking-wider transition-all duration-300 flex items-center gap-2"
            :class="isInLibrary ? 'bg-red-500/10 text-red-500 border border-red-500/50 hover:bg-red-500 hover:text-white' : 'bg-surface border border-gray-700 text-gray-300 hover:border-primary hover:text-primary'"
          >
            <span v-if="isInLibrary">- Remove</span>
            <span v-else>+ Add to Library</span>
          </button>
        </div>
        
        <div class="flex flex-wrap gap-2 mb-6">
          <span class="px-3 py-1 bg-primary/20 text-primary text-xs font-bold rounded-full uppercase border border-primary/30">
            {{ exercise.bodyPart }}
          </span>
          <span class="px-3 py-1 bg-secondary/20 text-secondary text-xs font-bold rounded-full uppercase border border-secondary/30">
            {{ exercise.equipment }}
          </span>
          <span class="px-3 py-1 bg-gray-700 text-gray-300 text-xs font-bold rounded-full uppercase">
            {{ exercise.target }}
          </span>
        </div>

        <div class="space-y-6 overflow-y-auto pr-2 custom-scrollbar">
          <div>
            <h3 class="text-lg font-bold text-white mb-2 uppercase tracking-wider border-b border-gray-700 pb-1">Instructions</h3>
            <ol class="list-decimal list-inside space-y-2 text-gray-300 text-sm leading-relaxed">
              <li v-for="(step, index) in exercise.instructions" :key="index">
                {{ step }}
              </li>
            </ol>
          </div>

          <div v-if="exercise.secondaryMuscles && exercise.secondaryMuscles.length">
            <h3 class="text-lg font-bold text-white mb-2 uppercase tracking-wider border-b border-gray-700 pb-1">Secondary Muscles</h3>
            <div class="flex flex-wrap gap-2">
              <span v-for="muscle in exercise.secondaryMuscles" :key="muscle" class="text-xs text-gray-400 bg-gray-800 px-2 py-1 rounded">
                {{ muscle }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: #1e1e1e; 
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #333; 
  border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #555; 
}
</style>
