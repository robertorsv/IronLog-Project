import type { Exercise } from '../stores/exercises'

export const mockBodyParts = [
    'back', 'cardio', 'chest', 'lower arms', 'lower legs', 'neck', 'shoulders', 'upper arms', 'upper legs', 'waist'
]

export const mockEquipmentList = [
    'assisted', 'band', 'barbell', 'body weight', 'bosu ball', 'cable', 'dumbbell', 'elliptical machine',
    'ez barbell', 'kettlebell', 'leverage machine', 'medicine ball', 'olympic barbell', 'resistance band',
    'roller', 'rope', 'skierg machine', 'sled machine', 'smith machine', 'stability ball', 'stationary bike',
    'stepmill machine', 'tire', 'trap bar', 'upper body ergometer', 'weighted', 'wheel roller'
]

export const mockExercises: Exercise[] = [
    {
        id: '0001',
        name: '3/4 sit-up',
        bodyPart: 'waist',
        equipment: 'body weight',
        target: 'abs',
        gifUrl: 'https://v2.exercisedb.io/image/Hqa-iBq-81h8-5',
        instructions: [
            'Lie flat on your back with your knees bent and feet flat on the ground.',
            'Place your hands behind your head with your elbows pointing outwards.',
            'Engaging your abs, slowly lift your upper body off the ground, curling forward until your torso is at a 45-degree angle.',
            'Pause for a moment at the top, then slowly lower your upper body back down to the starting position.',
            'Repeat for the desired number of repetitions.'
        ],
        secondaryMuscles: ['hip flexors', 'lower back']
    },
    {
        id: '0002',
        name: '45° side bend',
        bodyPart: 'waist',
        equipment: 'body weight',
        target: 'abs',
        gifUrl: 'https://v2.exercisedb.io/image/TCP-2-2-2-2',
        instructions: [
            'Stand with your feet shoulder-width apart and your arms hanging down by your sides.',
            'Keeping your back straight and your core engaged, slowly bend your torso to one side, lowering your hand towards your knee.',
            'Pause for a moment at the bottom, then slowly return to the starting position.',
            'Repeat on the other side.',
            'Continue alternating sides for the desired number of repetitions.'
        ],
        secondaryMuscles: ['obliques']
    },
    {
        id: '0003',
        name: 'air bike',
        bodyPart: 'waist',
        equipment: 'body weight',
        target: 'abs',
        gifUrl: 'https://v2.exercisedb.io/image/3-3-3-3',
        instructions: [
            'Lie flat on your back with your hands behind your head and your legs lifted off the ground.',
            'Bring your right elbow towards your left knee while straightening your right leg.',
            'Return to the starting position and repeat on the other side, bringing your left elbow towards your right knee while straightening your left leg.',
            'Continue alternating sides in a pedaling motion for the desired number of repetitions.'
        ],
        secondaryMuscles: ['hip flexors']
    },
    {
        id: '0006',
        name: 'alternate heel touchers',
        bodyPart: 'waist',
        equipment: 'body weight',
        target: 'abs',
        gifUrl: 'https://v2.exercisedb.io/image/6-6-6-6',
        instructions: [
            'Lie flat on your back with your knees bent and feet flat on the ground.',
            'Extend your arms down by your sides.',
            'Engaging your abs, lift your shoulders slightly off the ground and reach your right hand towards your right heel.',
            'Return to the starting position and repeat on the left side, reaching your left hand towards your left heel.',
            'Continue alternating sides for the desired number of repetitions.'
        ],
        secondaryMuscles: ['obliques']
    },
    {
        id: '0009',
        name: 'assisted chest dip (kneeling)',
        bodyPart: 'chest',
        equipment: 'leverage machine',
        target: 'pectorals',
        gifUrl: 'https://v2.exercisedb.io/image/9-9-9-9',
        instructions: [
            'Adjust the machine to the desired weight and height.',
            'Kneel on the pad and grasp the handles with your palms facing down.',
            'Lower your body by bending your elbows until your upper arms are parallel to the floor.',
            'Pause for a moment, then push yourself back up to the starting position.',
            'Repeat for the desired number of repetitions.'
        ],
        secondaryMuscles: ['triceps', 'shoulders']
    },
    {
        id: '0023',
        name: 'barbell bench press',
        bodyPart: 'chest',
        equipment: 'barbell',
        target: 'pectorals',
        gifUrl: 'https://v2.exercisedb.io/image/23-23-23',
        instructions: [
            'Lie flat on a bench with your feet flat on the ground.',
            'Grasp the barbell with an overhand grip slightly wider than shoulder-width apart.',
            'Unrack the barbell and lower it slowly towards your chest.',
            'Pause for a moment, then push the barbell back up to the starting position.',
            'Repeat for the desired number of repetitions.'
        ],
        secondaryMuscles: ['triceps', 'shoulders']
    },
    {
        id: '0047',
        name: 'barbell curl',
        bodyPart: 'upper arms',
        equipment: 'barbell',
        target: 'biceps',
        gifUrl: 'https://v2.exercisedb.io/image/47-47-47',
        instructions: [
            'Stand with your feet shoulder-width apart and hold a barbell with an underhand grip.',
            'Keeping your elbows close to your sides, curl the barbell up towards your chest.',
            'Pause for a moment at the top, then slowly lower the barbell back down to the starting position.',
            'Repeat for the desired number of repetitions.'
        ],
        secondaryMuscles: ['forearms']
    },
    {
        id: '0092',
        name: 'cable rope triceps pushdown',
        bodyPart: 'upper arms',
        equipment: 'cable',
        target: 'triceps',
        gifUrl: 'https://v2.exercisedb.io/image/92-92-92',
        instructions: [
            'Attach a rope handle to a high pulley cable machine.',
            'Stand facing the machine with your feet shoulder-width apart.',
            'Grasp the rope handle with both hands, palms facing each other.',
            'Keeping your elbows close to your sides, push the rope down until your arms are fully extended.',
            'Pause for a moment, then slowly return to the starting position.',
            'Repeat for the desired number of repetitions.'
        ],
        secondaryMuscles: ['shoulders', 'forearms']
    },
    {
        id: '0290',
        name: 'dumbbell shoulder press',
        bodyPart: 'shoulders',
        equipment: 'dumbbell',
        target: 'delts',
        gifUrl: 'https://v2.exercisedb.io/image/290-290',
        instructions: [
            'Sit on a bench with back support and hold a dumbbell in each hand at shoulder height.',
            'Press the dumbbells up overhead until your arms are fully extended.',
            'Pause for a moment, then slowly lower the dumbbells back down to the starting position.',
            'Repeat for the desired number of repetitions.'
        ],
        secondaryMuscles: ['triceps', 'upper chest']
    },
    {
        id: '0381',
        name: 'ez barbell curl',
        bodyPart: 'upper arms',
        equipment: 'ez barbell',
        target: 'biceps',
        gifUrl: 'https://v2.exercisedb.io/image/381-381',
        instructions: [
            'Stand with your feet shoulder-width apart and hold an EZ curl bar with an underhand grip.',
            'Keeping your elbows close to your sides, curl the bar up towards your chest.',
            'Pause for a moment at the top, then slowly lower the bar back down to the starting position.',
            'Repeat for the desired number of repetitions.'
        ],
        secondaryMuscles: ['forearms']
    }
]
