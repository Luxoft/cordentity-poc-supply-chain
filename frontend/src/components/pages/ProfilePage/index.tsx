import {h, Component} from 'preact';
import styles from './index.scss';
import BackBtn from '/static/img/back-btn.svg'
import PatientAvatar from '/static/img/patient-avatar@3x.png'
import {route} from 'preact-router';

export default class ProfilePage extends Component {
    render() {
        return (
            <section className={styles.profilePage}>
                <div className={styles.iconWrapper}>
                    <img onClick={this.handleOnBackBtnClick} src={BackBtn} alt=""/>
                </div>
                <div className={styles.dataWrapper}>
                    <div className={styles.header}>
                        <img src={PatientAvatar} alt=""/>
                        <div className={styles.avatarDescription}>
                            <h3>Mark Rubinshtein</h3>
                            <p>verified by Official Authorities</p>
                        </div>
                    </div>
                </div>
                <div className={styles.textWrapper}>
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
        )
    }

    handleOnBackBtnClick = () => {
        route('/request');
    }
}
