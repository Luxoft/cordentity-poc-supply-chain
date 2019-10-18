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

    didBySchemaName = role => () => {
        const {identifiers} = this.props;
        identifiers.filter(id => id["schema_id"].includes({`:${role}:`}))
    }

    render() {
        const {info}: IState = this.state;

        const {
            onClose, medicineName, serial, state, requestedAt, requestedBy, issuedAt, issuedBy, processedAt, processedBy,
            deliveredAt, deliveredTo, collectedAt, patientDid, profileInfo
        } = this.props;

        return (
            <section className='profile-page'>
                <div className='data-wrapper'>
                    {
                        info
                            ? [
                                <div className='header'>
                                    <div className="social-id" style={{display: 'flex'}}>
                                        <Claim
                                            value={profileInfo["medical id"]['raw']}
                                        />
                                        <div>
                                            <img src={CloseBtnPNG} className='close-btn' onClick={onClose} alt="Close"/>
                                        </div>
                                    </div>
                                    <div className='avatar-and-close' style={{display: 'flex'}}>
                                        <img className='avatar' src={`data:image/png;base64,${profileInfo["profile picture"]['raw']}`} alt=''/>
                                        <div className='avatar-description'>
                                            <h3>{profileInfo["name"]['raw']}</h3>
                                            <p>verified by {info.name.verifiedBy}</p>
                                        </div>
                                    </div>
                                </div>,
                                <div className='content'>
                                    <div className='entry'>
                                        <p className='name'>Client name</p>
                                        <Claim
                                            value={profileInfo["name"]['raw']}
                                        />
                                    </div>
                                    <div className='entry'>
                                        <p className='name'>Medical ID</p>
                                        <Claim
                                            value={profileInfo["medical id"]['raw']}
                                        />
                                    </div>
                                    <div className='entry'>
                                        <p className='name'>Medical Condition</p>
                                        <Claim
                                            value={profileInfo["medical condition"]['raw']}
                                        />
                                    </div>
                                    <div className='entry'>
                                        <p className='name'>Gender</p>
                                        <Claim
                                            value={profileInfo["sex"]['raw']}
                                        />
                                    </div>
                                    <h5>Order details</h5>
                                    { medicineName && <div className='entry'>
                                        <p className='name'>Prescription</p>
                                        <Claim
                                            value={medicineName}
                                        />
                                        </div>
                                    }
                                    { serial && <div className='entry'>
                                        <p className='name'>Order Serial No.</p>
                                        <Claim
                                            value={serial}
                                        />
                                        </div>
                                    }
                                    { state && <div className='entry'>
                                        <p className='name'>Package State</p>
                                        <Claim
                                            value={state}
                                        />
                                        </div>
                                    }
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
