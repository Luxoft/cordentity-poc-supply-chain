import {Component, h} from 'preact';
import CVCImg from '/static/img/cvc@3x.png';
import styles from './index.scss';

export default class AboutPage extends Component {
    render() {
        return (
            <section className={styles.aboutPage}>
                <article>
                    <p>Trusted Identity backed by credentials</p>
                    <p>Patient privacy is preserved</p>
                    <p>Digital proof of every change of custody is tracked</p>
                    <p>
                        Quality certificates are
                        <br/>
                        attached to the package
                    </p>
                    <p>
                        Embedded license
                        <br/>
                        and certification
                        <br/>
                        enforcement
                    </p>
                </article>
                <img src={CVCImg} alt=""/>
            </section>
        )
    }
}
