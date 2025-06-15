# Helsinki Route Planner

A JavaFX-based application for planning public transport routes in Helsinki, featuring real-time journey planning and transit heatmap visualization.

## Application Interface 🖥️

### Main Menu


### Main Menu
![Main Menu](ApplicationDesign/MainMenu.png)
*The main interface allows users to choose between route planning and heatmap visualization*

### Journey Planning
![Route Planning](ApplicationDesign/Route.png)
*Plan your journey with interactive map selection and multiple routing options*

### Heatmap View
![Heatmap Visualization](ApplicationDesign/Heatmap.png)
*Visualize transit accessibility across Helsinki with color-coded regions*

## Features 🌟

### 1. Journey Planning
- Plan public transport routes across Helsinki
- Support for multiple routing algorithms:
    - Dijkstra's Algorithm
    - A* Algorithm
- Real-time journey calculations considering:
    - Multiple tarnsport types
    - Walking distances
    - Transit times

### 2. Heatmap Visualization
- Visual representation of transit accessibility
- Color-coded areas showing
    - Travel time zones
    - Transport coverage
- Support for multiple heatmap algorithms:
    - Dijkstra's Algorithm
    - A* Algorithm
- Interactive map interface

### 3. User Interface
- Interactive map with zoom and dragging functionality
- Waypoint selection for start/end locations
- Real-time weather information display
- Clean, intuitive interface with:
    - Main menu navigation
    - Algorithm selection
    - Results visualization

### Key classes
- `RoutingEngine`: Core routing logic
- `WindowManager`: UI coordination
- `MapComponent`: Map visualization
- `DirectionsWindow`: Route planning interface
- `HeatMapWindow`: Heatmap visualization

## Data Sources 📊

- Helsinki Regional Transport (HSL) GTFS data
- OpenStreetMap for base map visualization
- Locat tifos for offline map rendering

## Technologies Used 💻

- Java 17
- JavaFX for UI
- JXMapViewer2 for map visualization
- OpenCSV for data parsing
- Maven for dependency management

## Usage Guide 📝

1. Launch the application
2. Choose between "Plan a journey" or "Heatmap" mode
3. Select your preferred algorithm 
4. For journey planning:
    - Click to set start and end points
    - View route suggestion
5. For heatmap view:
    - Select a start point
    - View accessibility visualization

## Special Thanks 🤝

This project was developed as part of the Computer Science Bachelor at Maastricht University, The Netherlands. Special thanks to my group teammates with which we worked collaboratively to deliver this comprehensive route planning solution, combining our diverse skills in software development, algorithmic thinking, and user experience design.

