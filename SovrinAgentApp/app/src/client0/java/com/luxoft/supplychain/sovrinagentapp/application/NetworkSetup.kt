package com.luxoft.supplychain.sovrinagentapp.application

import java.net.URL

/**
 * Global parameters for communicating with other parts of the network
 * such as backends, indy wallet, indy agents, etc.
 *
 * Those parameters may be specific to a client setup.
 *
 * The following configuration is for client-0 and should not be used for development.
 * */

const val GENESIS_PATH = "./docker.txn"
const val TAILS_PATH = "./tails"

const val GENESIS_IP = """52.224.217.165"""  // client0 azure

val BACKEND_BASE_URL = URL("http", "13.92.115.239", 8081, "/api/hospital/")  // client0 azure backend

const val WS_ENDPOINT = "ws://13.92.115.239:8096/ws"  // protected client0 client

const val WS_LOGIN = "medical-supplychain"
const val WS_PASS = "secretPassword"
