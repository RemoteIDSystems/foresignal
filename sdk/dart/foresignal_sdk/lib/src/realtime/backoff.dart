/// Jittered exponential reconnect backoff: 1 s doubling to 60 s, each delay multiplied by a random factor in
/// [0.8, 1.2) and clamped to [initial, max], never below a server-requested floor (`retry:` or `Retry-After`).
library;

import 'dart:math' as math;

/// Computes successive reconnect delays.
class Backoff {
  /// Creates a backoff; [random] returns values in [0, 1) (default `Random().nextDouble`).
  Backoff({this.initial = const Duration(seconds: 1), this.max = const Duration(seconds: 60), double Function()? random})
      : _random = random ?? math.Random().nextDouble;

  /// First delay.
  final Duration initial;

  /// Largest delay.
  final Duration max;

  final double Function() _random;
  int _attempt = 0;

  /// Delays handed out since the last [reset].
  int get attempt => _attempt;

  /// The next delay, never below [minimum] (which may exceed [max]).
  Duration next([Duration minimum = Duration.zero]) {
    final initialMs = initial.inMilliseconds;
    final maxMs = max.inMilliseconds;
    final base = math.min(maxMs.toDouble(), initialMs * math.pow(2, math.min(_attempt, 30)).toDouble());
    _attempt++;
    final jittered = math.min(maxMs.toDouble(), math.max(initialMs.toDouble(), base * (0.8 + 0.4 * _random())));
    final delay = Duration(milliseconds: jittered.round());
    return delay < minimum ? minimum : delay;
  }

  /// Starts over at [initial] (after a connection that worked).
  void reset() {
    _attempt = 0;
  }
}
