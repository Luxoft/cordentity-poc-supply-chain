import {h, Component} from 'preact';
import styles from './index.scss';
import BackBtn from '/static/img/back-btn.svg'
import {route} from 'preact-router';
// @ts-ignore
import QRCode from 'qrcode.react';

interface IState {
    qrContent: string;
}

export default class RequestPage extends Component<{}, IState> {
    state = {
        qrContent: 'error'
    };

    public componentDidMount(): void {
        this.getInvite()
            .then(value => this.setState({qrContent: value}))
            .catch(reason => console.error(reason));
    }

    render() {
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

    getInvite = () => Promise.resolve('erroraaaaaaaaadasdasdasdasdasdaerroraaaaaaaaadasdasdasdasdasdaerroraaaaaaaaadasdasdasdasdasdaerroraaaaaaaaadasdasdasdasdasdaerroraaaaaaaaadasdasdasdasdasdaerroraaaaaaaaadasdasdasdasdasdaerroraaaaaaaaadasdasdasdasdasdaerroraaaaaaaaadasdasdasdasdasdaerroraaaaaaaaadasdasdasdasdasda');

    handleOnBackBtnClick = () => {
        route('/');
    };
}
