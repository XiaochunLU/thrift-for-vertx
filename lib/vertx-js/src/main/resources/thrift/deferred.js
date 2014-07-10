//     This deferred implementation is ported from Zepto.js.
//     Modified by XiaochunLU at 2014-06-18.
//
//     Zepto.js
//     (c) 2010-2014 Thomas Fuchs
//     Zepto.js may be freely distributed under the MIT license.
//
//     Some code (c) 2005, 2013 jQuery Foundation, Inc. and other contributors

//
// Simple implementations for $ functions used below.
//

var $ = {},
    emptyArray = [],
    slice = Array.prototype.slice;

function likeArray(obj) {
  return typeof obj.length === "number";
}
$.each = function(elements, callback) {
  var i, key;
  if (likeArray(elements)) {
    for (i = 0; i < elements.length; i++)
      if (callback.call(elements[i], i, elements[i]) === false) return elements;
  } else {
    for (key in elements)
      if (callback.call(elements[key], key, elements[key]) === false) return elements;
  }

  return elements;
};
$.isFunction = function(value) {
  return typeof value === "function";
};
$.extend = function(target, source) {
  if (source) {
    for (var k in source) {
      if (source.hasOwnProperty(k)) {
        target[k] = source[k];
      }
    }
  }
  return target;
};
$.inArray = function(elem, array, i) {
  return emptyArray.indexOf.call(array, elem, i);
};

//
// Zepto -> _callback.js_
//

// Create a collection of callbacks to be fired in a sequence, with configurable behaviour
// Option flags:
//   - once: Callbacks fired at most one time.
//   - memory: Remember the most recent context and arguments
//   - stopOnFalse: Cease iterating over callback list
//   - unique: Permit adding at most one instance of the same callback
$.Callbacks = function(options) {
  options = $.extend({}, options);

  var memory, // Last fire value (for non-forgettable lists)
      fired, // Flag to know if list was already fired
      firing, // Flag to know if list is currently firing
      firingStart, // First callback to fire (used internally by add and fireWith)
      firingLength, // End of the loop when firing
      firingIndex, // Index of currently firing callback (modified by remove if needed)
      list = [], // Actual callback list
      stack = !options.once && [], // Stack of fire calls for repeatable lists
      fire = function(data) {
        memory = options.memory && data;
        fired = true;
        firingIndex = firingStart || 0;
        firingStart = 0;
        firingLength = list.length;
        firing = true;
        for (; list && firingIndex < firingLength; ++firingIndex) {
          if (list[firingIndex].apply(data[0], data[1]) === false && options.stopOnFalse) {
            memory = false;
            break;
          }
        }
        firing = false;
        if (list) {
          if (stack) stack.length && fire(stack.shift());
          else if (memory) list.length = 0;
          else Callbacks.disable();
        }
      },

      Callbacks = {
        add: function() {
          if (list) {
            var start = list.length,
              add = function(args) {
                $.each(args, function(_, arg) {
                  if (typeof arg === "function") {
                    if (!options.unique || !Callbacks.has(arg)) list.push(arg);
                  } else if (arg && arg.length && typeof arg !== 'string') add(arg);
                });
              };
            add(arguments);
            if (firing) firingLength = list.length;
            else if (memory) {
              firingStart = start;
              fire(memory);
            }
          }
          return this;
        },
        remove: function() {
          if (list) {
            $.each(arguments, function(_, arg) {
              var index;
              while ((index = $.inArray(arg, list, index)) > -1) {
                list.splice(index, 1);
                // Handle firing indexes
                if (firing) {
                  if (index <= firingLength)--firingLength;
                  if (index <= firingIndex)--firingIndex;
                }
              }
            });
          }
          return this;
        },
        has: function(fn) {
          return !!(list && (fn ? $.inArray(fn, list) > -1 : list.length));
        },
        empty: function() {
          firingLength = list.length = 0;
          return this;
        },
        disable: function() {
          list = stack = memory = undefined;
          return this;
        },
        disabled: function() {
          return !list;
        },
        lock: function() {
          stack = undefined;
          if (!memory) Callbacks.disable();
          return this;
        },
        locked: function() {
          return !stack;
        },
        fireWith: function(context, args) {
          if (list && (!fired || stack)) {
            args = args || [];
            args = [context, args.slice ? args.slice() : args];
            if (firing) stack.push(args);
            else fire(args);
          }
          return this;
        },
        fire: function() {
          return Callbacks.fireWith(this, arguments);
        },
        fired: function() {
          return !!fired;
        }
      };

  return Callbacks;
};

//
// Zepto -> _deferred.js_
//

var Deferred = module.exports.Deferred = function(func) {
  var tuples = [
        // action, add listener, listener list, final state
        ["resolve", "done", $.Callbacks({
          once: 1,
          memory: 1
        }), "resolved"],
        ["reject", "fail", $.Callbacks({
          once: 1,
          memory: 1
        }), "rejected"],
        ["notify", "progress", $.Callbacks({
          memory: 1
        })]
      ],
      state = "pending",
      promise = {
        state: function() {
          return state;
        },
        always: function() {
          deferred.done(arguments).fail(arguments);
          return this;
        },
        then: function( /* fnDone [, fnFailed [, fnProgress]] */ ) {
          var fns = arguments;
          return Deferred(function(defer) {
            $.each(tuples, function(i, tuple) {
              var fn = $.isFunction(fns[i]) && fns[i];
              deferred[tuple[1]](function() {
                var returned = fn && fn.apply(this, arguments);
                if (returned && $.isFunction(returned.promise)) {
                  returned.promise()
                    .done(defer.resolve)
                    .fail(defer.reject)
                    .progress(defer.notify);
                } else {
                  var context = this === promise ? defer.promise() : this,
                    values = fn ? [returned] : arguments;
                  defer[tuple[0] + "With"](context, values);
                }
              });
            });
            fns = null;
          }).promise();
        },
        promise: function(obj) {
          return obj ? $.extend(obj, promise) : promise;
        }
      },
      deferred = {};

  $.each(tuples, function(i, tuple) {
    var list = tuple[2],
        stateString = tuple[3];

    promise[tuple[1]] = list.add;

    if (stateString) {
      list.add(function() {
        state = stateString;
      }, tuples[i ^ 1][2].disable, tuples[2][2].lock);
    }

    deferred[tuple[0]] = function() {
      deferred[tuple[0] + "With"](this === deferred ? promise : this, arguments);
      return this;
    };
    deferred[tuple[0] + "With"] = list.fireWith;
  });

  promise.promise(deferred);
  if (func) func.call(deferred, deferred);
  return deferred;
}

module.exports.when = function(sub) {
  var resolveValues = slice.call(arguments),
      len = resolveValues.length,
      i = 0,
      remain = len !== 1 || (sub && $.isFunction(sub.promise)) ? len : 0,
      deferred = remain === 1 ? sub : Deferred(),
      progressValues, progressContexts, resolveContexts,
      updateFn = function(i, ctx, val) {
        return function(value) {
          ctx[i] = this;
          val[i] = arguments.length > 1 ? slice.call(arguments) : value;
          if (val === progressValues) {
            deferred.notifyWith(ctx, val);
          } else if (!(--remain)) {
            deferred.resolveWith(ctx, val);
          }
        };
      };

  if (len > 1) {
    progressValues = new Array(len);
    progressContexts = new Array(len);
    resolveContexts = new Array(len);
    for (; i < len; ++i) {
      if (resolveValues[i] && $.isFunction(resolveValues[i].promise)) {
        resolveValues[i].promise()
          .done(updateFn(i, resolveContexts, resolveValues))
          .fail(deferred.reject)
          .progress(updateFn(i, progressContexts, progressValues));
      } else {
        --remain;
      }
    }
  }
  if (!remain) deferred.resolveWith(resolveContexts, resolveValues);
  return deferred.promise();
};
