import React from 'react'
import PropTypes from 'prop-types'
import { Link } from 'react-router-dom'
import { theme } from 'Theme'
import { APP_ROUTES } from 'Data/constants'
import { Button, Avatar, GlobalNavigationBar, GlobalNavigationBarRegion } from '@salesforce/design-system-react'
export const LUMGlobalHeader = function LUMGlobalHeader({
  isAuthenticated, authInProgress, userMeta, title,
}) {
  return (
    <>
      <GlobalNavigationBar
        className="no-print-display"
        style={{
          borderBottom: theme.palette.black,
        }}
      >

        <GlobalNavigationBarRegion
          region="secondary"
          className="lum-global-header__logo"
        >
          <li className="slds-context-bar__item">
            <Link
              className="slds-context-bar__label-action"
              to={APP_ROUTES.Home}
            >
              <img
                src="/assets/images/lumedic_logo_color.svg"
                alt="Lumedic Logotype"
                style={{ width: '29px' }}
              />
            </Link>
          </li>
          <li className="slds-context-bar__item lum-global-header-title">
            <div className="slds-context-bar__label-title">
              {title}
            </div>
          </li>
        </GlobalNavigationBarRegion>
        <GlobalNavigationBarRegion
          region="secondary"
          className="lum-global-header__right"
        >
            <Button className="slds-global-actions__avatar">
                <Avatar
                    variant="user"
                    label={userMeta.name}
                    size="medium"
                />
            </Button>
        </GlobalNavigationBarRegion>
      </GlobalNavigationBar>
      {/* Print-only display for labeled header */}
      <div
        className="print-display"
        style={{
          borderBottom: `1px ${theme.palette.grays.gray5} solid`,
          paddingBottom: `${theme.units.md}px`,
        }}
      >
        <img
          src="/assets/images/lumedic_logotype_color.svg"
          alt="Lumedic Logotype"
          style={{
            width: '150px',
          }}
        />
        <span
          style={{ float: 'right' }}
        >
          {new Date().toLocaleDateString()}
        </span>
      </div>
    </>
  )
}

LUMGlobalHeader.defaultProps = {
  title: '',
  isAuthenticated: true,
  authInProgress: false,
  userMeta: { name: 'Test Patient'}
}

LUMGlobalHeader.propTypes = {
  title: PropTypes.string,
  isAuthenticated: PropTypes.bool,//.isRequired,
  authInProgress: PropTypes.bool,//.isRequired,
  userMeta: PropTypes.object,//.isRequired,
}

export default LUMGlobalHeader
