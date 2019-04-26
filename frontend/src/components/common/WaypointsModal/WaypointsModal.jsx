import React from 'react';
import PropTypes from 'prop-types';
import CloseBtnPNG from '../../../assets/img/close-btn.svg';
import {formatDateTime, PackageStatus} from '../../../utils';
import './WaypointsModal.scss';
import classSet from 'react-classset';


const waypointsComments = {
    [PackageStatus.NEW]: () => 'Insurer has confirmed the coverage for the personalized therapy treatment costs',
    [PackageStatus.ISSUED]: () => 'Manufacturing request is created.',
    [PackageStatus.PROCESSED]: () => 'Medicine is produced.',
    [PackageStatus.DELIVERED]: () => 'Medicine is delivered.',
    [PackageStatus.QP_PASSED]: () => 'Medicine is ready for pick up.',
    [PackageStatus.COLLECTED]: () => 'Medicine is picked up'
};

export default class WaypointsModal extends React.Component {
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
    };

    state = {};

    renderWaypoint = (index, {at, by, text, notVisible = false}) => {
        const classes = classSet({
            waypoint: true,
            'not-visible': notVisible
        });

        let byVerbose;
        switch (by) {
            case 'TreatmentCenter':
                byVerbose = 'Marina Bay Hospital';
                break;

            case 'Manufacture':
                byVerbose = 'PharmaOne';
                break;

            default:
                byVerbose = by
        }

        return (
            <React.Fragment key={index}>
                <div className={classes}>
                    <div className="point"/>
                </div>
                <div className="description">
                    <div className="plate">
                        <div className="at">{formatDateTime(new Date(at))}</div>
                        <div className="text">{text}</div>
                        <div className="by">signed by: {byVerbose}</div>
                    </div>
                </div>
            </React.Fragment>
        )
    };

    render() {
        const {
            onClose, medicineName, serial, state, requestedAt, requestedBy, issuedAt, issuedBy, processedAt, processedBy,
            deliveredAt, deliveredTo, collectedAt, patientDid
        } = this.props;

        const waypoints = [];

        const statusNum = PackageStatus[state];

        if (statusNum > -1) waypoints.push({at: requestedAt, by: requestedBy.organisation, text: waypointsComments[0]()});
        if (statusNum > 0) waypoints.push({at: issuedAt, by: issuedBy.organisation, text: waypointsComments[1]()});
        if (statusNum > 1) waypoints.push({at: processedAt, by: processedBy.organisation, text: waypointsComments[2]()});
        if (statusNum > 2) waypoints.push({at: deliveredAt, by: deliveredTo.organisation, text: waypointsComments[3]()});
        if (statusNum > 3) waypoints.push({at: deliveredAt, by: deliveredTo.organisation, text: waypointsComments[4]()});
        if (statusNum > 4) waypoints.push({at: collectedAt, by: `did:sov:${patientDid}`, text: waypointsComments[5]()});
        waypoints[waypoints.length - 1].notVisible = true;

        const waypointsElems = waypoints.map((waypoint, index) => this.renderWaypoint(index, waypoint));

        return (
            <div className="waypoints-modal">
                <div className="header">
                    <h5>{medicineName} ({serial})</h5>
                    <img src={CloseBtnPNG} className='close-btn' onClick={onClose} alt="Close"/>
                </div>
                <div className="body">
                    <div className="waypoints">
                        {waypointsElems}
                    </div>
                </div>
            </div>
        );
    }
}