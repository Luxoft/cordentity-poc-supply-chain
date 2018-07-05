import React from 'react';
import './Dimmer.scss';
import classSet from 'react-classset';


export default class Dimmer extends React.Component {
    state = {
        fade: true
    };

    componentDidMount() {
        setImmediate(() => this.setState({fade: false}))
    }

    render() {
        const {fade} = this.state;

        const classes = classSet({
            fade,
            dimmer: true
        });

        return (
            <div className={classes}>{this.props.children}</div>
        )
    }
};