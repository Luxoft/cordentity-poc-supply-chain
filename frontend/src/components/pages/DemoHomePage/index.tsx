import {h, Component} from 'preact';
import styles from './index.scss';
import FirstIcon from '/static/img/icon-first.png';
import SecondIcon from '/static/img/icon-second.png';
import ThirdIcon from '/static/img/icon-third.png';

export default class DemoHomePage extends Component {
    public render() {
        return (
            <section className={styles.demoHomePage}>
                <article>
                    <img src={FirstIcon} alt=""/>
                    <p>
                        <b>Patients</b> have all their identity verifications in the app, giving them ability to
                        identify themselves at <b>treatment centres</b> in order to request a personalized medicine for
                        their condition.
                        <div/>
                        They can track <b>delivery process</b> and <b>receive</b> notifications when the medicine is
                        ready.
                    </p>
                </article>
                <article>
                    <img src={SecondIcon} alt=""/>
                    <p>
                        <b>Treatment centres</b> are authorised to create medicine production requests to
                        the <b>Manufacturing companies</b>.
                        <div/>
                        After the request is made, it is possible
                        to <b>track the manufacturing</b> and <b>delivery process</b>.
                    </p>
                </article>
                <article>
                    <img src={ThirdIcon} alt=""/>
                    <p>
                        <b>Manufacturers</b> digitalize production, so that it is clear when the drug is ready, packed
                        and shipped.
                        <div/>
                        Shared information includes delivery requirements, such as temperature and vibration levels,
                        which can be synced with the smart Dewar containers automatically when needed.
                    </p>
                </article>
            </section>
        )
    }
}
