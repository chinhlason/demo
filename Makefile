up:
	docker-compose -f docker-compose.yml -f docker-compose-monitor.yml up -d

down:
	docker-compose -f docker-compose.yml -f docker-compose-monitor.yml down
