// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_event_log_entry.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelEventLogEntryCWProxy {
  IntelEventLogEntry at(DateTime at);

  IntelEventLogEntry actor(String actor);

  IntelEventLogEntry action(String action);

  IntelEventLogEntry before(Map<String, Object>? before);

  IntelEventLogEntry after(Map<String, Object>? after);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventLogEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventLogEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelEventLogEntry call({
    DateTime at,
    String actor,
    String action,
    Map<String, Object>? before,
    Map<String, Object>? after,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelEventLogEntry.copyWith(...)` or call `instanceOfIntelEventLogEntry.copyWith.fieldName(value)` for a single field.
class _$IntelEventLogEntryCWProxyImpl implements _$IntelEventLogEntryCWProxy {
  const _$IntelEventLogEntryCWProxyImpl(this._value);

  final IntelEventLogEntry _value;

  @override
  IntelEventLogEntry at(DateTime at) => call(at: at);

  @override
  IntelEventLogEntry actor(String actor) => call(actor: actor);

  @override
  IntelEventLogEntry action(String action) => call(action: action);

  @override
  IntelEventLogEntry before(Map<String, Object>? before) =>
      call(before: before);

  @override
  IntelEventLogEntry after(Map<String, Object>? after) => call(after: after);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventLogEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventLogEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelEventLogEntry call({
    Object? at = const $CopyWithPlaceholder(),
    Object? actor = const $CopyWithPlaceholder(),
    Object? action = const $CopyWithPlaceholder(),
    Object? before = const $CopyWithPlaceholder(),
    Object? after = const $CopyWithPlaceholder(),
  }) {
    return IntelEventLogEntry(
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      actor: actor == const $CopyWithPlaceholder() || actor == null
          ? _value.actor
          // ignore: cast_nullable_to_non_nullable
          : actor as String,
      action: action == const $CopyWithPlaceholder() || action == null
          ? _value.action
          // ignore: cast_nullable_to_non_nullable
          : action as String,
      before: before == const $CopyWithPlaceholder()
          ? _value.before
          // ignore: cast_nullable_to_non_nullable
          : before as Map<String, Object>?,
      after: after == const $CopyWithPlaceholder()
          ? _value.after
          // ignore: cast_nullable_to_non_nullable
          : after as Map<String, Object>?,
    );
  }
}

extension $IntelEventLogEntryCopyWith on IntelEventLogEntry {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelEventLogEntry.copyWith(...)` or `instanceOfIntelEventLogEntry.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelEventLogEntryCWProxy get copyWith =>
      _$IntelEventLogEntryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelEventLogEntry _$IntelEventLogEntryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelEventLogEntry', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['at', 'actor', 'action']);
  final val = IntelEventLogEntry(
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
    actor: $checkedConvert('actor', (v) => v as String),
    action: $checkedConvert('action', (v) => v as String),
    before: $checkedConvert(
      'before',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    after: $checkedConvert(
      'after',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
  );
  return val;
});

Map<String, dynamic> _$IntelEventLogEntryToJson(IntelEventLogEntry instance) =>
    <String, dynamic>{
      'at': instance.at.toIso8601String(),
      'actor': instance.actor,
      'action': instance.action,
      'before': ?instance.before,
      'after': ?instance.after,
    };
