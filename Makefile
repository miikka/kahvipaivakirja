WAR := target/kahvipaivakirja-0.1.0-SNAPSHOT-standalone.war

.PHONY: all
all: docs $(WAR)

.PHONY: docs
docs:
	cd doc && make all

$(WAR): src/*/*.clj
	lein ring uberwar

.PHONY: deploy
deploy: $(WAR)
	scp $< users.cs.helsinki.fi:tomcat/webapps/kahvipaivakirja.war
