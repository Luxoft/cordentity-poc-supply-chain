import React from 'react';
import Card from '../../common/Card/Card';
import BgPNG from '../../../assets/img/back@3x.png';
import SchemePNG from '../../../assets/img/scheme@3x.png';
import Scheme1PNG from '../../../assets/img/scheme1.png';
import luxLogoPng from '../../../assets/img/luxoft-03-logo-white@3x.png';
import classSet from 'react-classset';
import './HomePage.scss';
import {doNavigate} from '../../../index';
import {ENTITY_MODIFIERS, users} from '../../../utils';
import Footer from '../../common/Footer/Footer';


export default class HomePage extends React.Component {
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
            active: pageChanged
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
                        <h1>Blockchain-powered solution for personalized medicine</h1>
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
                    <div className='logo-wrapper'>
                        <img src={luxLogoPng} alt='Luxoft Logo' />
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
                                    <Card onClick={this.handleCardClick(pa.url)} header={pa.description} imgSrc={pa.avatar}
                                          imgTitle={pa.name}/>
                                    <Card onClick={this.handleCardClick(tc.url)} header={tc.description} imgSrc={tc.avatar}
                                          imgTitle={tc.name}/>
                                    <Card onClick={this.handleCardClick(mf.url)} header={mf.description} imgSrc={mf.avatar}
                                          imgTitle={mf.name}/>
                                    <Card disabled header={cr.description} imgSrc={cr.avatar} imgTitle={cr.name}/>
                                    <Card disabled header={rm.description} imgSrc={rm.avatar} imgTitle={rm.name}/>
                                </div>
                                : <div className={archClasses}>
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

    handleCardClick = target => () => {
        this.setState({
            cardClicked: true
        });
        setTimeout(() => {
            doNavigate(target);
        }, 300)
    }
}
