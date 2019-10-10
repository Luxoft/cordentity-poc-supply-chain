import React from 'react';

import Claim from '../../common/Claim/Claim';
import styles from './ProfilePage.scss';
import {claims, IClaim, IInfo, parseClaims} from './utils';
import BackBtn from '../../../assets/img/back-btn.svg';
import PatientAvatar from '../../../assets/img/patient-avatar@3x.png';
import PropTypes from 'prop-types';

interface IState {
    info: IInfo | null;
}

export default class ProfilePage extends React.Component {

    static propTypes = {
        onClose: PropTypes.func.isRequired
    };

    state = {
        info: null,
    };

    componentDidMount() {
        this.getClaims()
            .then((value: IClaim[]) => this.setState({info: parseClaims(value)}))
            .catch((reason) => console.error(reason));
    }

    render() {
        const {onClose} = this.props;
        const {info}: IState = this.state;

        return (
            <section className='profile-page'>
                <div className='icon-wrapper'>
                    <img onClick={onClose} src={BackBtn} alt=""/>
                </div>
                <div className='data-wrapper'>
                    {
                        info
                            ? [
                                <div className='header'>
                                    <img src={PatientAvatar} alt=''/>
                                    <div className='avatar-description'>
                                        <h3>{info.name.value}</h3>
                                        <p>verified by {info.name.verifiedBy}</p>
                                    </div>
                                </div>,
                                <div className='content'>
                                    <div className='entry'>
                                        <p className='name'>Date of birth</p>
                                        <Claim
                                            value={info.dateOfBirth.value}
                                            verifiedBy={info.dateOfBirth.verifiedBy}
                                            text={`${info.dateOfBirth.years} years`}
                                        />
                                    </div>
                                    <div className='entry'>
                                        <p className='name'>Nationality</p>
                                        <Claim
                                            value={info.nationality.value}
                                            verifiedBy={info.nationality.verifiedBy}
                                        />
                                    </div>
                                    <div className='entry'>
                                        <p className='name'>Diagnosis</p>
                                        <div className='two-claims'>
                                            <Claim
                                                className='claim'
                                                value={info.diagnosis1.name}
                                                verifiedBy={info.diagnosis1.verifiedBy}
                                                text={info.diagnosis1.date}
                                            />
                                            <Claim
                                                className='claim'
                                                value={info.diagnosis2.name}
                                                verifiedBy={info.diagnosis2.verifiedBy}
                                                text={info.diagnosis2.date}
                                            />
                                        </div>
                                    </div>
                                    <div className='entry'>
                                        <p className='name'>Insurer</p>
                                        <Claim
                                            value={info.insurer.name}
                                            verifiedBy={info.insurer.verifiedBy}
                                            text={`${info.insurer.period.from} - ${info.insurer.period.to}`}
                                        />
                                    </div>
                                    <div className='entry'>
                                        <p className='name'>Coverage Plan</p>
                                        <Claim
                                            value={`up to $${info.coveragePlan.amount.toString()}`}
                                            verifiedBy={info.coveragePlan.verifiedBy}
                                            text={`${info.coveragePlan.period.from} - ${info.coveragePlan.period.to}`}
                                            annotation={`${info.coveragePlan.type} is covered`}
                                        />
                                    </div>
                                    <div className='entry'>
                                        <p className='name'>Prescription</p>
                                        <Claim
                                            value={info.prescription.name}
                                            verifiedBy={info.prescription.verifiedBy}
                                            buttonText='Produce'
                                        />
                                    </div>
                                    <div className='entry'>
                                        <p className='name'>Physician qualification</p>
                                        <Claim
                                            value={info.qualification.name}
                                            verifiedBy={info.qualification.verifiedBy}
                                        />
                                    </div>
                                </div>
                            ]
                            : <div/>
                    }
                </div>
                <div className='text-wrapper'>
                    <p><b>Patient profile</b></p>
                    <p>
                        Produce and handle the package to the Patient.
                    </p>
                    <p>
                        Patient will be able to request the list of quality
                        <br/>
                        certificates by scanning QR code on the package
                    </p>
                </div>
            </section>
        );
    }

    getClaims = () => Promise.resolve(claims);
}
