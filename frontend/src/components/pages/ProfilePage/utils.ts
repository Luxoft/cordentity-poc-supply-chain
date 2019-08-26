import moment from 'moment';

interface IInterval {
    from: string;
    to: string;
}

interface IClaimAttribute {
    name: string;
    value: string;
}

export interface IClaim {
    attributes: IClaimAttribute[];
    verifiedBy: string;
}

enum PersonalInformation {
    FullName = 'Full Name',
    DateOfBirth = 'Date of birth',
    Nationality = 'Nationality',
    Qualification = 'Physician qualification'
}

enum MedicalCondition {
    DiagnosisName1 = 'Diagnosis:1:Name',
    DiagnosisDate1 = 'Diagnosis:1:Date',
    DiagnosisName2 = 'Diagnosis:2:Name',
    DiagnosisDate2 = 'Diagnosis:2:Date',
    Prescription = 'Prescription'
}

enum InsurerInformation {
    InsurerName = 'Insurer:Name',
    InsurerDateFrom = 'Insurer:Date:From',
    InsurerDateTo = 'Insurer:Date:To'
}

enum InsuranceDetails {
    CoveragePlanAmount = 'Coverage Plan:Amount',
    CoveragePlanType = 'Coverage Plan:Type',
    CoveragePlanDateFrom = 'Coverage Plan:Date:From',
    CoveragePlanDateTo = 'Coverage Plan:Date:To'
}

enum Verifiers {
    OfficialAuthority = 'Official Authorities',
    Insurer = 'Insurer'
}

export const claims: IClaim[] = [
    {
        attributes: [
            {name: PersonalInformation.FullName, value: 'Mark Rubinshtein'},
            {name: PersonalInformation.DateOfBirth, value: '1565559987992'},
            {name: PersonalInformation.Nationality, value: 'jewish'},
            {name: PersonalInformation.Qualification, value: '25 years, MD'}
        ],
        verifiedBy: Verifiers.OfficialAuthority
    },
    {
        attributes: [
            {name: MedicalCondition.DiagnosisName1, value: 'Neuroblastoma III'},
            {name: MedicalCondition.DiagnosisDate1, value: '1565559987992'},
            {name: MedicalCondition.DiagnosisName2, value: 'Neuroblastoma IV'},
            {name: MedicalCondition.DiagnosisDate2, value: '1565559987992'}
        ],
        verifiedBy: Verifiers.Insurer
    },
    {
        attributes: [
            {name: InsurerInformation.InsurerName, value: 'Insurer Name'},
            {name: InsurerInformation.InsurerDateFrom, value: '1565559987992'},
            {name: InsurerInformation.InsurerDateTo, value: '1565559987992'},
        ],
        verifiedBy: Verifiers.Insurer
    },
    {
        attributes: [
            {name: InsuranceDetails.CoveragePlanAmount, value: '10000000'},
            {name: InsuranceDetails.CoveragePlanType, value: 'individual care'},
            {name: InsuranceDetails.CoveragePlanDateFrom, value: '1565559987992'},
            {name: InsuranceDetails.CoveragePlanDateTo, value: '1565559987992'},
        ],
        verifiedBy: Verifiers.Insurer
    },
    {
        attributes: [
            {name: MedicalCondition.Prescription, value: 'Santorium'}
        ],
        verifiedBy: Verifiers.Insurer
    }
];

interface IInfoEntry {
    verifiedBy: string;
}

interface INameEntry extends IInfoEntry {
    value: string;
}

interface IDateOfBirthEntry extends IInfoEntry {
    value: string;
    years: number;
}

interface INationalityEntry extends IInfoEntry {
    value: string;
}

interface IDiagnosisEntry extends IInfoEntry {
    name?: string;
    date?: string;
}

interface IInsurerEntry extends IInfoEntry {
    name?: string;
    period?: IInterval;
}

interface ICoveragePlanEntry extends IInfoEntry {
    amount?: number;
    type?: string;
    period?: IInterval;
}

interface IPrescriptionEntry extends IInfoEntry {
    name: string;
}

interface IQualificationEntry extends IInfoEntry {
    name: string;
}

export interface IInfo {
    name?: INameEntry,
    dateOfBirth?: IDateOfBirthEntry,
    nationality?: INationalityEntry,
    diagnosis1?: IDiagnosisEntry,
    diagnosis2?: IDiagnosisEntry,
    insurer?: IInsurerEntry,
    coveragePlan?: ICoveragePlanEntry,
    prescription?: IPrescriptionEntry,
    qualification?: IQualificationEntry
}

// tslint:disable-next-line:no-shadowed-variable
export function parseClaims(claims: IClaim[]): IInfo {
    const result: IInfo = {};

    claims.forEach((claim: IClaim) => {
        claim.attributes.forEach((attr: IClaimAttribute) => {
            switch (attr.name) {
                case PersonalInformation.FullName:
                    result.name = {
                        value: attr.value,
                        verifiedBy: claim.verifiedBy
                    };
                    return;

                case PersonalInformation.Qualification:
                    result.qualification = {
                        name: attr.value,
                        verifiedBy: claim.verifiedBy
                    };
                    return;

                case PersonalInformation.Nationality:
                    result.nationality = {
                        value: attr.value,
                        verifiedBy: claim.verifiedBy
                    };
                    return;

                case PersonalInformation.DateOfBirth:
                    const date = moment(parseInt(attr.value, 10));

                    result.dateOfBirth = {
                        value: date.format('Do MMM YYYY'),
                        years: parseInt(date.fromNow(true), 10),
                        verifiedBy: claim.verifiedBy
                    };
                    return;

                case MedicalCondition.DiagnosisName1:
                    if (!result.diagnosis1) {
                        result.diagnosis1 = {verifiedBy: claim.verifiedBy};
                    }
                    if (result.diagnosis1) {
                        result.diagnosis1.name = attr.value;
                    }
                    return;

                case MedicalCondition.DiagnosisDate1:
                    if (!result.diagnosis1) {
                        result.diagnosis1 = {verifiedBy: claim.verifiedBy};
                    }
                    if (result.diagnosis1) {
                        result.diagnosis1.date = moment(parseInt(attr.value, 10)).format('Do MMM YYYY');
                    }
                    return;

                case MedicalCondition.DiagnosisName2:
                    if (!result.diagnosis2) {
                        result.diagnosis2 = {verifiedBy: claim.verifiedBy};
                    }
                    if (result.diagnosis2) {
                        result.diagnosis2.name = attr.value;
                    }
                    return;

                case MedicalCondition.DiagnosisDate2:
                    if (!result.diagnosis2) {
                        result.diagnosis2 = {verifiedBy: claim.verifiedBy};
                    }
                    if (result.diagnosis2) {
                        result.diagnosis2.date = moment(parseInt(attr.value, 10)).format('Do MMM YYYY');
                    }
                    return;

                case MedicalCondition.Prescription:
                    result.prescription = {
                        name: attr.value,
                        verifiedBy: claim.verifiedBy
                    };
                    return;

                case InsurerInformation.InsurerName:
                    if (!result.insurer) {
                        result.insurer = {verifiedBy: claim.verifiedBy};
                    }
                    if (result.insurer) {
                        result.insurer.name = attr.value;
                    }
                    return;

                case InsurerInformation.InsurerDateFrom:
                    if (!result.insurer) {
                        result.insurer = {verifiedBy: claim.verifiedBy};
                    }
                    if (result.insurer) {
                        if (!result.insurer.period) {
                            result.insurer.period = {from: '', to: ''};
                        }
                        result.insurer.period.from = moment(parseInt(attr.value, 10)).format('Do MMM YYYY');
                    }
                    return;

                case InsurerInformation.InsurerDateTo:
                    if (!result.insurer) {
                        result.insurer = {verifiedBy: claim.verifiedBy};
                    }
                    if (result.insurer) {
                        if (!result.insurer.period) {
                            result.insurer.period = {from: '', to: ''};
                        }
                        result.insurer.period.to = moment(parseInt(attr.value, 10)).format('Do MMM YYYY');
                    }
                    return;

                case InsuranceDetails.CoveragePlanAmount:
                    if (!result.coveragePlan) {
                        result.coveragePlan = {verifiedBy: claim.verifiedBy};
                    }
                    if (result.coveragePlan) {
                        result.coveragePlan.amount = parseInt(attr.value, 10);
                    }
                    return;

                case InsuranceDetails.CoveragePlanType:
                    if (!result.coveragePlan) {
                        result.coveragePlan = {verifiedBy: claim.verifiedBy};
                    }
                    if (result.coveragePlan) {
                        result.coveragePlan.type = attr.value;
                    }
                    return;

                case InsuranceDetails.CoveragePlanDateFrom:
                    if (!result.coveragePlan) {
                        result.coveragePlan = {verifiedBy: claim.verifiedBy};
                    }
                    if (result.coveragePlan) {
                        if (!result.coveragePlan.period) {
                            result.coveragePlan.period = {from: '', to: ''};
                        }
                        result.coveragePlan.period.from = moment(parseInt(attr.value, 10)).format('Do MMM YYYY');
                    }
                    return;

                case InsuranceDetails.CoveragePlanDateTo:
                    if (!result.coveragePlan) {
                        result.coveragePlan = {verifiedBy: claim.verifiedBy};
                    }
                    if (result.coveragePlan) {
                        if (!result.coveragePlan.period) {
                            result.coveragePlan.period = {from: '', to: ''};
                        }
                        result.coveragePlan.period.to = moment(parseInt(attr.value, 10)).format('Do MMM YYYY');
                    }
                    return;
            }
        });
    });

    return result;
}
