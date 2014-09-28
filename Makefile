WAR := target/kahvipaivakirja-0.1.0-SNAPSHOT-standalone.war

.PHONY: all
all: docs $(WAR)

.PHONY: docs
docs:
	cd doc && make all

.PHONY: war
war: $(WAR)

$(WAR): src/*/*.clj src/*/*.sql project.clj README.md
	lein ring uberwar

.PHONY: deploy
deploy: $(WAR)
	scp $< users.cs.helsinki.fi:tomcat/webapps/kahvipaivakirja.war

.PHONY: reset-db
reset-db:
	cat src/sql/drop-tables.sql src/sql/create-tables.sql src/sql/add-test-data.sql | psql
