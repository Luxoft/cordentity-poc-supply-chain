import {h, Component} from 'preact';
import styles from './index.scss';
import SchemaImg from '/static/img/scheme1.png'

export default class ArchitecturePage extends Component {
    render() {
        return (
            <section className={styles.architecturePage}>
                <img src={SchemaImg} alt=""/>
            </section>
        )
    }
}
