
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import type { Exercise } from '../stores/exercises'
import { useExerciseStore } from '../stores/exercises'

const props = defineProps<{
  exercise: Exercise
}>()

const store = useExerciseStore()
const imageUrl = ref<string | null>(null)
const isLoadingImage = ref(false)
const imageError = ref(false)
const cardRef = ref<HTMLElement | null>(null)
let observer: IntersectionObserver | null = null

import { computed } from 'vue'

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

async function fetchImage() {
  if (imageUrl.value || isLoadingImage.value) return
  
  isLoadingImage.value = true
  try {
    const url = `/api/image?exerciseId=${props.exercise.id}&resolution=180`
    const response = await fetch(url, {
      headers: {
        'x-rapidapi-key': import.meta.env.VITE_RAPIDAPI_KEY,
        'x-rapidapi-host': import.meta.env.VITE_RAPIDAPI_HOST
      }
    })
    
    if (!response.ok) throw new Error('Failed to load image')
    
    const blob = await response.blob()
    imageUrl.value = URL.createObjectURL(blob)
  } catch (e) {
    console.error('Image load error:', e)
    imageError.value = true
    if (e instanceof Error) {
        console.error('Error details:', e.message)
    }
  } finally {
    isLoadingImage.value = false
  }
}

onMounted(() => {
  // Lazy load image when card is visible
  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting) {
      fetchImage()
      if (cardRef.value && observer) observer.unobserve(cardRef.value)
    }
  })
  
  if (cardRef.value) observer.observe(cardRef.value)
})

onUnmounted(() => {
  if (observer) observer.disconnect()
  if (imageUrl.value) URL.revokeObjectURL(imageUrl.value)
})
</script>

<template>
  <div 
    ref="cardRef"
    class="bg-surface rounded-xl overflow-hidden shadow-lg hover:shadow-primary/20 transition-all duration-300 group border border-gray-800 hover:border-primary cursor-pointer"
    @click="$emit('open-modal', { exercise, image: imageUrl })"
  >
    <!-- Image Container -->
    <div class="relative h-48 bg-black overflow-hidden flex items-center justify-center">
      <div v-if="isLoadingImage" class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-primary"></div>
      <img 
        v-else-if="imageUrl"
        :src="imageUrl" 
        :alt="exercise.name" 
        class="w-full h-full object-cover opacity-80 group-hover:opacity-100 transition-opacity duration-300"
      >
      <div v-else class="text-gray-600 text-xs flex flex-col items-center">
        <span>No GIF</span>
        <span v-if="imageError" class="text-[8px] text-red-500">Load Failed</span>
      </div>
      
      <!-- Badge -->
      <div class="absolute top-2 left-2">
        <span class="px-2 py-1 bg-primary text-black text-[10px] font-bold uppercase rounded-md shadow-md">
          {{ exercise.bodyPart }}
        </span>
      </div>
    </div>

    <!-- Content -->
    <div class="p-4">
      <h3 class="text-lg font-bold text-white mb-1 truncate capitalize group-hover:text-primary transition-colors">
        {{ exercise.name }}
      </h3>
      <p class="text-xs text-gray-400 capitalize mb-4">
        {{ exercise.target }} • {{ exercise.equipment }}
      </p>
      
      <button 
        @click.stop="toggleLibrary"
        class="w-full py-2 rounded-lg text-xs font-bold uppercase tracking-wider transition-all duration-300 flex items-center justify-center gap-2"
        :class="isInLibrary ? 'bg-red-500/10 text-red-500 border border-red-500/50 hover:bg-red-500 hover:text-white' : 'bg-surface border border-gray-700 text-gray-300 hover:border-primary hover:text-primary'"
      >
        <span v-if="isInLibrary">- Remove</span>
        <span v-else>+ Add to Library</span>
      </button>
    </div>
  </div>
</template>
