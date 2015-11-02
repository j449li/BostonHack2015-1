# TurfWarz

###Endpoints
```
POST https://turfwarz.herokuapp.com/user/info 
request: {
    "player_id":"maidi"
}
response: {
  "area": [
    {
      "id": "3392053E-9094-4D84-8654-2EEBE6578184",
      "player_id": "maidi",
      "area": null,
      "latitude": 42.34482,
      "longitude": -71.10372
    },
    {
      "id": "6D961A7B-7CAD-4B3D-A4EF-2A2773960004",
      "player_id": "maidi",
      "area": null,
      "latitude": 42.34482,
      "longitude": -71.1039
    }
  ]

POST https://turfwarz.herokuapp.com/map/update
request: {
    "points":[
        {"latitude":42.345279, "longitude":-71.104384},
        {"latitude":42.345466, "longitude":-71.103963},
        {"latitude":42.344652, "longitude":-71.103960},
        {"latitude":42.345049, "longitude":-71.103322}
    ],
    "player_id":"ethan"
}
response: check for status 200 for success
```
