import 'dotenv/config'

const apiKey = process.env.VITE_RAPIDAPI_KEY || '4e0d7d2894msh3937f4d160a4429p112a03jsn696c1a82a08a';
const host = 'exercisedb.p.rapidapi.com';

async function debugImages() {
    try {
        // 1. Fetch one exercise to check keys
        console.log('Fetching one exercise...');
        const url1 = `https://${host}/exercises?limit=1&offset=0`;
        const res1 = await fetch(url1, {
            method: 'GET',
            headers: { 'x-rapidapi-key': apiKey, 'x-rapidapi-host': host }
        });
        const data1 = await res1.json();

        if (data1.length > 0) {
            const ex = data1[0];
            console.log('Exercise ID:', ex.id);
            console.log('Exercise Name:', ex.name);
            console.log('Has gifUrl?', !!ex.gifUrl);
            console.log('gifUrl value:', ex.gifUrl);

            // 2. Try to fetch image from /image endpoint
            console.log('Testing /image endpoint...');
            const imgUrl = `https://${host}/image?exerciseId=${ex.id}&resolution=180`; // resolution might be optional or specific
            // Note: RapidAPI docs say /exercises/exercise/{id} might give gifUrl.
            // But let's test the endpoint I'm using.
            // Actually, looking at recent docs, there might not be an /image endpoint on the root?
            // Or maybe it is https://exercisedb.p.rapidapi.com/exercises/exercise/{id} that returns the object with gifUrl?

            const res2 = await fetch(imgUrl, {
                method: 'GET',
                headers: { 'x-rapidapi-key': apiKey, 'x-rapidapi-host': host }
            });

            console.log('/image status:', res2.status);
            if (!res2.ok) {
                console.log('Image fetch failed text:', await res2.text());
            } else {
                console.log('Image fetch success, content-type:', res2.headers.get('content-type'));
            }
        }

    } catch (e) {
        console.log('Error:', e);
    }
}

debugImages();
