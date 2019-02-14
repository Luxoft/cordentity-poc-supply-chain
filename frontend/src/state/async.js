import {doAction} from "../index";
import {
    PACKAGE_DELIVERY,
    PACKAGE_DELIVERY_FAIL,
    PACKAGE_DELIVERY_SUCCESS,
    PACKAGE_LOAD,
    PACKAGE_LOAD_FAIL,
    PACKAGE_LOAD_SUCCESS,
    PACKAGE_MANUFACTURE,
    PACKAGE_MANUFACTURE_FAIL,
    PACKAGE_MANUFACTURE_SUCCESS
} from "./actions";
import * as qwest from 'qwest';

qwest.setDefaultOptions({
    dataType: 'json',
    responseType: 'json',
    headers: {
        'Cache-Control': 'no-cache'
    }
});

const envs = {
    dev: 'development',
    prod: 'production'
};

/* --- CURRENT ENVIRONMENT --- */
const CURRENT_ENV = envs.dev;

const host = {
    [envs.dev]: 'localhost',
    [envs.prod]: '18.196.100.2'
};

const manufactureUrls = {
    LIST: `http://${host[CURRENT_ENV]}:8081/api/mf/package/list`,
    PROCESS: `http://${host[CURRENT_ENV]}:8081/api/mf/request/process`,
};

const patientAgentUrls = {
    LIST: `http://${host[CURRENT_ENV]}:8082/api/tc/package/list`
};

const treatmentCenterUrls = {
    LIST: `http://${host[CURRENT_ENV]}:8082/api/tc/package/list`,
    RECEIVE: `http://${host[CURRENT_ENV]}:8082/api/tc/package/receive`
};

export function fetchPackages(from) {
    doAction(PACKAGE_LOAD);

    return qwest.get(patientAgentUrls.LIST, null, {cache: false})
        .then((xhr, response) => doAction(PACKAGE_LOAD_SUCCESS, response))
        .catch(error => doAction(PACKAGE_LOAD_FAIL, error))
}

export function manufactureAndShipRequest(serial) {
    doAction(PACKAGE_MANUFACTURE, serial);

    return qwest.post(manufactureUrls.PROCESS, {serial}, {cache: false})
        .then(() => doAction(PACKAGE_MANUFACTURE_SUCCESS))
        .catch(() => doAction(PACKAGE_MANUFACTURE_FAIL, 'Cannot manufacture package ' + serial));
}

export function receiveShipment(serial) {
    doAction(PACKAGE_DELIVERY, serial);

    return qwest.post(treatmentCenterUrls.RECEIVE, {serial}, {cache: false})
        .then(() => doAction(PACKAGE_DELIVERY_SUCCESS))
        .catch(() => doAction(PACKAGE_DELIVERY_FAIL))
}