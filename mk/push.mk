.PHONY: git-push
git-push:
	@echo "Checking status..."
	@git status
	@echo "Adding all changes..."
	@git add .
	$(eval NEXT_COUNT=$(shell echo $$(($$(git rev-list --count HEAD 2>/dev/null || echo 0) + 1))))
	@echo "Attempting auto-commit as: commit $(NEXT_COUNT)"
	@git commit -m "commit $(NEXT_COUNT)" || echo "Nothing new to commit, moving to push..."
	@echo "Pushing to origin current branch..."
	@git push origin $(shell git rev-parse --abbrev-ref HEAD)
	@echo "Done!"
