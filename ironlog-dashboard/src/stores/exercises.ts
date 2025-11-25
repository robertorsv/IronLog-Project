import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { mockExercises, mockBodyParts, mockEquipmentList } from '../data/mockData'
import { db, auth } from '../firebase'
import {
    collection,
    doc,
    setDoc,
    deleteDoc,
    onSnapshot,
    query
} from 'firebase/firestore'
import { useAuthStore } from './auth'

export interface Exercise {
    id: string
    name: string
    bodyPart: string
    gifUrl: string
    target: string
    equipment: string
    instructions?: string[]
    secondaryMuscles?: string[]
}

export const useExerciseStore = defineStore('exercises', () => {
    const searchQuery = ref('')
    const selectedCategory = ref('all')
    const selectedEquipment = ref('all')
    const bodyParts = ref<string[]>([])
    const equipmentList = ref<string[]>([])
    const library = ref<Exercise[]>([])
    const allExercises = ref<Exercise[]>([])
    const isLoading = ref(false)
    const error = ref<string | null>(null)
    const devMode = ref(false)

    // Pagination state
    const offset = ref(0)
    const hasMore = ref(true)
    const limit = 4

    // For compatibility, filteredExercises just returns the loaded exercises
    // (Server does the filtering now)
    const filteredExercises = computed(() => allExercises.value)

    function toggleDevMode() {
        devMode.value = !devMode.value
        // Reset everything when toggling
        allExercises.value = []
        bodyParts.value = []
        equipmentList.value = []
        offset.value = 0
        hasMore.value = true
        error.value = null

        // Refetch data
        fetchBodyParts()
        fetchEquipmentList()
        fetchExercises(true)
    }

    async function fetchBodyParts() {
        if (bodyParts.value.length > 0) return

        if (devMode.value) {
            console.log('Dev Mode: Using mock body parts')
            bodyParts.value = ['all', ...mockBodyParts]
            return
        }

        try {
            const response = await fetch('https://exercisedb.p.rapidapi.com/exercises/bodyPartList', {
                method: 'GET',
                headers: {
                    'x-rapidapi-key': import.meta.env.VITE_RAPIDAPI_KEY,
                    'x-rapidapi-host': import.meta.env.VITE_RAPIDAPI_HOST
                }
            })

            if (response.ok) {
                const data = await response.json()
                bodyParts.value = ['all', ...data]
            }
        } catch (e) {
            console.error('Failed to fetch body parts', e)
        }
    }

    async function fetchEquipmentList() {
        if (equipmentList.value.length > 0) return

        if (devMode.value) {
            console.log('Dev Mode: Using mock equipment list')
            equipmentList.value = ['all', ...mockEquipmentList]
            return
        }

        try {
            const response = await fetch('https://exercisedb.p.rapidapi.com/exercises/equipmentList', {
                method: 'GET',
                headers: {
                    'x-rapidapi-key': import.meta.env.VITE_RAPIDAPI_KEY,
                    'x-rapidapi-host': import.meta.env.VITE_RAPIDAPI_HOST
                }
            })

            if (response.ok) {
                const data = await response.json()
                equipmentList.value = ['all', ...data]
            }
        } catch (e) {
            console.error('Failed to fetch equipment list', e)
        }
    }

    async function fetchExercises(reset = false) {
        if (isLoading.value && !reset) return

        if (reset) {
            allExercises.value = []
            offset.value = 0
            hasMore.value = true
            error.value = null
        }

        if (!hasMore.value && !reset) return

        isLoading.value = true
        error.value = null

        if (devMode.value) {
            console.log('Dev Mode: Using mock exercises')
            // Simulate network delay
            await new Promise(resolve => setTimeout(resolve, 500))

            let results = [...mockExercises]

            // Filter by search
            if (searchQuery.value.trim()) {
                const q = searchQuery.value.toLowerCase()
                results = results.filter(e => e.name.toLowerCase().includes(q) || e.target.toLowerCase().includes(q))
            }

            // Filter by category
            if (selectedCategory.value && selectedCategory.value !== 'all') {
                results = results.filter(e => e.bodyPart === selectedCategory.value)
            }

            // Filter by equipment
            if (selectedEquipment.value && selectedEquipment.value !== 'all') {
                results = results.filter(e => e.equipment === selectedEquipment.value)
            }

            // Pagination
            const start = offset.value
            const end = start + limit
            const page = results.slice(start, end)

            if (page.length < limit) {
                hasMore.value = false
            }

            allExercises.value = [...allExercises.value, ...page]
            offset.value += limit
            isLoading.value = false
            return
        }

        const headers = {
            'x-rapidapi-key': import.meta.env.VITE_RAPIDAPI_KEY,
            'x-rapidapi-host': import.meta.env.VITE_RAPIDAPI_HOST
        }

        try {
            let url = `https://exercisedb.p.rapidapi.com/exercises?limit=${limit}&offset=${offset.value}`
            let isCombinedFetch = false

            // Search takes precedence
            if (searchQuery.value.trim()) {
                url = `https://exercisedb.p.rapidapi.com/exercises/name/${encodeURIComponent(searchQuery.value.trim())}?limit=${limit}&offset=${offset.value}`
            }
            // Combined: Category AND Equipment
            else if (selectedCategory.value !== 'all' && selectedEquipment.value !== 'all') {
                // Strategy: Fetch ALL exercises for the body part, then filter by equipment locally
                // This avoids the pagination issue where a page of body part exercises might have 0 equipment matches
                url = `https://exercisedb.p.rapidapi.com/exercises/bodyPart/${encodeURIComponent(selectedCategory.value)}?limit=1300`
                isCombinedFetch = true
            }
            // Then category
            else if (selectedCategory.value && selectedCategory.value !== 'all') {
                url = `https://exercisedb.p.rapidapi.com/exercises/bodyPart/${encodeURIComponent(selectedCategory.value)}?limit=${limit}&offset=${offset.value}`
            }
            // Then equipment
            else if (selectedEquipment.value && selectedEquipment.value !== 'all') {
                url = `https://exercisedb.p.rapidapi.com/exercises/equipment/${encodeURIComponent(selectedEquipment.value)}?limit=${limit}&offset=${offset.value}`
            }

            console.log(`Fetching: ${url}`)
            const response = await fetch(url, { method: 'GET', headers })

            if (!response.ok) {
                if (response.status === 429) {
                    error.value = 'API Rate Limit Exceeded. Switch to Dev Mode.'
                    throw new Error('API Rate Limit Exceeded')
                }
                if (response.status === 404) {
                    hasMore.value = false
                    isLoading.value = false
                    return
                }
                throw new Error(`API Error: ${response.status} ${response.statusText}`)
            }

            let data = await response.json()

            if (!Array.isArray(data)) {
                console.error('Unexpected data:', data)
                hasMore.value = false
            } else {
                // Handle Combined Filtering
                if (isCombinedFetch) {
                    data = data.filter((e: Exercise) => e.equipment === selectedEquipment.value)
                    // Since we fetched ALL, we disable further pagination
                    hasMore.value = false
                } else {
                    if (data.length < limit) {
                        hasMore.value = false
                    }
                }

                // Filter out duplicates if any
                const newExercises = data.filter((newEx: Exercise) => !allExercises.value.some(existing => existing.id === newEx.id))

                allExercises.value = [...allExercises.value, ...newExercises]

                if (!isCombinedFetch) {
                    offset.value += limit
                }
            }

        } catch (err) {
            const msg = err instanceof Error ? err.message : 'Unknown error occurred'
            error.value = msg
            console.error('Failed to fetch exercises:', err)
        } finally {
            isLoading.value = false
        }
    }

    function searchExercises(query: string) {
        searchQuery.value = query
        // Only reset filters if we are actually searching for something
        // If query is empty (clearing search), we want to keep current filters (or they are already all)
        if (query.trim()) {
            selectedCategory.value = 'all'
            selectedEquipment.value = 'all'
        }
        fetchExercises(true)
    }

    function selectCategory(category: string) {
        if (selectedCategory.value === category && category !== 'all') {
            selectedCategory.value = 'all'
        } else {
            selectedCategory.value = category
        }
        // Do NOT reset selectedEquipment
        searchQuery.value = ''
        fetchExercises(true)
    }

    function selectEquipment(equipment: string) {
        if (selectedEquipment.value === equipment && equipment !== 'all') {
            selectedEquipment.value = 'all'
        } else {
            selectedEquipment.value = equipment
        }
        // Do NOT reset selectedCategory
        searchQuery.value = ''
        fetchExercises(true)
    }

    // Firestore Sync Logic
    let unsubscribeLibrary: (() => void) | null = null

    function initLibrarySync() {
        // Stop previous listener if any
        if (unsubscribeLibrary) {
            unsubscribeLibrary()
            unsubscribeLibrary = null
        }

        const user = auth.currentUser
        if (!user) {
            library.value = []
            return
        }

        const q = query(collection(db, `users/${user.uid}/exercises`))

        unsubscribeLibrary = onSnapshot(q, (snapshot) => {
            library.value = snapshot.docs.map(doc => doc.data() as Exercise)
        }, (err) => {
            console.error("Library sync error:", err)
        })
    }

    // Watch for auth changes to restart sync
    const authStore = useAuthStore()
    watch(() => authStore.user, (newUser) => {
        if (newUser) {
            initLibrarySync()
        } else {
            if (unsubscribeLibrary) {
                unsubscribeLibrary()
                unsubscribeLibrary = null
            }
            library.value = []
        }
    })

    async function addToLibrary(exercise: Exercise) {
        if (!authStore.user) {
            alert("Please login to save exercises.")
            return
        }

        try {
            // Firestore doesn't like 'undefined' values, so we sanitize the object
            const exerciseData = JSON.parse(JSON.stringify(exercise))

            // Ensure gifUrl is present if missing
            if (!exerciseData.gifUrl) {
                exerciseData.gifUrl = `https://exercisedb.p.rapidapi.com/image?exerciseId=${exercise.id}&resolution=360`
            } else if (!exerciseData.gifUrl.includes('resolution=')) {
                // Append resolution if missing
                exerciseData.gifUrl += '&resolution=360'
            }

            await setDoc(doc(db, `users/${authStore.user.uid}/exercises`, exercise.id), exerciseData)
        } catch (e: any) {
            console.error("Failed to add to library", e)
            alert(`Failed to save exercise: ${e.message}`)
        }
    }

    async function removeFromLibrary(id: string) {
        if (!authStore.user) return

        try {
            await deleteDoc(doc(db, `users/${authStore.user.uid}/exercises`, id))
        } catch (e) {
            console.error("Failed to remove from library", e)
        }
    }

    function showLess() {
        if (allExercises.value.length > limit) {
            const newLength = Math.max(limit, allExercises.value.length - limit)
            allExercises.value = allExercises.value.slice(0, newLength)
            offset.value = newLength
            hasMore.value = true
        }
    }

    return {
        searchQuery,
        selectedCategory,
        selectedEquipment,
        bodyParts,
        equipmentList,
        allExercises,
        filteredExercises,
        library,
        isLoading,
        error,
        hasMore,
        devMode,
        toggleDevMode,
        fetchBodyParts,
        fetchEquipmentList,
        fetchExercises,
        searchExercises,
        selectCategory,
        selectEquipment,
        showLess,
        addToLibrary,
        removeFromLibrary
    }
})
