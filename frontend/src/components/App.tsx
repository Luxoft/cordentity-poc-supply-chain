import {Component, h} from 'preact';
import LuxoftLogo from '/static/img/luxoft-03-logo-white@3x.png';
import SovrinLogo from '/static/img/sovrin-logo.png';
import CordaLogo from '/static/img/corda-logo.png';
import Match, {Link} from 'preact-router/match';
import styles from './App.scss';
import Router from 'preact-router';
import DemoHomePage from './pages/DemoHomePage';
import ArchitecturePage from './pages/ArchitecturePage';
import AboutPage from './pages/AboutPage';
import RequestPage from './pages/RequestPage';

export default class App extends Component {
    public render() {
        return (
            <main>
                <header>
                    <img src={LuxoftLogo} alt=""/>
                    <div>
                        <h1>
                            Blockchain-powered solution for
                            <br/>
                            Personalized medicine
                        </h1>
                        <Match path="/">
                            {({matches} : {matches: boolean}) => (
                                matches && <Link className={styles.button} activeClassName='' href='/request'>
                                    New patient request
                                </Link>
                            )}
                        </Match>
                    </div>
                </header>
                <nav>
                    <Link className={styles.link} activeClassName={styles.active} href='/'>
                        Demo
                    </Link>
                    <Link className={styles.link} activeClassName={styles.active} href='/architecture'>
                        Architecture
                    </Link>
                    <Link className={styles.link} activeClassName={styles.active} href='/about'>
                        About
                    </Link>
                </nav>
                <Router>
                    <DemoHomePage path='/'/>
                    <ArchitecturePage path='/architecture'/>
                    <AboutPage path='/about'/>
                    <RequestPage path='/request'/>
                </Router>
                <footer>
                    <img src={SovrinLogo} alt=""/>
                    <img className={styles.cordaLogo} src={CordaLogo} alt=""/>
                </footer>
            </main>
        )
    }
}
