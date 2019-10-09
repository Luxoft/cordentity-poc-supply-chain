import React from 'react';
import PropTypes from 'prop-types';
import './Card.scss';
import classSet from 'react-classset';


export default class Card extends React.Component {
    static propTypes = {
        header: PropTypes.string,
        imgSrc: PropTypes.string,
        imgTitle: PropTypes.string,
        onClick: PropTypes.func,
        disabled: PropTypes.bool
    };

    render() {
        const {header, imgSrc, imgTitle, onClick, disabled} = this.props;

        const classes = classSet({
            card: true,
            disabled
        });

        return (
            <div className="card-wrapper">
                <div onClick={!disabled ? onClick : undefined} className={classes}>
                    <h4 className='header'>
                        { header }
                    </h4>
                    <div className='img-wrapper'>
                        <div className='img' style={{ backgroundImage: `url(${imgSrc})` }} />
                    </div>
                    <p className='img-title'>{ imgTitle }</p>
                </div>
                <p style={{color: !disabled && 'transparent'}} className='disabled-text'>coming soon!</p>
            </div>
        )
    }
}
