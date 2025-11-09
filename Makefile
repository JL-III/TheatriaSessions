.PHONY: fmt
fmt:
	./gradlew spotlessApply

.PHONY: build
build:
	./gradlew shadowJar

.PHONY: clean
clean:
	./gradlew clean