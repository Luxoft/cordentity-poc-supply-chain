import React from 'react';
import {connect} from "react-redux";
import PropTypes from 'prop-types';
import {fetchPackages, receiveShipment, fetchProofs} from "../../../state/async";
import './TreatmentCenterPage.scss';
import {ENTITY_MODIFIERS, PackageStatus, users} from "../../../utils";
import Spinner from 'react-spinkit';
import BgPNG from '../../../assets/img/back@3x.png';
import LuxoftLogoPNG from '../../../assets/img/lux-logo@3x.png';
import Table, {TableRowTC} from '../../common/Table/Table';
import PlusBtnPNG from '../../../assets/img/plus-btn.svg';
import LetterBtnPNG from '../../../assets/img/letter-btn.svg';
import {doNavigate} from '../../../index';
import classSet from 'react-classset';
import Header from '../../common/Header/Header';
import Portal from '../../common/Portal/Portal';
import Dimmer from '../../common/Dimmer/Dimmer';
import AddRequestModal from '../../common/AddRequestModal/AddRequestModal';
import CollectPackageModal from '../../common/CollectPackageModal/CollectPackageModal';
import ProfilePage from '../ProfilePage/ProfilePage'
import WaypointsModal from '../../common/WaypointsModal/WaypointsModal';
import Footer from '../../common/Footer/Footer';


class TreatmentCenterPage extends React.Component {
    static propTypes = {
        packages: PropTypes.arrayOf(PropTypes.object),
        proofs: PropTypes.arrayOf(PropTypes.object),
        error: PropTypes.string,
        loading: PropTypes.bool,
        invite: PropTypes.string
    };

    state = {
        backPressed: false,
        addRequestModalVisible: false,
        collectPackageModalVisible: false,
        patientProfileVisible: false,
        waypointsModalVisible: false,
        active: true,
        currentPackage: null,
        profileInfo: null
    };

    componentDidMount() {
        const {active, loading} = this.state;

        this.setState({backPressed: false});
        if (active && !loading) setTimeout(() => this.setState({active: false}), 100);

        document.getElementById('page-wrapper').style.overflow = 'auto';
        fetchPackages(ENTITY_MODIFIERS.TREATMENT_CENTER);
    }

    componentDidUpdate() {
        const {active} = this.state;
        const {loading} = this.props;

        if (active && !loading) setTimeout(() => this.setState({active: false}), 100);
    }

    render() {
        const {error, loading, packages, proofs, invite} = this.props;
        const {addRequestModalVisible, collectPackageModalVisible, patientProfileVisible, waypointsModalVisible, backPressed, active, currentPackage, profileInfo} = this.state;

        const user = users[ENTITY_MODIFIERS.TREATMENT_CENTER];

        const classes = classSet({
            fade: true,
            active: backPressed || active,
            page: true,
            tc: true
        });

        const rowsNeedManufacturing = packages
            .filter(pack => pack.state === 'PROCESSED')
            .map((pack, index) => <TableRowTC key={index} {...pack} button
                                              onBtnClick={this.handleReceiveShipmentClick(pack.serial)}/>);

        const otherRows = packages
            .filter(pack => pack.state !== 'PROCESSED')
            .sort((a, b) => {
                if (PackageStatus[a.state] < PackageStatus[b.state]) return -1;
                if (PackageStatus[a.state] > PackageStatus[b.state]) return 1;
                return 0;
            })
            .map((pack, index) => <TableRowTC key={index} {...pack} onClick={this.handleDisplayPackProfileModalOpen(pack)} />);

        const headers = ['Medicine', 'Manufacturer', 'Request ID', 'Patient', 'Status', 'Action'];

        return (
            <main className={classes} style={{backgroundImage: `url(${BgPNG})`}}>
                <Header header='Demo' subheader='Treatment centre: Marina Bay Hospital' onBackClick={this.handleBackClick} user={user}/>
                <article>
                    <div className="controls">
                        <button onClick={this.handleAddRequestModalOpen} className='add-request-btn'>
                            <span className="img" style={{backgroundImage: `url(${PlusBtnPNG})`}}/> Add request
                        </button>
                        <button onClick={this.handleCollectPackageModalOpen} className='distribute-package-btn'>
                            <span className="img" style={{backgroundImage: `url(${LetterBtnPNG})`}}/> Package collection
                        </button>

                        {/*OMG THAT'S THE PORTAL*/}
                        {
                            addRequestModalVisible &&
                            <Portal>
                                <Dimmer>
                                    <AddRequestModal invite={invite} onClose={this.handleAddRequestModalClose}/>
                                </Dimmer>
                            </Portal>
                        }
                        {
                            collectPackageModalVisible &&
                            <Portal>
                                <Dimmer>
                                    <CollectPackageModal invite={invite} onClose={this.handleCollectPackageModalClose}/>
                                </Dimmer>
                            </Portal>
                        }
                    </div>
                    <div className='columns'>
                        <div className='left'>
                            <Table className='tc' headers={headers}>
                                {
                                    loading
                                        ? <div className='spinner-wrapper'>
                                            <Spinner name="ball-scale-ripple" color="#3943C7" noFadeIn/>
                                        </div>
                                        : <React.Fragment>
                                            {rowsNeedManufacturing}
                                            {rowsNeedManufacturing.length > 0 && otherRows.length > 0 && <div className="br"/>}
                                            {otherRows}
                                        </React.Fragment>
                                }
                            </Table>
                            {
                                waypointsModalVisible &&
                                <Portal>
                                    <Dimmer>
                                        <WaypointsModal onClose={this.handleWaypointsModalClose} {...currentPackage}/>
                                    </Dimmer>
                                </Portal>
                            }
                            {
                                patientProfileVisible &&
                                <Portal>
                                    <Dimmer>
                                        <ProfilePage profileInfo={profileInfo} onClose={this.handleDisplayProfileModalClose} {...currentPackage}/>
                                    </Dimmer>
                                </Portal>
                            }
                        </div>
                        <div className="right">
                            <div className='description'>
                                <p><b>Treatment centers</b> are authorized to make medicine production requests to <b>Manufacturing
                                    companies</b>.</p>
                                <p>After request is made, it is possible to <b>track the manufacturing</b> and <b>delivery
                                    process</b>.</p>
                            </div>
                        </div>
                    </div>
                </article>
                <Footer />
            </main>
        )
    }

    handleAddRequestModalOpen = () => {
        this.setState({addRequestModalVisible: true})
    };

    handleAddRequestModalClose = () => {
        this.setState({addRequestModalVisible: false})
    };

    handleCollectPackageModalOpen = () => {
        this.setState({collectPackageModalVisible: true})
    };

    handleDisplayProfileModalOpen = () => {
        this.setState({patientProfileVisible: true})
    };

    handleDisplayProfileModalClose = () => {
        this.setState({
            patientProfileVisible: false,
            currentPackage: null
        })
    };

    handleCollectPackageModalClose = () => {
        this.setState({collectPackageModalVisible: false})
    };

    handleBackClick = () => {
        this.setState({backPressed: true});
        setTimeout(() => doNavigate('/'), 300);
    };

    handleWaypointsModalOpen = pack => () => {
        this.setState({
            waypointsModalVisible: true,
            currentPackage: pack
        })
    };

    handleWaypointsModalClose = () => {
        this.setState({
            waypointsModalVisible: false,
            currentPackage: null
        })
    };

    handleDisplayPackProfileModalOpen = pack => () => {
        this.setState({currentPackage: pack})
        fetchProofs(pack.serial)
            .then(() => setTimeout(() => {
                const {proofs} = this.props;
                const profileInfo = proofs
                    .filter(proof => "profile picture" in proof["requestedProof"]["revealedAttrs"])
                    .map(proof => proof["requestedProof"]["revealedAttrs"])
                    [0]
                this.setState({ profileInfo: profileInfo })
                this.setState({ patientProfileVisible: true })
            }, 500))
    };


    handleReceiveShipmentClick = serial => () => {
        receiveShipment(serial)
            .then(() => setTimeout(() => fetchPackages(ENTITY_MODIFIERS.TREATMENT_CENTER), 1000))
    };
}

const mapStateToProps = state => {
    return {...state.packages, ...state.invite, ...state.proofs}
};

export default connect(mapStateToProps)(TreatmentCenterPage);
