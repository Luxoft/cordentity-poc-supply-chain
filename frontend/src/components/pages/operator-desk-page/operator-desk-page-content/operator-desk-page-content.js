import React, {useState} from 'react'
import { createUseStyles, useTheme } from 'react-jss'
import QRCode from 'qrcode.react'
import { ProgressRing, Toast } from '@salesforce/design-system-react/'

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

export default function OperatorDeskPageContent() {
    const classes = useStyles(useTheme())
    const [progress, setProgress] = useState(0)
    const isLoading = progress < 100
    let heading = 'Authorization failed'
    let variant = 'error'

    if (isLoading) {
        setTimeout(() => setProgress(progress + 1),20)
    }
    else {
        if (Date.now().valueOf() % 2 === 0) {
            heading = 'Authorization success'
            variant = 'success'
        }
    }
    const progressContent = isLoading ? (<>
            <ProgressRing flowDirection="fill" size="large" value={progress} />
            <strong className={classes.progressStatus}>Receiving authorization status... </strong>
        </>) : (<Toast className={classes.toast} labels={{ heading }} variant={variant} />)
    const qrSize = window.screen.width * window.devicePixelRatio < 600 ? 200 : 400;
    return (
                <div className={classes.root}>
                    <div className={classes.qrContainer}>
                        <QRCode fgColor="#161616" size={qrSize} value={JSON.stringify({"invite":"http://172.21.0.3:8094/indy?c_i=eyJAdHlwZSI6ICJkaWQ6c292OkJ6Q2JzTlloTXJqSGlxWkRUVUFTSGc7c3BlYy9jb25uZWN0aW9ucy8xLjAvaW52aXRhdGlvbiIsICJsYWJlbCI6ICJjcmVkZW50aWFsLWlzc3VlciIsICJyZWNpcGllbnRLZXlzIjogWyJDajdDc0Rydm9zMnZrSjdmekZVM3Jna1NUZVFNVTZuVkc4RzZ6dEh1ZlNMViJdLCAic2VydmljZUVuZHBvaW50IjogImh0dHA6Ly8xNzIuMjEuMC4zOjgwOTQvaW5keSIsICJAaWQiOiAiM2M4NDU4ZDMtNzIwMC00OWNkLWI2YWUtMWFiNmZmNzZlNjU3In0="})}
                                level='H'/>
                        <div className={classes.progressContainer}>
                            {progressContent}
                        </div>
                    </div>
                </div>
    )
}
