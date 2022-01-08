package com.supersuman.gitamtransit

class RoutesData(val busName : String, val startPoint : String, val route : String, val coordinatesDetailsList: MutableList<String>)

class NewData(val busName : String, val route : String, val results: MutableList<String>)