# Task Overview
A retail analytics company provides custom sales reports for internal teams. To avoid blocking operations and improve performance, the company wants their report generation system to handle requests asynchronously. When a new report is requested, the system should queue report creation in the background, instantly provide a unique tracking ID, and allow users to check the status of their report until it completes (or fails).

# Objectives
- Design a system that accepts report requests and returns a unique identifier without waiting for completion
- Provide a way to query the current state of any report using its identifier
- Handle report generation in the background to keep the main application responsive
- Simulate realistic report generation that takes time and may occasionally encounter issues
- Ensure the system works correctly when multiple reports are requested simultaneously
- Maintain clear organization and separation between different responsibilities in your code

# Helpful Tips
- Consider organizing your solution into distinct classes with well-defined responsibilities
- Think about what information needs to be tracked for each report throughout its lifecycle
- Remember that when multiple threads access shared data, you'll need appropriate safeguards
- Background operations should simulate realistic delays and handle both success and failure scenarios
- Status information should be retrievable at any time using the tracking identifier
- Consider how you'll manage background workers and ensure proper cleanup when done
- Exception handling and resource management are important for production-quality code
- Aim for efficiency: avoid unnecessary waiting, polling loops, or blocking operations

# How to Verify
- Submit multiple report requests rapidly and confirm each returns a unique identifier immediately
- Query report status at different intervals: observe the progression from initial state through to completion or failure
- Verify that completed reports provide expected outcome details while failed reports indicate what went wrong
- Test with concurrent requests to ensure statuses remain accurate and don't get mixed up
- Confirm that background workers are properly shut down when the application completes
- Review your code structure for clarity, appropriate naming, and adherence to Java conventions
