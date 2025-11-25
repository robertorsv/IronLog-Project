import 'dotenv/config'

const apiKey = process.env.VITE_RAPIDAPI_KEY || 'a247a14fcdmshfeb38f32ab1911bp1fffe3jsna69b9909097f';
const host = 'exercisedb.p.rapidapi.com';

async function checkDetailsAndEquipment() {
    try {
        // Check one exercise for instructions
        console.log('Checking exercise details...');
        const url1 = `https://${host}/exercises?limit=1&offset=0`;
        const res1 = await fetch(url1, {
            method: 'GET',
            headers: { 'x-rapidapi-key': apiKey, 'x-rapidapi-host': host }
        });
        const data1 = await res1.json();
        if (data1.length > 0) {
            console.log('Exercise keys:', Object.keys(data1[0]));
            console.log('Has instructions?', !!data1[0].instructions);
        }

        // Check equipment list
        console.log('Fetching equipment list...');
        const url2 = `https://${host}/exercises/equipmentList`;
        const res2 = await fetch(url2, {
            method: 'GET',
            headers: { 'x-rapidapi-key': apiKey, 'x-rapidapi-host': host }
        });
        const data2 = await res2.json();
        console.log('Equipment List:', data2);

    } catch (e) {
        console.log('Error:', e);
    }
}

checkDetailsAndEquipment();
