# Running Route Researcher
An Android app that generates circular running routes of exact distances using real road networks.

## Background
This app was created as the final project for CM2023 Mobile Sports Applications and Data Mining.

## Algorithm
### Concept
1. X waypoints get placed at start/end position
2. Waypoints get moved to form a circle.
3. Waypoints get snapped to nearest road.
4. Waypoints are connected with driving directions, displayed as polylines.

### Details
- Number of waypoints are determined by input distance.
- Circle formation (full circle, half circle, quarter circle) is determined by distance.
- The algorithm is ran recursively with binary search until distance tolerances are met or maxAttempts is reached.
- 
### Distance Accuracy
The App uses a two tolerance system to achive it's accurucy while not sacrificing too much performance.

### Tolerances
**Primary Tolerance**
Is the main target, total distance within 0.5km.

**Secondary Tolerance**
Is used to try to mitigate distances that differ too much from target.
It is set to 1.0km or 0.5% if distance is longer than 20km.
If secondary tolerance is not met maxAttempts are increased.

## API Setup
Google Maps Maps API used to retrieve and display the map.

OpenRouteService Snap API is used to snap a given coordinate to the closest road.

OpenRouteService Driving Directions API is used to generate directions and polylines between waypoints.

## Future Improvements

## License

MIT License - feel free to use and modify for your own projects

## Author
William Sjödén
