import React from 'react';

import Claim from '../../common/Claim/Claim';
import styles from './ProfilePage.scss';
import {claims, IClaim, IInfo, parseClaims} from './utils';
import {formatDateTime, PackageStatus} from '../../../utils';
import BackBtn from '../../../assets/img/back-btn.svg';
import CloseBtnPNG from '../../../assets/img/close-btn.svg';
import PatientAvatar from '../../../assets/img/patient-avatar@3x.png';
import PropTypes from 'prop-types';

interface IState {
    info: IInfo | null;
}

export default class ProfilePage extends React.Component {

    static propTypes = {
        onClose: PropTypes.func.isRequired,
        serial: PropTypes.string.isRequired,
        state: PropTypes.string.isRequired,
        medicineName: PropTypes.string.isRequired,
        patientDid: PropTypes.string.isRequired,
        requestedAt: PropTypes.number.isRequired,
        requestedBy: PropTypes.object.isRequired,
        issuedAt: PropTypes.number.isRequired,
        issuedBy: PropTypes.object.isRequired,
        processedAt: PropTypes.number,
        processedBy: PropTypes.object,
        deliveredAt: PropTypes.number,
        deliveredTo: PropTypes.object,
        collectedAt: PropTypes.number,
        patientDiagnosis: PropTypes.string.isRequired,
        profileInfo: PropTypes.object.isRequired,
        identifiers: PropTypes.object.isRequired
    };

    state = {
        info: null,
    };

    componentDidMount() {
        this.getClaims()
            .then((value: IClaim[]) => this.setState({info: parseClaims(value)}))
            .catch((reason) => console.error(reason));
    }

    didBySchemaName(role) {
        const {identifiers} = this.props;
        return identifiers.filter(id => id.schemaIdObject.name == role);
    }

    render() {
        const {info}: IState = this.state;

        const {
            onClose, medicineName, serial, state, requestedAt, requestedBy, issuedAt, issuedBy, processedAt, processedBy,
            deliveredAt, deliveredTo, collectedAt, patientDid, profileInfo, patientDiagnosis
        } = this.props;

        const ids = this.didBySchemaName('Human');
        const humanVerifierDid = ids[0].credentialDefinitionIdObject.did;
        const humanSchemaId = ids[0].schema_id;

        const patientIds = this.didBySchemaName('Patient')
        const patientVerifierDid = patientIds[0].credentialDefinitionIdObject.did;
        const patientSchemaId = patientIds[0].schema_id;

        return (
            <section className='profile-page'>
                <div className='data-wrapper'>
                    {
                        info
                            ? [
                                <div className='header'>
                                    <div>
                                        <img src={CloseBtnPNG} className='close-btn' onClick={onClose} alt="Close"/>
                                    </div>
                                    <div className="social-id" style={{display: 'flex'}}>
                                        <Claim
                                            value={profileInfo["medical id"]['raw']}
                                        />
                                        <p>verified by: &lt;{humanVerifierDid}&gt;</p>
                                    </div>
                                    <div className='avatar-and-close' style={{display: 'flex'}}>
                                        <img className='avatar' src={`data:image/png;base64,${profileInfo["profile picture"]['raw']}`} alt=''/>
                                        <div className='avatar-description'>
                                            <h3>{profileInfo["name"]['raw']}</h3>
                                            <p>Age: 45</p>
                                            <p>Gender: {profileInfo["sex"]['raw']}</p>
                                        </div>
                                    </div>
                                </div>,
                                <div className='content'>
                                    <div className="cred-header" style={{display: 'flex'}}>
                                        <h5>Person</h5>
                                        <p>verified by: &lt;{humanVerifierDid}&gt;</p>
                                    </div>
                                    <div className='schema-id'>
                                        <p className='name'>Schema ID</p>
                                        <p className='value'>{humanSchemaId}</p>
                                    </div>
                                    <ul className="list">
                                        <li className="list-item">
                                            <div className='entry'>
                                                <p className='name'>Name</p>
                                                <p className='value'>{profileInfo["name"]['raw']}</p>
                                            </div>
                                        </li>
                                        <li className="list-item">
                                            <div className='entry'>
                                                <p className='name'>Age</p>
                                                <p className='value'>45</p>
                                            </div>
                                        </li>
                                        <li className="list-item">
                                            <div className='entry'>
                                                <p className='name'>Gender</p>
                                                <p className='value'>{profileInfo["sex"]['raw']}</p>
                                            </div>
                                        </li>
                                        <li className="list-item">
                                            <div className='entry'>
                                                <p className='name'>Social ID</p>
                                                <p className='value'>{profileInfo["medical id"]['raw']}</p>
                                            </div>
                                        </li>
                                    </ul>
                                    <div className="cred-header" style={{display: 'flex'}}>
                                        <h5>Insurer</h5>
                                        <p>verified by: &lt;{patientVerifierDid}&gt;</p>
                                    </div>
                                    <div className='schema-id'>
                                        <p className='name'>Schema ID</p>
                                        <p className='value'>{patientSchemaId}</p>
                                    </div>
                                    <ul className="list">
                                        <li className="list-item">
                                            <div className='entry'>
                                                <p className='name'>Medical ID</p>
                                                <p className='value'>{profileInfo["medical id"]['raw']}</p>
                                            </div>
                                        </li>
                                        <li className="list-item">
                                            <div className='entry'>
                                                <p className='name'>Insurer</p>
                                                <p className='value'>TK Krankenkasse</p>
                                            </div>
                                        </li>
                                        {/*
                                        <li className="list-item">
                                            <div className='entry'>
                                                <p className='name'>Coverage</p>
                                                <p className='value'>up to &lt;50000; $&gt; fully covered</p>
                                            </div>
                                        </li>
                                        */}
                                    </ul>
                                    <div className="cred-header" style={{display: 'flex'}}>
                                        <h5>Prescription</h5>
                                        <p>verified by: &lt;{patientVerifierDid}&gt;</p>
                                    </div>
                                    <div className='schema-id'>
                                        <p className='name'>Schema ID</p>
                                        <p className='value'>{patientSchemaId}</p>
                                    </div>
                                    <ul className="list">
                                        <li className="list-item">
                                            <div className='entry'>
                                                <p className='name'>Diagnosis</p>
                                                <p className='value'>{patientDiagnosis}</p>
                                            </div>
                                        </li>
                                        <li className="list-item">
                                            <div className='entry'>
                                                <p className='name'>Prescription</p>
                                                <p className='value'>{medicineName}</p>
                                            </div>
                                        </li>
                                    </ul>
                                    <div className="cred-header" style={{display: 'flex'}}>
                                        <h5>Order details</h5>
                                        <p></p>
                                    </div>
                                    <ul className="list">
                                        <li className="list-item">
                                            { serial && <div className='entry'>
                                                <p className='name'>Order Serial No.</p>
                                                <p className='value'>{serial}</p>
                                                </div>
                                            }
                                        </li>
                                        <li className="list-item">
                                            { state && <div className='entry'>
                                                <p className='name'>Package State</p>
                                                <p className='value'>{state}</p>
                                                </div>
                                            }
                                        </li>
                                    </ul>
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
