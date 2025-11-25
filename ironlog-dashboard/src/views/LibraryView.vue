<script setup lang="ts">
import { ref } from 'vue'
import { useExerciseStore, type Exercise } from '../stores/exercises'
import ExerciseCard from '../components/ExerciseCard.vue'
import ExerciseModal from '../components/ExerciseModal.vue'

const store = useExerciseStore()
const showModal = ref(false)
const selectedExercise = ref<Exercise | null>(null)
const selectedImage = ref<string | null>(null)

function openModal(payload: { exercise: Exercise, image: string | null }) {
  selectedExercise.value = payload.exercise
  selectedImage.value = payload.image
  showModal.value = true
}
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <div class="text-center mb-12">
      <h1 class="text-4xl md:text-5xl font-black text-primary mb-4 tracking-tighter uppercase drop-shadow-[0_0_10px_rgba(224,93,38,0.3)]">
        My Library
      </h1>
      <p class="text-gray-400 max-w-2xl mx-auto mb-4">
        Your personal collection of exercises.
      </p>
    </div>

    <div v-if="store.library.length === 0" class="text-center py-20 text-gray-500">
      <p class="text-xl font-bold mb-2">Your library is empty</p>
      <p class="text-sm">Go to the Catalog to add exercises.</p>
      <router-link 
        to="/" 
        class="inline-block mt-4 px-6 py-2 bg-surface border border-primary text-primary rounded-full hover:bg-primary hover:text-black transition-colors font-bold uppercase text-sm"
      >
        Browse Catalog
      </router-link>
    </div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 p-4">
      <ExerciseCard 
        v-for="exercise in store.library" 
        :key="exercise.id" 
        :exercise="exercise" 
        @open-modal="openModal"
      />
    </div>

    <ExerciseModal 
      v-if="selectedExercise" 
      :exercise="selectedExercise" 
      :initial-image="selectedImage"
      :is-open="showModal" 
      @close="showModal = false" 
    />
  </div>
</template>
