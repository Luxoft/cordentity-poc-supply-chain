import React from 'react'
import PropTypes from 'prop-types'
import { Button } from '@salesforce/design-system-react'
import { Link } from 'react-router-dom'

const LUMButton = function LUMButton({
  link, canTruncate, classes, ...rest
}) {
  let className = ''

  if (canTruncate) className += ' slds-truncate'

  if (link) {
    if (link.type === 'internal') {
      return (
        <Link to={link.href}><Button className={className} {...rest} /></Link>
      )
    }

    return (
      <a href={link.href} target="_blank" rel="noopener noreferrer"><Button className={className} {...rest} /></a>
    )
  }

  return (
    <Button className={className} {...rest} />
  )
}

LUMButton.defaultProps = {
  canTruncate: false,
  classes: {},
  link: null,
}

LUMButton.propTypes = {
  canTruncate: PropTypes.bool,
  classes: PropTypes.object,
  link: PropTypes.object,
}

export default LUMButton
