# Response Time Analyzer

Tool for analyzing real-time task response times based on classical scheduling algorithms and resource sharing protocols.

## Description

This application calculates worst-case response times for sets of periodic tasks, supporting multiple modes:
- MRS (Rate-Monotonic Scheduling): Scheduling based on task periods.
- DMS (Deadline-Monotonic Scheduling): Scheduling based on task deadlines.
- Resource Sharing Protocols: Analysis of blocking times under Priority Inheritance Protocol (PIP) and Priority Ceiling Protocol (PCP).

It is built as a Spring Boot web application: simply upload a CSV file with your tasks and instantly get a detailed response-time analysis.

## Technologies Used

- Java 21
- Spring Boot 3.4.4
- Maven
- Git

## Installation

You can run the application directly using DockerHub without building it manually.

Pull the prebuilt Docker image:

docker pull semiguerra/response-time-analyzer:latest

Run the container: 

docker run -p 8080:8080 semiguerra/response-time-analyzer

Then open your browser and go to http://localhost:8080

If you prefer to build the application locally:

git clone https://github.com/semiguerra/response-time-analyzer.git
cd response-time-analyzer
./mvnw spring-boot:run

## How to Use

1. Access the app at http://localhost:8080
2. Upload a CSV file containing your task set.
3. View the scheduling analysis, including:
   - Response times
   - Schedulability tests
   - Blocking times if resource sharing is involved

## CSV Input Format

The application accepts two input formats:

Simple Format (without resources):
Process, Priority, C (Computation Time), T (Period)
Task1, 3, 2, 5
Task2, 2, 1, 10

Priority field is mandatory here.
Used for MRS or DMS analysis without considering resources.

Extended Format (with resources):
Task, Period, Deadline, C, Resources
TaskA, 5, 5, 2, Resource1:1;Resource2:2
TaskB, 10, 10, 1, Resource1:1

Resources field lists the resources used by the task and the time spent on each, separated by ';'.
Used for analysis with resource sharing protocols (PIP and PCP).

## Future Work

- Add support for task release jitter to model more realistic scheduling behavior.

- Include context switch overhead in the response time analysis for more accurate calculations.

- Improve input form flexibility to allow optional jitter and context switch parameters.

- Provide export options for analysis results (CSV, JSON).

## Contributions

Contributions, suggestions, and improvements are welcome.
Feel free to fork this project, open an issue, or create a pull request.

## License

This project is open-source and available under the MIT License.
