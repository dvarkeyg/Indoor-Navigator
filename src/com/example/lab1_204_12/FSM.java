package com.example.lab1_204_12;

public class FSM {
	private enum State{
		RISING, FALLING, STABLE, NEGATIVE;
	}
	
	
	State state = State.STABLE;
	public void setState(State toSet){
		state = toSet;
	}
	
	public State getState(){
		return state;
	}
	
	public boolean isStep(float [] vals){
		state = State.STABLE;
		for (int i = 1; i<20;i++){
			if(state == State.STABLE && vals[i] > vals[i-1] && vals[i] > 1.0 && vals[i]<2.2){
				state = State.RISING;
			}else if(state == State.RISING && vals[i] < vals[i-1] && vals[i] >1.0 && vals[i]<2.2){
				state = State.FALLING;
				return true;
			}else{
				state = State.STABLE;
			}
			
		}
		return false;
		
	}
}
