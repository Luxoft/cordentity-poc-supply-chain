import React from 'react';
import {connect} from "react-redux";
import PropTypes from 'prop-types';
import {fetchPackages, manufactureAndShipRequest} from "../../../state/async";
import './ManufacturePage.scss';
import {ENTITY_MODIFIERS, PackageStatus, users} from "../../../utils";
import Spinner from 'react-spinkit';
import BgPNG from '../../../assets/img/back@3x.png';
import LuxoftLogoPNG from '../../../assets/img/lux-logo@3x.png';
import Table, {TableRowMF} from '../../common/Table/Table';
import {doNavigate} from '../../../index';
import classSet from 'react-classset';
import Header from '../../common/Header/Header';
import Portal from '../../common/Portal/Portal';
import Dimmer from '../../common/Dimmer/Dimmer';
import WaypointsModal from '../../common/WaypointsModal/WaypointsModal';
import Footer from '../../common/Footer/Footer';


class ManufacturePage extends React.Component {
    static propTypes = {
        packages: PropTypes.arrayOf(PropTypes.object),
        error: PropTypes.string,
        loading: PropTypes.bool,
        manufacturing: PropTypes.string
    };

    state = {
        backPressed: false,
        addRequestModalVisible: false,
        waypointsModalVisible: false,
        active: true,
        currentPackage: null
    };

    componentDidMount() {
        const {active, loading} = this.state;

        this.setState({backPressed: false});
        if (active && !loading) setTimeout(() => this.setState({active: false}), 100);

        document.getElementById('page-wrapper').style.overflow = 'auto';
        fetchPackages(ENTITY_MODIFIERS.MANUFACTURER);
    }

    componentDidUpdate() {
        const {active} = this.state;
        const {loading} = this.props;

        if (active && !loading) setTimeout(() => this.setState({active: false}), 100);
    }

    render() {
        const {error, loading, packages} = this.props;
        const {backPressed, active, waypointsModalVisible, currentPackage} = this.state;

        const user = users[ENTITY_MODIFIERS.MANUFACTURER];

        const classes = classSet({
            fade: true,
            active: backPressed || active,
            page: true,
            mf: true
        });

        const rowsNeedManufacturing = packages
            .filter(pack => pack.state === 'ISSUED')
            .map((pack, index) => <TableRowMF key={index} {...pack} button
                                              onBtnClick={this.handleManufactureAndSendClick(pack.serial)}/>);

        const otherRows = packages
            .filter(pack => pack.state !== 'ISSUED')
            .sort((a, b) => {
                if (PackageStatus[a.state] < PackageStatus[b.state]) return -1;
                if (PackageStatus[a.state] > PackageStatus[b.state]) return 1;
                return 0;
            })
            .map((pack, index) => <TableRowMF key={index} {...pack} onClick={this.handleWaypointsModalOpen(pack)} />);

        const headers = ['Medicine', 'Request ID', 'Treatment center', 'Patient', 'Status', 'Action'];

        return (
            <main className={classes} style={{backgroundImage: `url(${BgPNG})`}}>
                <Header header='Demo' subheader='Manufacturing: PharmaOne' onBackClick={this.handleBackClick}
                        user={user}/>
                <article>
                    <div className='columns'>
                        <div className='left'>
                            <Table className='mf' headers={headers}>
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
                        </div>
                        <div className="right">
                            <div className='description'>
                                <p><b>Manufacturers</b> digitalize production, so that is it clear when the drug is
                                    ready, packed and shipped.</p>
                                <p>
                                    Shared information includes delivery requirements, such as temperature and vibration
                                    levels, which can be synced with the smart Dewar containers automatically when
                                    needed.
                                </p>
                            </div>
                            <img src={LuxoftLogoPNG} data-rjs="3" alt="Luxoft"/>
                        </div>
                    </div>
                </article>
                <Footer/>
            </main>
        )
    }

    handleManufactureAndSendClick = serial => () => {
        manufactureAndShipRequest(serial)
            .then(() => setTimeout(() => fetchPackages(ENTITY_MODIFIERS.MANUFACTURER), 1000))
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
}

const mapStateToProps = state => {
    return {...state.packages}
};

export default connect(mapStateToProps)(ManufacturePage);