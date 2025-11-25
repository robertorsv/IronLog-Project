import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { auth } from '../firebase'
import {
    signOut,
    onAuthStateChanged,
    GoogleAuthProvider,
    signInWithPopup,
    type User
} from 'firebase/auth'

export const useAuthStore = defineStore('auth', () => {
    const user = ref<User | null>(null)
    const authInitialized = ref(false)
    const error = ref<string | null>(null)
    const isLoading = ref(false)

    const isAuthenticated = computed(() => !!user.value)

    // Initialize Auth Listener
    function initAuth() {
        return new Promise<void>((resolve) => {
            onAuthStateChanged(auth, (currentUser) => {
                user.value = currentUser
                authInitialized.value = true
                resolve()
            })
        })
    }

    async function loginWithGoogle() {
        isLoading.value = true
        error.value = null
        try {
            const provider = new GoogleAuthProvider()
            await signInWithPopup(auth, provider)
        } catch (e: any) {
            error.value = formatAuthError(e.code)
            throw e
        } finally {
            isLoading.value = false
        }
    }

    async function logout() {
        try {
            await signOut(auth)
            user.value = null
        } catch (e) {
            console.error('Logout failed', e)
        }
    }

    function formatAuthError(code: string): string {
        switch (code) {
            case 'auth/invalid-email': return 'Invalid email address.'
            case 'auth/user-disabled': return 'User account is disabled.'
            case 'auth/user-not-found': return 'No user found with this email.'
            case 'auth/wrong-password': return 'Incorrect password.'
            case 'auth/email-already-in-use': return 'Email is already registered.'
            case 'auth/weak-password': return 'Password should be at least 6 characters.'
            default: return 'An error occurred. Please try again.'
        }
    }

    return {
        user,
        isAuthenticated,
        authInitialized,
        error,
        isLoading,
        initAuth,
        loginWithGoogle,
        logout
    }
})
