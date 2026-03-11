APP_NAME=fx-deals-warehouse

.PHONY: build test run docker-up docker-down clean

build:
	mvn clean package -DskipTests

test:
	mvn clean test

run:
	mvn spring-boot:run

docker-up:
	mvn clean package -DskipTests
	docker compose up --build -d

docker-down:
	docker compose down -v

clean:
	mvn clean
