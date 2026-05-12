.PHONY: status verify build install clean doctor

status:
	@echo "--- kotlin repo status ---"
	@pwd
	@git status --short --untracked-files=all
	@git branch --show-current
	@git remote -v || true
	@git log -1 --oneline 2>/dev/null || true

verify:
	./gradlew assembleDebug

build: verify

install:
	./gradlew installDebug

clean:
	./gradlew clean

doctor:
	@echo "--- java ---"
	@java -version
	@echo "--- javac ---"
	@javac -version
	@echo "--- gradle android build smoke ---"
	./gradlew assembleDebug
