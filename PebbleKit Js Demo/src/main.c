#include <pebble.h>

//Some vars used throughout the app
static Window *window;
static TextLayer *text_layer;
static GColor color;
static bool active;

/*
 * Handles incoming app messages
 */
static void in_received_handler(DictionaryIterator *iter, void *context) {
  //Create a tuple
  Tuple *tuple = NULL;

  //Look for the background toggle tuple, we know it will be on key 0
  tuple = dict_find(iter, 0);
  if(tuple) {
    active = tuple->value->int32;
  }

  //look for the color to set the background
  tuple = dict_find(iter, 1);
  if(tuple) {
    color = GColorFromHEX(tuple->value->int32);
  }

  //Apply the background color if it was changed and enabled
  if(active) {
    window_set_background_color(window, color);
  } else {
    window_set_background_color(window,GColorWhite);
  }

  //Update the quote text
  tuple = dict_find(iter, 2);
  if (tuple) {
    text_layer_set_text(text_layer, tuple->value->cstring);
  }

}

/*
 * Initializes the main window and adds the text layer which will contain the quote
 */
static void window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);

  //Setup the text layer
  text_layer = text_layer_create(GRect(0, 30, bounds.size.w, 120));
  text_layer_set_text(text_layer, "Loading...");
  text_layer_set_background_color(text_layer, GColorClear);
  text_layer_set_text_alignment(text_layer, GTextAlignmentCenter);

  //Add the text layer to the root window layer
  layer_add_child(window_layer, text_layer_get_layer(text_layer));
}

/*
 * Handles unloading the text layer
 */
static void window_unload(Window *window) {
  text_layer_destroy(text_layer);
}

/*
 * Initializes the app:
 * - Listen for app messages
 * - Initialize the main window
 */
static void init(void) {
  //Setup some defaults
  active = false;
  color = GColorWhite;

  //Listen for incoming app messages
  app_message_open(256, 0);
  app_message_register_inbox_received(in_received_handler);

  // Creates the main window and sets it's load and unload callbacks
  window = window_create();
  window_set_window_handlers(window, (WindowHandlers) {
    .load = window_load,
    .unload = window_unload,
  });

  //push the main window to the window stack
  window_stack_push(window, true);
}

/*
 * Destroys all windows
 * De-registers the app message handlers
 */
static void deinit(void) {
  window_destroy(window);
  app_message_deregister_callbacks();
}

/*
 * Main Loop
 * Initializes app and then performs the app loop
 */
int main(void) {
  init();
  app_event_loop();
  deinit();
}
