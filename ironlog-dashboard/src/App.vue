<script setup lang="ts">
import { RouterView } from 'vue-router'
import { useExerciseStore } from './stores/exercises'
import { useAuthStore } from './stores/auth'
import { storeToRefs } from 'pinia'

import { onMounted } from 'vue'

const store = useExerciseStore()
const authStore = useAuthStore()
const { library } = storeToRefs(store)

onMounted(() => {
  // Safety check: Ensure scroll is enabled
  document.body.style.overflow = 'auto'
})
</script>

<template>
  <div class="min-h-screen bg-background text-text flex">
    <!-- Sidebar (Enhanced) -->
    <aside class="w-20 lg:w-64 bg-gradient-to-b from-surface to-[#0a0a0a] border-r border-gray-800 flex flex-col hidden md:flex sticky top-0 h-screen">
      <div class="p-2 flex items-center justify-center gap-3">
        <img src="/logo-v4.png" alt="IronLog Logo" class="w-20 h-20 lg:w-60 lg:h-60 object-contain drop-shadow-[0_0_20px_rgba(224,93,38,0.5)] transition-all duration-300">
      </div>
      
      <nav class="flex-1 px-4 py-6 space-y-2">
        <router-link to="/" class="flex items-center gap-3 px-4 py-3 text-gray-400 hover:bg-gray-800 hover:text-white rounded-xl transition-all duration-300 border border-transparent" exact-active-class="bg-primary/10 text-primary border-primary/20 shadow-[0_0_15px_rgba(224,93,38,0.1)]">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
          </svg>
          <span class="hidden lg:block font-medium">Catalog</span>
        </router-link>
        <router-link to="/library" class="flex items-center gap-3 px-4 py-3 text-gray-400 hover:bg-gray-800 hover:text-white rounded-xl transition-all duration-300 border border-transparent" exact-active-class="bg-primary/10 text-primary border-primary/20 shadow-[0_0_15px_rgba(224,93,38,0.1)]">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
          </svg>
          <span class="hidden lg:block font-medium">Library</span>
          <span v-if="library.length" class="ml-auto bg-primary text-black text-xs font-bold px-2 py-0.5 rounded-full hidden lg:block">{{ library.length }}</span>
        </router-link>
      </nav>

      <!-- Daily Motivation Card -->
      <div class="px-4 mb-6 hidden lg:block">
        <div class="bg-gray-900/50 rounded-xl p-4 border border-gray-800 relative overflow-hidden group hover:border-primary/30 transition-colors duration-500">
          <div class="absolute inset-0 bg-gradient-to-br from-primary/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
          <div class="relative z-10">
            <div class="flex items-center gap-2 mb-2">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-3 w-3 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
              <p class="text-[10px] text-primary font-black uppercase tracking-widest">Daily Grind</p>
            </div>
            <p class="text-xs text-gray-400 font-medium leading-relaxed italic">"Consistency is what transforms average into excellence."</p>
          </div>
        </div>
      </div>

      <div class="p-4 border-t border-gray-800">
        <div v-if="authStore.user" class="flex items-center gap-3">
          <img 
            v-if="authStore.user.photoURL" 
            :src="authStore.user.photoURL" 
            alt="User Avatar" 
            class="w-10 h-10 rounded-full border border-gray-700"
          >
          <div v-else class="w-10 h-10 rounded-full bg-primary flex items-center justify-center text-black font-bold">
            {{ authStore.user.email?.charAt(0).toUpperCase() }}
          </div>
          
          <div class="hidden lg:block flex-1 min-w-0">
            <p class="text-sm font-medium truncate">{{ authStore.user.displayName || 'User' }}</p>
            <p class="text-xs text-gray-500 truncate">{{ authStore.user.email }}</p>
          </div>

          <button 
            @click="authStore.logout()" 
            class="text-gray-400 hover:text-red-500 transition-colors p-2"
            title="Logout"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
          </button>
        </div>

        <div v-else class="flex items-center gap-3">
          <router-link 
            to="/login"
            class="w-full flex items-center justify-center gap-2 bg-primary/10 text-primary hover:bg-primary hover:text-black py-2 rounded-lg transition-all duration-300 font-bold uppercase text-xs tracking-wider"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
            </svg>
            <span class="hidden lg:inline">Sign In</span>
          </router-link>
        </div>
      </div>
    </aside>

    <!-- Mobile Header -->
    <div class="md:hidden fixed top-0 w-full bg-surface z-50 border-b border-gray-800 p-4 flex justify-between items-center">
       <div class="flex items-center gap-2">
         <img src="/logo-v4.png" alt="IronLog Logo" class="w-20 h-20 object-contain">
       </div>
       <button class="text-gray-400">
         <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
           <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16m-7 6h7" />
         </svg>
       </button>
    </div>

    <!-- Main Content -->
    <div class="flex-1 flex flex-col min-h-screen md:ml-0 pt-16 md:pt-0">
      <RouterView />
    </div>
  </div>
</template>
