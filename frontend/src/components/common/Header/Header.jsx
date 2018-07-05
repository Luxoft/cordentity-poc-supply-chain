import BackBtnPNG from '../../../assets/img/back-btn.svg';
import React from 'react';
import PropTypes from 'prop-types';
import './Header.scss';


const UserShape = PropTypes.shape({
    name: PropTypes.string,
    description: PropTypes.string,
    avatar: PropTypes.string
});

export default class Header extends React.Component {
    static propTypes = {
        header: PropTypes.string,
        subheader: PropTypes.string,
        user: UserShape,
        onBackClick: PropTypes.func,
    };

    render() {
        const {header, subheader, onBackClick, user} = this.props;

        return (
            <header className='header'>
                <nav>
                    <img src={BackBtnPNG} onClick={onBackClick} className='back-btn' alt="Go back"/>
                    <div className="text">
                        <h1 className="header">{header}</h1>
                        <h2 className="subheader">{subheader}</h2>
                    </div>
                </nav>
                <div className='user'>
                    <div className='info'>
                        <h3 className='name'>{user.name}</h3>
                        <h5 className='description'>{user.description}</h5>
                    </div>
                    <img className='avatar' data-rjs="3" src={user.avatar} alt={user.name}/>
                </div>
            </header>
        )
    }
}