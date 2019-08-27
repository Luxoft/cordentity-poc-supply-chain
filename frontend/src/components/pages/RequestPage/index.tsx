import {h, Component} from 'preact';
import {getInvite} from '../../../api';
import styles from './index.scss';
import BackBtn from '/static/img/back-btn.svg'
import {route} from 'preact-router';
// @ts-ignore
import QRCode from 'qrcode.react';

interface IState {
    qrContent: string;
}

export default class RequestPage extends Component<{}, IState> {
    public state = {
        qrContent: 'error'
    };

    public componentDidMount(): void {
        this.getInvite()
            .then((value: string) => this.setState({qrContent: value}))
            .catch((reason) => console.error(reason));
    }

    public render() {
        const {qrContent} = this.state;

        return (
            <section className={styles.requestPage}>
                <div className={styles.iconWrapper}>
                    <img onClick={this.handleOnBackBtnClick} src={BackBtn} alt=""/>
                </div>
                <div className={styles.qrWrapper}>
                    <QRCode value={qrContent} size={336} level='L'/>
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

    private getInvite = async (): Promise<string> => {
        const invite = await getInvite();
        console.log(invite);
        return JSON.stringify(invite);
    };

    private handleOnBackBtnClick = () => {
        route('/');
    };
}
