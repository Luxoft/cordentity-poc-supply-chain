import {v4} from 'uuid';
import PatientAvatar from './assets/img/patient-avatar@3x.png';
import TreatmentCenterAvatar from './assets/img/treatment-center-avatar@3x.png';
import ManufacturerAvatar from './assets/img/manufacturer-avatar@3x.png';
import CourierAvatar from './assets/img/courier-avatar@3x.png';
import RiskManagerAvatar from './assets/img/group-4@3x.png';
import Med1PNG from './assets/img/med1.png';


export function doRequest() {
    return new Promise((resolve, reject) => {
        const requestTime = Math.random() * 2000 + 1000;
        const success = true; //Math.random() > 0.3;

        setTimeout(() => success ? resolve() : reject(), requestTime)
    });
}

export const ENTITY_MODIFIERS = {
    TREATMENT_CENTER: 'TC',
    MANUFACTURER: 'MF',
    PATIENT: 'PA',
    COURIER: 'CR',
    RISK_MANAGER: 'RM'
};

export const users = {
    [ENTITY_MODIFIERS.PATIENT]: {
        name: 'Mark Rubinshtein',
        description: 'Patient',
        avatar: PatientAvatar,
        url: '/pa'
    },
    [ENTITY_MODIFIERS.TREATMENT_CENTER]: {
        name: 'Marla Brunier',
        description: 'Treatment centre operator',
        avatar: TreatmentCenterAvatar,
        url: '/tc',
    },
    [ENTITY_MODIFIERS.MANUFACTURER]: {
        name: 'Peter Tornkvist',
        description: 'Production coordinator',
        avatar: ManufacturerAvatar,
        url: '/mf',
    },
    [ENTITY_MODIFIERS.COURIER]: {
        name: 'Carl Hoffman',
        description: 'Courier',
        avatar: CourierAvatar,
        url: '/cr'
    },
    [ENTITY_MODIFIERS.RISK_MANAGER]: {
        name: 'Daniel Zimmerman',
        description: 'Risk manager',
        avatar: RiskManagerAvatar,
        url: '/rm'
    }
};

function randInt(min, max) {
    return ~~(Math.random() * (max - min + 1)) + min
}

function choiceArray(list) {
    return list[randInt(0, list.length - 1)]
}

const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

function addLeadingZero(number) {
    return number < 10 ? `0${number}` : `${number}`;
}

export const formatDate = date => `${months[date.getMonth()]} ${date.getDate()}, ${date.getFullYear()}`;
export const formatDateTime = date => `${months[date.getMonth()]} ${date.getDate()}, ${date.getFullYear()}, ${addLeadingZero(date.getHours())}:${addLeadingZero(date.getMinutes())}`;

function generateRandomDate() {
    return randInt(1517432400000, 1530392400000)
}

function generateRandomDid(length = 5) {
    const result = [];

    for (let i = 0; i < length; i++)
        result.push(randInt(0, 256))

    return `did:sov:${btoa(result).slice(0, -2)}`
}

function generateRandomDisease() {
    const diseases = ['neuroblastoma', 'diabetes (type 2)', 'rheumatoid arthrits', 'lymphoma', 'myeloma'];

    return choiceArray(diseases)
}

function generateRandomManufacturer() {
    const manufacturers = ['PharmaOne', 'NextGen', 'Medicare'];

    return choiceArray(manufacturers)
}

function generateRandomWaypoint(prefix) {
    return {
        [`${prefix}At`]: generateRandomDate(),
        [`${prefix}By`]: generateRandomDid()
    }
}

export function generateRandomImg() {
    //return choiceArray([Med1PNG, Med2PNG, Med3PNG, Med4PNG, Med5PNG, Med6PNG]);
    return Med1PNG
}

function generateRandomMedicine() {
    const names = ['Santorium', 'Glickeplus', 'Ximab', 'Hemta One', 'Temporalim', 'Teoxalin'];
    const types = ['T-cells mediator', 'Sulphonylureas', 'CB20 antibodies', 'Immune suppressor'];

    return {
        medicineName: choiceArray(names),
        medicineDescription: choiceArray(types)
    }
}

function generateRandomTreatmentCenter() {
    const tcs = [
        {
            treatmentCenterName: 'Santa Monica Hospital TC',
            treatmentCenterAddress: 'Zeehofstrasse 21, Zug'
        },
        {
            treatmentCenterName: 'MARINA BAY Hospital',
            treatmentCenterAddress: 'Marina Sands 117, Singapore'
        }
    ];

    return choiceArray(tcs);
}

function doesUniverseLoveMe() {
    return Math.random() > 0.5
}

function generateRandomPackage() {
    const serial = v4();
    const patient = {
        patientDid: generateRandomDid(),
        patientDiagnosis: generateRandomDisease()
    };
    const treatmentCenter = generateRandomTreatmentCenter();
    const medicine = generateRandomMedicine();
    const manufacturer = generateRandomManufacturer();
    const issued = generateRandomWaypoint('issued');
    const processed = generateRandomWaypoint('processed');
    const delivered = generateRandomWaypoint('delivered');
    const collected = generateRandomWaypoint('collected');
    const qp = doesUniverseLoveMe();

    const status = randInt(1, 5);

    switch (status) {
        case 1: return {serial, status, ...patient, ...medicine, ...treatmentCenter, manufacturer, ...issued, qp};
        case 2: return {serial, status, ...patient, ...medicine, ...treatmentCenter, manufacturer, ...issued, ...processed};
        case 3: return {serial, status, ...patient, ...medicine, ...treatmentCenter, manufacturer, ...issued, ...processed, ...delivered};
        case 4: return {serial, status, ...patient, ...medicine, ...treatmentCenter, manufacturer, ...issued, ...processed, ...delivered, qp};
        case 5: return {serial, status, ...patient, ...medicine, ...treatmentCenter, manufacturer, ...issued, ...processed, ...delivered, qp, ...collected};
    }
}

export function generateRandomPackages(count = randInt(5, 10)) {
    const result = [];
    for (let i = 0; i < count; i++) result.push(generateRandomPackage())

    return result;
}

export const PackageStatus = {
    NEW: 0,
    ISSUED: 1,
    PROCESSED: 2,
    DELIVERED: 3,
    QP_PASSED: 4,
    COLLECTED: 5
};
