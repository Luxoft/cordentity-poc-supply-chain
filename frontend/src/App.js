import React from 'react'
import {BrowserRouter as Router, Route, Switch} from 'react-router-dom'
import { IconSettings } from '@salesforce/design-system-react'
import standardSprite from '@salesforce-ux/design-system/assets/icons/standard-sprite/svg/symbols.svg'
import utilitySprite from '@salesforce-ux/design-system/assets/icons/utility-sprite/svg/symbols.svg'
import svg4everybody from 'svg4everybody'
import { ThemeProvider } from 'react-jss'
import { theme } from './theme'
import GlobalStyles from './theme/globals'
import OperatorDeskPage from 'Components/pages/operator-desk-page'

export default function App() {
    return (
        <ThemeProvider theme={theme}>
            <IconSettings standardSprite={standardSprite} utilitySprite={utilitySprite}>
                <GlobalStyles />
                 <Router>
                    <Switch>
                        <Route exact path='/' component={OperatorDeskPage} />
                    </Switch>
                </Router>
                {svg4everybody()}
            </IconSettings>
        </ThemeProvider>  )
}
