import {h, Component} from 'preact';
import styles from './index.scss';
import BackBtn from '/static/img/back-btn.svg'
import QrCode from '/static/img/qr-code.png'
import {route} from 'preact-router';

export default class RequestPage extends Component {
    render() {
        return (
            <section className={styles.requestPage}>
                <div className={styles.iconWrapper}>
                    <img onClick={this.handleOnBackBtnClick} src={BackBtn} alt=""/>
                </div>
                <div className={styles.qrWrapper}>
                    <img src={QrCode} alt=""/>
                </div>
                <div className={styles.textWrapper}>
                    <p><b>New patient request</b></p>
                    <p>
                        Ask patient to scan this QR code in order
                        <br/>
                        to verify the identity and prescription.
                    </p>
                    <p>
                        Once confirmed, the request will be sent
                        <br/>
                        to the assigned Manufacturer.
                    </p>
                </div>
            </section>
        )
    }

    handleOnBackBtnClick = () => {
        route('/');
    };
}
