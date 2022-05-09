#include <stdio.h>
#include <pthread.h>
#include <dispatch/dispatch.h>
#include <CydiaSubstrate/CydiaSubstrate.h>

bool _dispatch_runloop_root_queue_perform_4CF(dispatch_queue_t queue);

struct perform_state {
  dispatch_queue_t dq;
  pthread_cond_t cond;
  pthread_mutex_t lock;
  volatile bool finished;
};

static void *dispatch_queue_perform(void *arg) {
  struct perform_state *state = (struct perform_state *) arg;
  dispatch_queue_t dq = state->dq;

  while(_dispatch_runloop_root_queue_perform_4CF(dq)) {
  }

  state->finished = true;
  pthread_cond_broadcast(&state->cond);
  return NULL;
}

void (*old_dispatch_sync)(dispatch_queue_t dq, void (^work)(void));
void (*old_dispatch_async)(dispatch_queue_t dq, void (^work)(void));

void new_dispatch_sync(dispatch_queue_t dq, void (^work)(void)) {
  struct perform_state state;
  state.dq = dq;
  state.finished = false;
  pthread_cond_init(&state.cond, NULL);
  pthread_mutex_init(&state.lock, NULL);
  pthread_t thread = NULL;
  int ret = pthread_create(&thread, NULL, dispatch_queue_perform, &state);
  if(ret != 0) {
    printf("Patch dispatch_sync dq=%p, ret=%d, thread=%p\n", dq, ret, thread);
  }
  old_dispatch_async(dq, work);
  while (!state.finished) {
    pthread_cond_wait(&state.cond, &state.lock);
  }

  pthread_cond_destroy(&state.cond);
  pthread_mutex_destroy(&state.lock);
}

void new_dispatch_async(dispatch_queue_t dq, void (^work)(void)) {
  dq = dispatch_queue_create(NULL, NULL);
  struct perform_state state;
  state.dq = dq;
  state.finished = false;
  pthread_cond_init(&state.cond, NULL);
  pthread_mutex_init(&state.lock, NULL);
  pthread_t thread = NULL;
  int ret = pthread_create(&thread, NULL, dispatch_queue_perform, &state);
  if(ret != 0) {
    printf("Patch dispatch_async dq=%p, ret=%d, thread=%p\n", dq, ret, thread);
  }
  old_dispatch_async(dq, work);
  while (!state.finished) {
    pthread_cond_wait(&state.cond, &state.lock);
  }

  pthread_cond_destroy(&state.cond);
  pthread_mutex_destroy(&state.lock);
  dispatch_release(dq);
}

__attribute__((constructor))
static void init() {
  printf("Patch dispatch: dispatch_sync=%p, dispatch_async=%p.\n", &dispatch_sync, &dispatch_async);
  MSHookFunction((void*)&dispatch_async, (void*)new_dispatch_async, (void**)&old_dispatch_async);
  // MSHookFunction((void*)&dispatch_sync, (void*)new_dispatch_sync, (void**)&old_dispatch_sync);
}
