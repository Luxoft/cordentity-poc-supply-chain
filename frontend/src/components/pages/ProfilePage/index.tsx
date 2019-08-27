import {Component, h} from 'preact';
import {route} from 'preact-router';
import Claim from '../../common/Claim';
import styles from './index.scss';
import {claims, IClaim, IInfo, parseClaims} from './utils';
import BackBtn from '/static/img/back-btn.svg';
import PatientAvatar from '/static/img/patient-avatar@3x.png';

interface IState {
    info: IInfo | null;
}

export default class ProfilePage extends Component<{}, IState> {
    public state = {
        info: null,
    };

    public componentDidMount(): void {
        this.getClaims()
            .then((value: IClaim[]) => this.setState({info: parseClaims(value)}))
            .catch((reason) => console.error(reason));
    }

    public render() {
        const {info}: IState = this.state;

        return (
            <section className={styles.profilePage}>
                <div className={styles.iconWrapper}>
                    <img onClick={this.handleOnBackBtnClick} src={BackBtn} alt=""/>
                </div>
                <div className={styles.dataWrapper}>
                    {
                        info
                            ? [
                                <div className={styles.header}>
                                    <img src={PatientAvatar} alt=''/>
                                    <div className={styles.avatarDescription}>
                                        <h3>{info!.name!.value}</h3>
                                        <p>verified by {info!.name!.verifiedBy}</p>
                                    </div>
                                </div>,
                                <div className={styles.content}>
                                    <div className={styles.entry}>
                                        <p className={styles.name}>Date of birth</p>
                                        <Claim
                                            value={info!.dateOfBirth!.value}
                                            verifiedBy={info!.dateOfBirth!.verifiedBy}
                                            text={`${info!.dateOfBirth!.years} years`}
                                        />
                                    </div>
                                    <div className={styles.entry}>
                                        <p className={styles.name}>Nationality</p>
                                        <Claim
                                            value={info!.nationality!.value}
                                            verifiedBy={info!.nationality!.verifiedBy}
                                        />
                                    </div>
                                    <div className={styles.entry}>
                                        <p className={styles.name}>Diagnosis</p>
                                        <div className={styles.twoClaims}>
                                            <Claim
                                                className={styles.claim}
                                                value={info!.diagnosis1!.name!}
                                                verifiedBy={info!.diagnosis1!.verifiedBy}
                                                text={info!.diagnosis1!.date}
                                            />
                                            <Claim
                                                className={styles.claim}
                                                value={info!.diagnosis2!.name!}
                                                verifiedBy={info!.diagnosis2!.verifiedBy}
                                                text={info!.diagnosis2!.date}
                                            />
                                        </div>
                                    </div>
                                    <div className={styles.entry}>
                                        <p className={styles.name}>Insurer</p>
                                        <Claim
                                            value={info!.insurer!.name!}
                                            verifiedBy={info!.insurer!.verifiedBy}
                                            text={`${info!.insurer!.period!.from} - ${info!.insurer!.period!.to}`}
                                        />
                                    </div>
                                    <div className={styles.entry}>
                                        <p className={styles.name}>Coverage Plan</p>
                                        <Claim
                                            value={`up to $${info!.coveragePlan!.amount!.toString()}`}
                                            verifiedBy={info!.coveragePlan!.verifiedBy}
                                            text={`${info!.coveragePlan!.period!.from} - ${info!.coveragePlan!.period!.to}`}
                                            annotation={`${info!.coveragePlan!.type} is covered`}
                                        />
                                    </div>
                                    <div className={styles.entry}>
                                        <p className={styles.name}>Prescription</p>
                                        <Claim
                                            value={info!.prescription!.name}
                                            verifiedBy={info!.prescription!.verifiedBy}
                                            buttonText='Produce'
                                        />
                                    </div>
                                    <div className={styles.entry}>
                                        <p className={styles.name}>Physician qualification</p>
                                        <Claim
                                            value={info!.qualification!.name}
                                            verifiedBy={info!.qualification!.verifiedBy}
                                        />
                                    </div>
                                </div>
                            ]
                            : <div/>
                    }
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
        );
    }

    private handleOnBackBtnClick = () => {
        route('/request');
    };

    private getClaims = () => Promise.resolve(claims);
}
