import React from 'react';
import QRCode from '../../../assets/img/qr-code.png';
import BackBtn from '../../../assets/img/back-btn.svg';
import BgPNG from '../../../assets/img/back@3x.png';
import SchemePNG from '../../../assets/img/cvc@3x.png';
import Scheme1PNG from '../../../assets/img/scheme1.png';
import luxLogoPng from '../../../assets/img/luxoft-03-logo-white@3x.png';
import classSet from 'react-classset';
import './NewPatientRequestPage.scss';
import {doNavigate} from '../../../index';
import {ENTITY_MODIFIERS, users} from '../../../utils';
import Footer from '../../common/Footer/Footer';


export default class NewPatientRequestPage extends React.Component {
    state = {
        cardClicked: false,
        loaded: false,
        page: 1,
        pageChanged: false
    };

    componentDidMount() {
        document.getElementById('page-wrapper').style.overflow = 'hidden';
        setTimeout(() => this.setState({loaded: true}), 100)
    }

    render() {
        const classes = classSet({
            'fade-blow': true,
            active: this.state.cardClicked || !this.state.loaded,
            page: true,
            home: true
        });

        const {page, pageChanged} = this.state;

        const pa = users[ENTITY_MODIFIERS.PATIENT];
        const tc = users[ENTITY_MODIFIERS.TREATMENT_CENTER];
        const mf = users[ENTITY_MODIFIERS.MANUFACTURER];
        const cr = users[ENTITY_MODIFIERS.COURIER];
        const rm = users[ENTITY_MODIFIERS.RISK_MANAGER];

        const archClasses = classSet({
            architecture: true,
            fade: true,
            active: pageChanged,
            about: page === -1
        });

        const demoClasses = classSet({
            demo: true,
            fade: true,
            active: pageChanged
        });

        return (
            <main className={classes} style={{backgroundImage: `url(${BgPNG})`}}>
                <header>
                    <div className='title-wrapper'>
                        <div className='logo-wrapper'>
                            <img src={luxLogoPng} alt='Luxoft Logo' />
                        </div>
                        <div className='header-wrapper'>
                            <h1>Blockchain-powered solution for personalized medicine</h1>
                        </div>
                        <nav>
                            <ul>
                                <li>
                                    <a className={page === 1 ? 'active' : ''} onClick={this.handleToDemo}>Demo</a>
                                </li>
                                <li>
                                    <a className={page === 0 ? 'active' : ''} onClick={this.handleToArchitecture}>
                                        Architecture
                                    </a>
                                </li>
                                <li>
                                    <a className={page === -1 ? 'active' : ''} onClick={this.handleToAbout}>
                                        About
                                    </a>
                                </li>
                            </ul>
                        </nav>
                    </div>
                </header>
                <article>
                    <div className="content-wrapper">
                        {
                            page === 0
                                ? <div className={archClasses}>
                                    <img src={Scheme1PNG} data-rjs="3" alt="Architecture"/>
                                </div>
                                : page === 1
                                ? <div className={demoClasses}>
                                    <img onClick={this.handleToHomeClick} src={BackBtn} alt="" className="back-btn"/>
                                    <div className="scan-qr-area">
                                        <img src={QRCode} alt=""/>
                                    </div>
                                    <div className='description'>
                                        <p><b>New patient request</b></p>
                                        <p>
                                            Ask patient to scan this QR code in order to verify the identity and prescription.
                                        </p>
                                        <p>
                                            Once confirmed, the request will be sent to the assigned Manufacturer.
                                        </p>
                                    </div>
                                </div>
                                : <div className={archClasses}>
                                    <div className='text-wrapper'>
                                        <p>Trusted Identity backed by credentials</p>
                                        <p>Patient privacy is preserved</p>
                                        <p>Digital proof of every change of custody is tracked</p>
                                        <p>Quality certificates are attached to the package</p>
                                        <p>Embedded license and certification enforcement</p>
                                    </div>
                                    <img src={SchemePNG} data-rjs="3" alt="About"/>
                                </div>
                        }
                    </div>
                </article>
                <Footer/>
            </main>
        )
    }

    handleToArchitecture = () => {
        if (this.state.page !== 0) {
            this.setState(
                {pageChanged: true},
                () => setTimeout(() => this.setState(
                    {page: 0},
                    () => setTimeout(() => this.setState({pageChanged: false}), 100)
                ), 300)
            );
        }
    };

    handleToAbout = () => {
        if (this.state.page !== -1) {
            this.setState(
                {pageChanged: true},
                () => setTimeout(() => this.setState(
                    {page: -1},
                    () => setTimeout(() => this.setState({pageChanged: false}), 100)
                ), 300)
            );
        }
    };

    handleToDemo = () => {
        if (this.state.page !== 1) {
            this.setState(
                {pageChanged: true},
                () => setTimeout(() => this.setState(
                    {page: 1},
                    () => setTimeout(() => this.setState({pageChanged: false}), 100)
                ), 300)
            );
        }
    };

    handleToHomeClick = () => {
        setTimeout(() => {
            doNavigate('/');
        }, 300)
    };
}
