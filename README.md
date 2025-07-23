# 🗺️ City Tour Planner with Dynamic Programming

This project implements a **Dynamic Programming (DP)** approach to optimize city tour planning for tourists. The aim is to determine the most enjoyable tour path by considering various dynamic factors.

## ✅ Problem Description

- The city is modeled as a graph:
  - **Nodes** represent landmarks (with names and base attractiveness scores)
  - **Edges** represent streets (with travel time as weights)
- The tourist tour:
  - **Starts and ends at the Hotel**
  - **Visits each landmark at most once**
  - **Maximizes the total adjusted attractiveness score**

## 🧮 Attractiveness Score Formula

Each landmark's adjusted score is calculated with the formula:

AdjustedScore = BaseScore * PersonalInterest * max(1 - 0.03 * VisitorLoad * TimeToLandmark, 0.1)


Factors:
- **BaseScore**: inherent appeal of the landmark
- **PersonalInterest**: user's rating (0 to 1) for that type of landmark
- **VisitorLoad**: how crowded the landmark is
- **TimeToLandmark**: travel time from the previous landmark

## 🧠 Solution Approach

- Graph structure is parsed from input files
- Uses **bitmask-based Dynamic Programming** to:
  - Track visited landmarks
  - Maximize total adjusted score
  - Trace back the optimal route

## 📁 Input Files

- `landmark_map_data.txt`: Graph structure
- `personal_interest.txt`: User's interest values
- `visitor_load.txt`: Visitor density per landmark

## 💻 Technologies

- Java
- File I/O
- Bitmask Dynamic Programming

---

Feel free to explore the code and test your own city maps or tourist preferences!

