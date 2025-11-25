<script setup lang="ts">
import { onMounted } from 'vue'
import { useExerciseStore } from '../stores/exercises'
import { storeToRefs } from 'pinia'

const store = useExerciseStore()
const { equipmentList, selectedEquipment } = storeToRefs(store)

onMounted(() => {
  store.fetchEquipmentList()
})
</script>

<template>
  <div class="w-full overflow-x-auto pb-4 mb-6 custom-scrollbar">
    <div class="flex space-x-4 px-2">
      <button
        v-for="item in equipmentList"
        :key="item"
        @click="store.selectEquipment(item)"
        class="whitespace-nowrap px-6 py-2 rounded-full text-sm font-bold uppercase tracking-wider transition-all duration-300 border"
        :class="[
          selectedEquipment === item 
            ? 'bg-secondary text-black border-secondary shadow-[0_0_15px_rgba(245,158,11,0.4)]' 
            : 'bg-surface text-gray-400 border-gray-700 hover:border-secondary hover:text-white'
        ]"
      >
        {{ item }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
    height: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
    background: rgba(57, 255, 20, 0.3);
    border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
    background: rgba(57, 255, 20, 0.6);
}
</style>
