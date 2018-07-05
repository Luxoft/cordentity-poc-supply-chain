import {PACKAGE_LOAD, PACKAGE_LOAD_FAIL, PACKAGE_LOAD_SUCCESS, PACKAGE_MANUFACTURE} from "./actions";

const initialPackagesState = {
    packages: [],
    error: null,
    loading: false,
    manufacturing: null
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

        default:
            return state;
    }
};


export default {
    packages: PackagesReducer
}