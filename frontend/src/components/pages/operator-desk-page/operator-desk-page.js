import React from 'react'
import PropTypes from 'prop-types'
import TemplateFullWidth from 'Components/templates/template-full-width'
import LUMGlobalHeader from 'Components/organisms/lum-global-header'
import OperatorDeskPageContent from './operator-desk-page-content'
import OperatorDeskPageHeader from './operator-desk-page-header'

export default function OperatorDeskPage({ userMeta = { name: 'Adam Smith'} }) {
    return (
        <>
            <TemplateFullWidth
                growContent
                globalHeaderEl={<LUMGlobalHeader title='Welcome to Cherry Hill Medical Center' userMeta={null}/>}
                pageHeaderEl={<OperatorDeskPageHeader userMeta={null}/>}
                pageContentEl={<OperatorDeskPageContent />}
            />
        </>
    )
}

 OperatorDeskPage.propTypes = {
    userMeta: PropTypes.object,
 }
