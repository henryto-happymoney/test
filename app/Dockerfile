FROM 730502903637.dkr.ecr.us-east-1.amazonaws.com/openjdk:11.0.9.1
ENV WORKSPACE=/home/builder/app

COPY . .

EXPOSE 8080
CMD ["java", "-jar", "app.jar", "-noverify"]
