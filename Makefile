.PHONY: build
build:
	./gradlew shadowJar

.PHONY: clean
clean:
	./gradlew clean

.PHONY: test
test:
	./gradlew runServer --console=plain