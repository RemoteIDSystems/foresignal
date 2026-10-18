// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'narrative_log_entry.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NarrativeLogEntryCWProxy {
  NarrativeLogEntry at(DateTime at);

  NarrativeLogEntry action(NarrativeLogEntryActionEnum action);

  NarrativeLogEntry by(String by);

  NarrativeLogEntry relatedNarrativeIds(List<String>? relatedNarrativeIds);

  NarrativeLogEntry from(String? from);

  NarrativeLogEntry to(String? to);

  NarrativeLogEntry reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativeLogEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativeLogEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  NarrativeLogEntry call({
    DateTime at,
    NarrativeLogEntryActionEnum action,
    String by,
    List<String>? relatedNarrativeIds,
    String? from,
    String? to,
    String? reason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNarrativeLogEntry.copyWith(...)` or call `instanceOfNarrativeLogEntry.copyWith.fieldName(value)` for a single field.
class _$NarrativeLogEntryCWProxyImpl implements _$NarrativeLogEntryCWProxy {
  const _$NarrativeLogEntryCWProxyImpl(this._value);

  final NarrativeLogEntry _value;

  @override
  NarrativeLogEntry at(DateTime at) => call(at: at);

  @override
  NarrativeLogEntry action(NarrativeLogEntryActionEnum action) =>
      call(action: action);

  @override
  NarrativeLogEntry by(String by) => call(by: by);

  @override
  NarrativeLogEntry relatedNarrativeIds(List<String>? relatedNarrativeIds) =>
      call(relatedNarrativeIds: relatedNarrativeIds);

  @override
  NarrativeLogEntry from(String? from) => call(from: from);

  @override
  NarrativeLogEntry to(String? to) => call(to: to);

  @override
  NarrativeLogEntry reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativeLogEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativeLogEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NarrativeLogEntry call({
    Object? at = const $CopyWithPlaceholder(),
    Object? action = const $CopyWithPlaceholder(),
    Object? by = const $CopyWithPlaceholder(),
    Object? relatedNarrativeIds = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return NarrativeLogEntry(
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      action: action == const $CopyWithPlaceholder() || action == null
          ? _value.action
          // ignore: cast_nullable_to_non_nullable
          : action as NarrativeLogEntryActionEnum,
      by: by == const $CopyWithPlaceholder() || by == null
          ? _value.by
          // ignore: cast_nullable_to_non_nullable
          : by as String,
      relatedNarrativeIds: relatedNarrativeIds == const $CopyWithPlaceholder()
          ? _value.relatedNarrativeIds
          // ignore: cast_nullable_to_non_nullable
          : relatedNarrativeIds as List<String>?,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as String?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as String?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $NarrativeLogEntryCopyWith on NarrativeLogEntry {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNarrativeLogEntry.copyWith(...)` or `instanceOfNarrativeLogEntry.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NarrativeLogEntryCWProxy get copyWith =>
      _$NarrativeLogEntryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NarrativeLogEntry _$NarrativeLogEntryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NarrativeLogEntry', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['at', 'action', 'by']);
      final val = NarrativeLogEntry(
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
        action: $checkedConvert(
          'action',
          (v) => $enumDecode(_$NarrativeLogEntryActionEnumEnumMap, v),
        ),
        by: $checkedConvert('by', (v) => v as String),
        relatedNarrativeIds: $checkedConvert(
          'relatedNarrativeIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        from: $checkedConvert('from', (v) => v as String?),
        to: $checkedConvert('to', (v) => v as String?),
        reason: $checkedConvert('reason', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$NarrativeLogEntryToJson(NarrativeLogEntry instance) =>
    <String, dynamic>{
      'at': instance.at.toIso8601String(),
      'action': _$NarrativeLogEntryActionEnumEnumMap[instance.action]!,
      'by': instance.by,
      'relatedNarrativeIds': ?instance.relatedNarrativeIds,
      'from': ?instance.from,
      'to': ?instance.to,
      'reason': ?instance.reason,
    };

const _$NarrativeLogEntryActionEnumEnumMap = {
  NarrativeLogEntryActionEnum.created: 'created',
  NarrativeLogEntryActionEnum.merged: 'merged',
  NarrativeLogEntryActionEnum.mergedInto: 'merged-into',
  NarrativeLogEntryActionEnum.split: 'split',
  NarrativeLogEntryActionEnum.splitFrom: 'split-from',
  NarrativeLogEntryActionEnum.stateChanged: 'state-changed',
  NarrativeLogEntryActionEnum.statusChanged: 'status-changed',
  NarrativeLogEntryActionEnum.titleChanged: 'title-changed',
};
