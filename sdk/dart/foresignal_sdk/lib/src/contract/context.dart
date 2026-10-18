/// Workspace contexts (docs/Client-Contract.md §5, schema version 1): JSON merge patches (RFC 7396) and the
/// client-side checks that mirror the server's validation.
library;

import 'dart:convert';

import '../errors.dart';
import 'object_ref.dart';

/// The context members of schema version 1.
const contextMembers = [
  'selectedRefs', 'focusRef', 'timeRange', 'asOf', 'playback', 'viewport', 'activeSituationId',
  'activeHypothesisSetId', 'filters', 'layers',
];

/// Most selected references.
const maxSelectedRefs = 200;

/// Most layers.
const maxLayers = 200;

/// Largest serialized context, in bytes.
const maxContextBytes = 16384;

/// Largest serialized filters object, in bytes.
const maxFiltersBytes = 4096;

final _ulid = RegExp(r'^[0-9A-HJKMNP-TV-Z]{26}$');
final _time = RegExp(r'^\d{4}-\d{2}-\d{2}T');

/// A deep copy of a JSON value.
T cloneJson<T>(T value) => value == null ? value : jsonDecode(jsonEncode(value)) as T;

/// Applies a JSON merge patch (RFC 7396) and returns the result; neither argument is modified. Objects merge
/// member by member, `null` removes a member, anything else (lists included) replaces.
Object? applyMergePatch(Object? target, Object? patch) {
  if (patch is! Map) {
    return cloneJson(patch);
  }
  final out = target is Map ? Map<String, Object?>.from(cloneJson(target)) : <String, Object?>{};
  patch.forEach((key, value) {
    if (value == null) {
      out.remove(key);
    } else {
      out['$key'] = applyMergePatch(out['$key'], value);
    }
  });
  return out;
}

/// A copy of [context] without its top-level null members (what the server stores and deep links carry).
Map<String, Object?> stripNullMembers(Map<String, Object?> context) =>
    {for (final e in context.entries) if (e.value != null) e.key: e.value};

DateTime? _parseTime(Object? value) => value is String && _time.hasMatch(value) ? DateTime.tryParse(value) : null;

bool _isSimple(Object? value) {
  bool scalar(Object? v) => v == null || v is String || v is num || v is bool;
  return scalar(value) || (value is List && value.every(scalar));
}

int _utf8Length(Object? value) => utf8.encode(jsonEncode(value)).length;

/// The problems of a context under schema version 1 (empty when valid).
List<ContextFieldError> contextProblems(Object? context, {String prefix = 'context'}) {
  final errors = <ContextFieldError>[];
  void add(String field, String message) => errors.add(ContextFieldError(field, message));
  if (context == null) {
    return errors;
  }
  if (context is! Map) {
    return [ContextFieldError(prefix, 'must be an object')];
  }
  final p = '$prefix.';
  for (final name in context.keys) {
    if (!contextMembers.contains(name)) {
      add('$p$name', 'is not a workspace context member');
    }
  }
  final selected = context['selectedRefs'];
  if (selected != null) {
    if (selected is! List) {
      add('${p}selectedRefs', 'must be an array of references');
    } else {
      if (selected.length > maxSelectedRefs) {
        add('${p}selectedRefs', 'holds more than $maxSelectedRefs references');
      }
      for (var i = 0; i < selected.length; i++) {
        final r = selected[i];
        if (r is! String || !isValidObjectRef(r)) {
          add('${p}selectedRefs[$i]', 'is not a valid object reference');
        }
      }
    }
  }
  final focus = context['focusRef'];
  if (focus != null && (focus is! String || !isValidObjectRef(focus))) {
    add('${p}focusRef', 'is not a valid object reference');
  }
  final range = context['timeRange'];
  if (range != null) {
    if (range is! Map) {
      add('${p}timeRange', 'must be an object {start, end}');
    } else {
      final start = _parseTime(range['start']);
      if (start == null) {
        add('${p}timeRange.start', 'is required (an RFC 3339 date-time)');
      }
      final endValue = range['end'];
      if (endValue != null) {
        final end = _parseTime(endValue);
        if (end == null) {
          add('${p}timeRange.end', 'must be an RFC 3339 date-time or null');
        } else if (start != null && end.isBefore(start)) {
          add('${p}timeRange.end', 'must not be before start');
        }
      }
      for (final name in range.keys) {
        if (name != 'start' && name != 'end') {
          add('${p}timeRange.$name', 'is not a time range member');
        }
      }
    }
  }
  final asOf = context['asOf'];
  if (asOf != null && _parseTime(asOf) == null) {
    add('${p}asOf', 'must be an RFC 3339 date-time');
  }
  final playback = context['playback'];
  if (playback != null) {
    if (playback is! Map) {
      add('${p}playback', 'must be an object {state, rate}');
    } else {
      if (playback['state'] != 'playing' && playback['state'] != 'paused') {
        add('${p}playback.state', 'must be playing or paused');
      }
      final rate = playback['rate'];
      if (rate != null && (rate is! num || rate <= 0)) {
        add('${p}playback.rate', 'must be a number above 0');
      }
    }
  }
  final viewport = context['viewport'];
  if (viewport != null) {
    if (viewport is! Map) {
      add('${p}viewport', 'must be an object');
    } else {
      final hasBbox = viewport['bbox'] != null;
      final hasCenter = viewport['center'] != null || viewport['radiusKm'] != null;
      if (hasBbox == hasCenter) {
        add('${p}viewport', 'must be either bbox or center with radiusKm');
      } else if (hasBbox) {
        final b = viewport['bbox'];
        final ok = b is List &&
            b.length == 4 &&
            b.every((n) => n is num) &&
            (b[1] as num) < (b[3] as num) &&
            (b[0] as num).abs() <= 180 &&
            (b[2] as num).abs() <= 180 &&
            (b[1] as num).abs() <= 90 &&
            (b[3] as num).abs() <= 90;
        if (!ok) {
          add('${p}viewport.bbox', 'must be [minLon, minLat, maxLon, maxLat]');
        }
      } else {
        final c = viewport['center'];
        final ok = c is List &&
            c.length == 2 &&
            c.every((n) => n is num) &&
            (c[0] as num).abs() <= 180 &&
            (c[1] as num).abs() <= 90;
        if (!ok) {
          add('${p}viewport.center', 'must be [lon, lat]');
        }
        final r = viewport['radiusKm'];
        if (r is! num || r <= 0 || r > 20000) {
          add('${p}viewport.radiusKm', 'must be above 0 and at most 20000');
        }
      }
    }
  }
  for (final id in ['activeSituationId', 'activeHypothesisSetId']) {
    final v = context[id];
    if (v != null && (v is! String || !_ulid.hasMatch(v))) {
      add('$p$id', 'must be a ULID or null');
    }
  }
  final filters = context['filters'];
  if (filters != null) {
    if (filters is! Map) {
      add('${p}filters', 'must be an object');
    } else {
      filters.forEach((name, value) {
        if (!_isSimple(value)) {
          add('${p}filters.$name', 'must be a string, number, boolean, null or an array of those');
        }
      });
      if (_utf8Length(filters) > maxFiltersBytes) {
        add('${p}filters', 'is larger than $maxFiltersBytes bytes');
      }
    }
  }
  final layers = context['layers'];
  if (layers != null) {
    if (layers is! List || layers.length > maxLayers) {
      add('${p}layers', 'must be an array of at most $maxLayers strings');
    } else if (layers.any((l) => l is! String || l.trim().isEmpty || l.length > 128)) {
      add('${p}layers', 'each layer is a string of 1 to 128 characters');
    }
  }
  if (_utf8Length(context) > maxContextBytes) {
    add(prefix, 'is larger than $maxContextBytes bytes');
  }
  return errors;
}

/// Validates a context under schema version 1; throws [ContextValidationException] listing every problem.
void validateContext(Object? context, {String prefix = 'context'}) {
  final errors = contextProblems(context, prefix: prefix);
  if (errors.isNotEmpty) {
    throw ContextValidationException(errors);
  }
}
