# github-repos-list
A GitHub REST API use task

![Java](https://img.shields.io/badge/Java-22%2B-blue)
![Maven](https://img.shields.io/badge/Maven-Build-brightgreen)
![JUnit](https://img.shields.io/badge/Tests-JUnit%205-orange)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)

A simple Java application that fetches all public repositories of a given GitHub user, lists their branches, and displays the latest commit SHA for each branch.

---

## Features

- Fetches all public repositories for a given GitHub username.
- Excludes forks
- Lists branches for each repository.
- Shows the latest commit SHA for every branch.
- Includes a **JUnit 5 integration test** for full output validation.

## Technologies Used

- **Java 22.0.2**
- **GitHub REST API**
- **JUnit 5** for testing
- **Maven** for dependency management and build

## Installation & Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/woltimer/github-repos-list.git
2. **Build the project**
   ```bash
   mvn clean install
4. **Run the application**
   ```bash
   mvn exec:java -Dexec.mainClass="main"
   ```
   By default, the username is hardcoded in main.java
   ```java
   new ListOfRepos().getRepos("torvalds");
   ```
   You can change "torvalds" to any valid GitHub username.

## Running Tests
```bash
mvn test
```
  The integration test:
  
  - Validates that repositories, branches, and SHAs are printed correctly.
  - Confirms that the program handles a valid GitHub user without fetch errors.
  Example Output:
  ```yaml
  Owner: torvalds
  Repository #1: linux
    - Branch: master, SHA: 4c1b6d...
    - Branch: fix-branch, SHA: a3e5d2...
  Repository #2: subsurface
    - Branch: master, SHA: 0f8b2a...
  ```
## License


   cd github-repos-list

