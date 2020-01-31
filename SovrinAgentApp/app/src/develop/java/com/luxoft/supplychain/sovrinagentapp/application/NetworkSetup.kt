package com.luxoft.supplychain.sovrinagentapp.application

/**
 * Global parameters for communicating with other parts of the network
 * such as backends, indy wallet, indy agents, etc.
 *
 * Those parameters may be specific to a client setup.
 *
 * Use the following setup for development.
 * */

const val GENESIS_PATH = "./docker.txn"
const val TAILS_PATH = "./tails"

//const val GENESIS_IP = """52.15.95.244"""  // luxoft amazon
const val GENESIS_IP = """52.224.217.165"""  // client0 azure


//const val WS_ENDPOINT = "ws://localhost:8094/ws"
//const val WS_ENDPOINT = "ws://3.17.65.252:8094/ws"  // luxoft amazon
//const val WS_ENDPOINT = "ws://13.92.115.239:8094/ws"  // client0 azure backend
const val WS_ENDPOINT = "ws://52.224.104.135:8094/ws"  // client0 azure cred-issuer

const val WS_LOGIN = "medical-supplychain"
const val WS_PASS = "secretPassword"
