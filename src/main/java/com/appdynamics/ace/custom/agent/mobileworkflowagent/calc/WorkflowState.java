package com.appdynamics.ace.custom.agent.mobileworkflowagent.calc;

/**
 * Created by stefan.marx on 22.02.17.
 */
public class WorkflowState {

    private long _lastUpdate;
    private long _startTime;

    public String getLastState() {
        return _lastState;
    }

    private String _lastState;

    public String getStartState() {
        return _startState;
    }

    private String _startState;

    public State getState() {
        return _state;
    }

    private State _state;

    public String getName() {
        return _name;

    }

    private final String _name;

    public WorkflowState(String flowName) {
        _name = flowName;
        _lastUpdate = System.currentTimeMillis();
        _state = State.NONE;
    }

    public long age(long bcTime) {
        return bcTime-_lastUpdate;
    }

    public long flowTime(long bcTime) {
        return bcTime-_startTime;
    }

    public void start(String bcName, long bcTime) {
        _startState = bcName;
        _startTime = bcTime;
        _state = State.ACTIVE;
        update(bcName,bcTime);
    }

    private void update( long bcTime) {
        _lastUpdate = bcTime;
    }

    public void restart(String bcName, long bcTime) {
        start(bcName, bcTime);
    }

    public void update(String bcName, long bcTime) {
        update(bcTime);
        _lastState = bcName;
    }

    public void stop() {
        reset();
        _state = State.CLOSED;
    }

    public void reset() {
        _lastUpdate = 0;
        _lastState = null;
        _state = State.NONE;
    }



    public enum State {
        NONE,ACTIVE,CLOSED
    }
}
