import React from 'react'
import ReactDOM from 'react-dom'
import {Provider} from 'react-redux'
import {Route} from 'react-router'
import {ConnectedRouter, push, routerMiddleware, routerReducer} from 'react-router-redux'
import createHistory from 'history/createHashHistory'
import {applyMiddleware, combineReducers, createStore} from 'redux'
import reducers from './state/reducers'
import logger from 'redux-logger'
import HomePage from './components/pages/HomePage/HomePage';
import TreatmentCenterPage from './components/pages/TreatmentCenterPage/TreatmentCenterPage';
import PatientPage from './components/pages/PatientPage/PatientPage';
import ManufacturePage from './components/pages/ManufacturePage/ManufacturePage';
import NewPatientRequestPage from "./components/pages/NewPatientRequestPage/NewPatientRequestPage";


const history = createHistory();
const router = routerMiddleware(history);

export const store = createStore(
    combineReducers({
        ...reducers,
        router: routerReducer
    }),
    applyMiddleware(router, logger)
);

export const doAction = (type, payload) => store.dispatch({type, payload});
export const doNavigate = url => store.dispatch(push(url));

ReactDOM.render(
    <Provider store={store}>
        <ConnectedRouter history={history}>
            <div id='page-wrapper'>
                <Route exact path='/' component={HomePage}/>
                <Route path='/request' component={NewPatientRequestPage} />
            </div>
        </ConnectedRouter>
    </Provider>,
    document.getElementById('root')
);
