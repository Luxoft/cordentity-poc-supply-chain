import React, {useRef, useReducer, useEffect} from 'react'
import { createUseStyles, useTheme } from 'react-jss'
import QRCode from 'qrcode.react'
import { ProgressRing, Toast } from '@salesforce/design-system-react/'
import credentialsService from 'Services/CredentialsService'

const useStyles = createUseStyles((theme) => ({
    root: {
        height: '100%',
        minHeight: '24em',
        margin: '0px',
        padding: '0px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        flexDirection: 'column'
    },
    qrContainer: {
        order: 1,
    },
    progressContainer: {
        order: 2,
        paddingTop: '2rem',
        textAlign: 'center',
        '& .slds-notify__close': {
            display: 'none'
        }
    },
    toast: {
        width: '100%',
        minWidth: 'unset',
        margin: 0,
        '& button': {
            color: 'white'
        },
    },
    progressStatus: {
        paddingLeft: '1rem',
        verticalAlign: 'middle'
    },
    '@media all and (max-width: 500px)': {
        progressContainer: {
            width: '200px !important',
            height: '200px !important'
        },
    }
}))

const initialState = {
    invite: null,
    requestId: null,
    status: null,
    progress: 0
}

function reducer(state, action) {
  switch (action.type) {
    case 'setInvite':
      return {
        ...state,
        invite: action.invite,
        requestId: action.requestId,
    }
    case 'updateInviteStatus':
      return {
        ...state,
        status: action.status
    }
    case 'setProgress': 
      return {
        ...state,
        progress: action.progress,
    }
    default:
      return state
  }
}

const mapToastPropsFromStatus = (status) => {
    if (status === 'SUCCESS') {
        return {
            heading: 'Patient information updated!',
            variant: 'success'
        }
    }
    return {
        heading: 'Authorization failed',
        variant: 'error'
    }
} 

export default function OperatorDeskPageContent() {
    const classes = useStyles(useTheme())
    const [state, dispatch] = useReducer(reducer, initialState)
    const progressRef = useRef(state.progress);
    progressRef.current = state.progress;
    const qrSize = window.screen.width * window.devicePixelRatio < 500 ? 200 : 300
    const isComplete = (['SUCCESS', 'FAILED'].includes(state.status))

    useEffect(() => {
        if(!state.requestId) {
            credentialsService.auth({})
                .then(invite => dispatch({ type: 'setInvite', ...invite }))
                .catch(e => console.error(e))
        }
        else if (!isComplete) {
            let progressHandle
            const statusHandle = setInterval(() => {
                credentialsService.checkStatus(state.requestId)
                .then(status => {                    
                    dispatch({ type: 'updateInviteStatus', status })
                    if (status === 'CONNECTED' && !progressHandle) {
                        progressHandle = setInterval(() =>  {
                            const newProgress = progressRef.current === 100 ? 0 : progressRef.current + 2
                            dispatch({ type: 'setProgress', progress: newProgress })
                        }, 20)
                    }
                })
                .catch(e => console.error(e))
            }, 1500)
        
            return () => { 
                window.clearInterval(progressHandle)
                window.clearInterval(statusHandle) 
            }
        }
    }, [state.requestId])

    const toastProps = mapToastPropsFromStatus(state.status)
    const inviteContent = state.invite ? <QRCode fgColor="#161616" value={JSON.stringify(state.invite)} size={qrSize} level='H'/> : ''
    return (
            <div className={classes.root}>
                <div className={classes.qrContainer}>
                    {inviteContent}
                    <div className={classes.progressContainer}>
                        {state.status === 'CONNECTED' && <>
                            <ProgressRing flowDirection="fill" size="large" value={state.progress} />
                            <strong className={classes.progressStatus}>Receiving personal and coverage information... </strong>
                        </>}
                        {isComplete && <Toast className={classes.toast} labels={{ heading: toastProps.heading }} variant={toastProps.variant} />}
                    </div>
                </div>
            </div>
    )
}
