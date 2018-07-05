import React from 'react';
import './PatientPage.scss';
import BgPNG from '../../../assets/img/back@3x.png';
import PatientPhonePNG from '../../../assets/img/patient-phone.png';
import {doNavigate} from '../../../index';
import classSet from 'react-classset';
import Header from '../../common/Header/Header';
import {ENTITY_MODIFIERS, users} from '../../../utils';
import Footer from '../../common/Footer/Footer';


export default class PatientPage extends React.Component {
    state = {
        backPressed: false,
        active: true,
    };

    componentDidMount() {
        const {backPressed, active} = this.state;

        if (backPressed) this.setState({backPressed: false});
        if (active) setTimeout(() => this.setState({active: false}), 100);

        document.getElementById('page-wrapper').style.overflow = 'auto';
    }

    render() {
        const {backPressed, active} = this.state;

        const classes = classSet({
            fade: true,
            active: backPressed || active,
            page: true,
            pa: true
        });

        const user = users[ENTITY_MODIFIERS.PATIENT];

        return (
            <main className={classes} style={{backgroundImage: `url(${BgPNG})`}}>
                <Header header='Demo' subheader='Patient' onBackClick={this.handleBackClick} user={user}/>
                <article>
                    <Footer/>
                    <img className='patients-app' src={PatientPhonePNG} alt="Patient's app"/>
                    <div className="info">
                        <div className="description">
                            <p>
                                <b>Patients</b> have all their identity verifications in the app, giving them an ability to
                                identify themselves at <b>treatment centres</b> in order to request a personalized medicine
                                for their condition.
                            </p>
                            <p>
                                They can track <b>the delivery process</b> and receive <b>notifications</b> when the
                                medicine is ready.
                            </p>
                        </div>
                    </div>
                </article>
            </main>
        )
    }

    handleBackClick = () => {
        this.setState({backPressed: true});
        setTimeout(() => doNavigate('/'), 300);
    }
}
