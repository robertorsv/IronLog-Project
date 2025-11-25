/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{vue,js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                background: '#121212',
                surface: '#1E1E1E',
                primary: '#E05D26', // Rust Orange
                secondary: '#F59E0B', // Molten Amber
                text: '#E0E0E0',
                muted: '#A0A0A0'
            },
            fontFamily: {
                sans: ['Inter', 'sans-serif'],
            }
        },
    },
    plugins: [],
}
