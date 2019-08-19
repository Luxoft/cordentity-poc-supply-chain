import React from 'react';
import LuxoftLogoPNG from '../../../assets/img/lux-logo@3x.png';
import SovrinLogoPNG from '../../../assets/img/sovrin-logo.png';
import CordaLogoPNG from '../../../assets/img/corda-logo.png';
import './Footer.scss';


export default class Footer extends React.Component {
    static propTypes = {};
    state = {};

    render() {
        return (
            <footer className='footer'>
                <div className='partners-logos'>
                    <img id='sovrin-logo' src={SovrinLogoPNG} alt="Sovrin"/>
                    <img src={CordaLogoPNG} alt="Corda"/>
                </div>
            </footer>
        );
    }
}
