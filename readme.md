Steps to initiate the services

1. Create docker network

    -- docker network create app-net

2. Run docker compose to start pulsar

    -- docker compose -f "docker-compose-pulsar.yml" up

3. Run docker compose to bring up orchestrator, service-a, service-b

    -- docker compose -f "docker-compose-services.yml" up --build

############## Important URLs ###########

Orchestrator: http://localhost:8080

Service A: http://localhost:8081

Service B: http://localhost:8082

Pulsar Admin (standalone): http://localhost:8085

Pulsar Manager UI: http://localhost:9527
