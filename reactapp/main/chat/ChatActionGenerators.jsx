import { POST_MESSAGE, RECEIVE_MESSAGE, CONNECT, DISCONNECT } from './chatActionTypes';

export function connect() {
    return {
        type: CONNECT
    }
}

export function disconnect() {
    return {
        type: DISCONNECT
    }
}

export function receiveMessage(message){
    return {
        type: RECEIVE_MESSAGE,
        message
    }
}

export function postMessage(text, recipient){
    return {
        type: POST_MESSAGE,
        text,
        recipient
    }
}