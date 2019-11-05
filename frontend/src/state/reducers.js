import {
    INVITE_GET, INVITE_GET_FAIL,
    INVITE_GET_SUCCESS,
    PACKAGE_LOAD,
    PACKAGE_LOAD_FAIL,
    PACKAGE_LOAD_SUCCESS,
    PACKAGE_PROOFS,
    PACKAGE_PROOFS_FAIL,
    PACKAGE_PROOFS_SUCCESS,
    PACKAGE_MANUFACTURE
} from "./actions";

const initialPackagesState = {
    packages: [],
    error: null,
    loading: false,
    manufacturing: null,
    invite: null
};

const initialProofsState = {
    proofs: [],
    error: null,
    loading: false
};

const PackagesReducer = (state = initialPackagesState, action) => {
    switch (action.type) {
        case PACKAGE_LOAD:
            return {
                ...state,
                loading: true
            };

        case PACKAGE_LOAD_SUCCESS:
            return {
                ...initialPackagesState,
                packages: action.payload,
            };

        case PACKAGE_LOAD_FAIL:
            return {
                ...initialPackagesState,
                error: action.payload
            };

        case PACKAGE_MANUFACTURE:
            return {
                ...state,
                manufacturing: action.payload,
            };

        case INVITE_GET:
            return {
                ...state,
                invite: null
            };

        case INVITE_GET_SUCCESS:
            return {
                ...state,
                invite: action.payload
            };

        case INVITE_GET_FAIL:
            return {
                ...initialPackagesState,
                error: action.payload
            };

        default:
            return state;
    }
};

const ProofsReducer = (state = initialProofsState, action) => {
    switch (action.type) {
        case PACKAGE_PROOFS:
            return {
                ...state,
                loading: true
            };

        case PACKAGE_PROOFS_SUCCESS:
            return {
                ...initialProofsState,
                proofs: action.payload,
            };

        case PACKAGE_PROOFS_FAIL:
            return {
                ...initialProofsState,
                error: action.payload
            };

        default:
            return state;
    }
};

export default {
    packages: PackagesReducer,
    proofs: ProofsReducer
}
