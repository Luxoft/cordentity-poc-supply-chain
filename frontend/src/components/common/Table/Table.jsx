import React from 'react';
import PropTypes from 'prop-types';
import classSet from 'react-classset';
import {formatDateTime, generateRandomImg} from '../../../utils';
import './Table.scss';


const HeaderShape = {
    name: PropTypes.string,
    width: PropTypes.number
};

export default class Table extends React.Component {
    static propTypes = {
        headers: PropTypes.array,
        className: PropTypes.string
    };

    render() {
        const {children, headers, className} = this.props;
        const head = headers.map((header, index) => <div className="td" key={index}>{header}</div>);

        return (
            <div className={`table ${className}`}>
                <div className="thead">
                    <div className="td"/>
                    {head}
                </div>
                <div className="tbody">
                    {children}
                </div>
            </div>
        )
    }
}

export class TableRowTC extends React.Component {
    state = {
        img: null,
        blinking: false
    };

    componentDidMount() {
        this.setState({img: generateRandomImg()})
    }

    handleClick = () => {
        const {onBtnClick} = this.props;

        if (onBtnClick)
            this.setState({blinking: true}, onBtnClick);
    };

    render() {
        const {
            serial, state, patientDid, patientDiagnosis, medicineName, medicineDescription, processedBy,
            requestedAt, processedAt, deliveredAt, collectedAt, onClick, button
        } = this.props;

        const {img, blinking} = this.state;

        let filledPins = 0;
        let date = new Date();

        switch (state) {
            case 'COLLECTED':
                filledPins = 5;
                date = new Date(collectedAt);
                break;

            case 'DELIVERED':
                filledPins = 4;
                date = new Date(deliveredAt);
                break;

            case 'PROCESSED':
                filledPins = 3;
                date = new Date(deliveredAt);
                break;

            case 'ISSUED':
                filledPins = 2;
                date = new Date(processedAt);
                break;

            case 'NEW':
                filledPins = 1;
                date = new Date(requestedAt);
                break;
        }

        const classes = classSet({
            tr: true,
            blinking
        });

        return (
            <div className={classes} onClick={onClick}>
                <div className="td">
                    <TableImg {...{img}} />
                </div>
                <div className="td">
                    <TableManufacturerPlate {...{manufacturer: 'PharmaOne'}} />
                </div>
                <div className="td">
                    <TableMedicinePlate medicineName={medicineName} medicineDescription={medicineDescription}/>
                </div>
                <div className="td">
                    <TableSerialPlate {...{serial}} />
                </div>
                <div className="td">
                    <TablePatientPlate patientDid={`did:sov:${patientDid}`} patientDiagnosis={patientDiagnosis}/>
                </div>
                <div className="td">
                    <TableStatusPlate {...{date, filledPins}} />
                </div>
                <div className="td" onClick={this.handleClick}>
                    {button && <TableButton text='receive shipment'/>}
                </div>
            </div>
        )
    }
}

export class TableRowMF extends React.Component {
    state = {
        img: null
    };

    componentDidMount() {
        this.setState({img: generateRandomImg()})
    }

    handleClick = () => {
        const {onBtnClick} = this.props;

        if (onBtnClick)
            this.setState({blinking: true}, onBtnClick);
    };

    render() {
        const {
            serial, state, medicineName, medicineDescription, requestedBy, requestedAt,
            processedAt, deliveredAt, collectedAt, button, onClick, patientDid, patientDiagnosis
        } = this.props;

        const {blinking} = this.state;

        const img = generateRandomImg();

        let filledPins = 0;
        let date = new Date();

        switch (state) {
            case 'COLLECTED':
                filledPins = 5;
                date = new Date(collectedAt);
                break;

            case 'DELIVERED':
                filledPins = 4;
                date = new Date(deliveredAt);
                break;

            case 'PROCESSED':
                filledPins = 3;
                date = new Date(deliveredAt);
                break;

            case 'ISSUED':
                filledPins = 2;
                date = new Date(processedAt);
                break;

            case 'NEW':
                filledPins = 1;
                date = new Date(requestedAt);
                break;
        }

        const classes = classSet({
            tr: true,
            blinking
        });

        return (
            <div className={classes} onClick={onClick}>
                <div className="td">
                    <TableImg {...{img}} />
                </div>
                <div className="td">
                    <TableMedicinePlate medicineName={medicineName} medicineDescription={medicineDescription}/>
                </div>
                <div className="td">
                    <TableSerialPlate {...{serial}} />
                </div>
                <div className="td">
                    <TableTreatmentCenterPlate treatmentCenterName='Marina Bay Hosp ital' treatmentCenterAddress='Marina Sands 117, Singapore'/>
                </div>
                <div className="td">
                    <TablePatientPlate patientDid={`did:sov:${patientDid}`} patientDiagnosis={patientDiagnosis}/>
                </div>
                <div className="td">
                    <TableStatusPlate {...{date, filledPins}} />
                </div>
                <div className="td" onClick={this.handleClick}>
                    {button && <TableButton text='manufacture & ship'/>}
                </div>
            </div>
        )
    }
}

function TableButton(props) {
    return <div className="table-btn">
        <svg className='icon' xmlns="http://www.w3.org/2000/svg" width="7" height="12">
            <path d="M.86.146a.502.502 0 1 0-.713.71l5.635 5.141-5.635 5.147a.502.502 0 1 0 .713.71l5.996-5.471A.502.502 0 0 0 7 5.997a.498.498 0 0 0-.143-.38L.86.147z"/>
        </svg>
        <span className="text">{props.text}</span>
    </div>;
}

function TableImg(props) {
    return (
        <div className="img">
            <img src={props.img}/>
        </div>
    )
}

function TablePatientPlate(props) {
    const {patientDid, patientDiagnosis} = props;

    return (
        <div className='patient-plate'>
            <p className="did">{patientDid.substring(0, 16)}...</p>
            <p className="diagnosis">{patientDiagnosis}</p>
        </div>
    )
}

function TableTreatmentCenterPlate(props) {
    const {treatmentCenterName, treatmentCenterAddress} = props;

    return (
        <div className='treatment-center-plate'>
            <p className="name">{treatmentCenterName}</p>
            <p className="address">{treatmentCenterAddress}</p>
        </div>
    )
}

function TableMedicinePlate(props) {
    const {medicineName, medicineDescription} = props;

    return (
        <div className="medicine-type">
            <p className="name">{medicineName}</p>
            <p className="description">{medicineDescription}</p>
        </div>
    )
}

function TableManufacturerPlate(props) {
    const {manufacturer} = props;

    return (
        <div className="manufacturer">
            <p className="name">{manufacturer}</p>
        </div>
    )
}

function TableSerialPlate(props) {
    const {serial} = props;

    const serialParts = serial.split('-');

    return (
        <div className="serial">
            {serialParts[serialParts.length - 1]}
        </div>
    )
}

export const packageStatuses = [
    '',
    'Manufacture request created',
    'Processed and shipped',
    'Ready to pick up',
    'Picked up'
];

function TableStatusPlate(props) {
    const {date, filledPins} = props;

    return (
        <div className="status-plane">
            <ProgressBar filledPins={filledPins} date={formatDateTime(date)} maxPins={packageStatuses.length}/>
            <p className="info">{packageStatuses[filledPins - 1]}</p>
        </div>
    )
}

function ProgressBar(props) {
    const {maxPins, filledPins, date} = props;

    const pins = Array.from(Array(maxPins)).map((_, index) => {
        const classes = classSet({
            pin: true,
            active: index < filledPins
        });
        return <span key={index} className={classes}/>
    });

    const classes = classSet({
        qp: true,
        active: filledPins > 3
    });

    return (
        <div className="progress-bar">
            <div className="pins">
                {pins}
            </div>
            <div className="date">{date}</div>
        </div>
    )
}