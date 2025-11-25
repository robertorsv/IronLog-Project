import 'dotenv/config'

const apiKey = process.env.VITE_RAPIDAPI_KEY || '4e0d7d2894msh3937f4d160a4429p112a03jsn696c1a82a08a';
const host = 'exercisedb.p.rapidapi.com';

async function checkEquipment() {
    try {
        // 1. Fetch equipment list
        console.log('Fetching equipment list...');
        const res = await fetch(`https://${host}/exercises/equipmentList`, {
            method: 'GET',
            headers: { 'x-rapidapi-key': apiKey, 'x-rapidapi-host': host }
        });

        if (!res.ok) {
            console.log('Equipment list fetch failed:', res.status);
            console.log(await res.text());
            return;
        }

        const equipmentList = await res.json();
        if (!Array.isArray(equipmentList)) {
            console.log('Equipment list is not an array:', equipmentList);
            return;
        }
        console.log('Equipment List Sample:', equipmentList.slice(0, 5));

        // 2. Pick 'ez barbell'
        const testItem = 'ez barbell';
        console.log(`Testing fetch for equipment: "${testItem}"`);

        const url = `https://${host}/exercises/equipment/${encodeURIComponent(testItem)}?limit=5`;
        console.log(`URL: ${url}`);

        const res2 = await fetch(url, {
            method: 'GET',
            headers: { 'x-rapidapi-key': apiKey, 'x-rapidapi-host': host }
        });

        if (!res2.ok) {
            console.log('Error status:', res2.status);
            console.log('Error text:', await res2.text());
        } else {
            const data = await res2.json();
            console.log(`Found ${data.length} exercises.`);
            if (data.length > 0) {
                console.log('First exercise equipment:', data[0].equipment);
            }
        }

    } catch (e) {
        console.log('Error:', e);
    }
}

checkEquipment();
