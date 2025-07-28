FROM openjdk:21
LABEL authors="AL-AMEER S R"

# Set working directory
WORKDIR /app

# Copy JAR to /app
COPY target/Job_Automation_Tool-0.0.1-SNAPSHOT.jar Job_Automation_Tool.jar

# Run the JAR
ENTRYPOINT ["java", "-jar", "Job_Automation_Tool.jar"]
