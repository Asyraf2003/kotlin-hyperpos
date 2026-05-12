include mk/push.mk
include mk/android.mk

.PHONY: push
push:
	@$(MAKE) git-push
