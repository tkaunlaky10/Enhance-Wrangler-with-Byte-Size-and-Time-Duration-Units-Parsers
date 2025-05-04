FROM maven:3.6-jdk-8
WORKDIR /app
COPY . /app

# Run the test command directly
CMD ["mvn", "test", "-Dmaven.exec.skip=true", "-Drat.numUnapprovedLicenses=100"]
