import './Steps.scss';
import classSet from 'react-classset';
import React from 'react';
import PropTypes from 'prop-types';


const StepsShape = PropTypes.shape({
    header: PropTypes.string,
    subheader: PropTypes.string
});

export default class Steps extends React.Component {
    static propTypes = {
        steps: PropTypes.arrayOf(StepsShape),
        onStepChange: PropTypes.func,
        currentStep: PropTypes.number,
    };

    renderStep = (step, index) => {
        const classes = classSet({
            step: true,
            active: this.props.currentStep === index
        });

        return (
            <div key={index} onClick={this.handleStepChange(index)} className={classes}>
                <div className="content">
                    <p className='header'>{step.header}</p>
                    <p className='subheader'>{step.subheader}</p>
                </div>
            </div>
        )
    };

    handleStepChange = index => () => {
        this.props.onStepChange(index);
    };

    render() {
        const steps = this.props.steps.map(this.renderStep);

        return (
            <div className='steps'>
                {steps}
            </div>
        )
    }
}