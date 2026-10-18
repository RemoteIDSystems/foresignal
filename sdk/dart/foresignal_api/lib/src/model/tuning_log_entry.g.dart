// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tuning_log_entry.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TuningLogEntryCWProxy {
  TuningLogEntry id(String id);

  TuningLogEntry ruleId(String ruleId);

  TuningLogEntry suggestionId(String? suggestionId);

  TuningLogEntry before(TriggerRuleInput before);

  TuningLogEntry after(TriggerRuleInput after);

  TuningLogEntry note(String? note);

  TuningLogEntry appliedBy(String appliedBy);

  TuningLogEntry appliedAt(DateTime appliedAt);

  TuningLogEntry undoneBy(String? undoneBy);

  TuningLogEntry undoneAt(DateTime? undoneAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TuningLogEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TuningLogEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  TuningLogEntry call({
    String id,
    String ruleId,
    String? suggestionId,
    TriggerRuleInput before,
    TriggerRuleInput after,
    String? note,
    String appliedBy,
    DateTime appliedAt,
    String? undoneBy,
    DateTime? undoneAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTuningLogEntry.copyWith(...)` or call `instanceOfTuningLogEntry.copyWith.fieldName(value)` for a single field.
class _$TuningLogEntryCWProxyImpl implements _$TuningLogEntryCWProxy {
  const _$TuningLogEntryCWProxyImpl(this._value);

  final TuningLogEntry _value;

  @override
  TuningLogEntry id(String id) => call(id: id);

  @override
  TuningLogEntry ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  TuningLogEntry suggestionId(String? suggestionId) =>
      call(suggestionId: suggestionId);

  @override
  TuningLogEntry before(TriggerRuleInput before) => call(before: before);

  @override
  TuningLogEntry after(TriggerRuleInput after) => call(after: after);

  @override
  TuningLogEntry note(String? note) => call(note: note);

  @override
  TuningLogEntry appliedBy(String appliedBy) => call(appliedBy: appliedBy);

  @override
  TuningLogEntry appliedAt(DateTime appliedAt) => call(appliedAt: appliedAt);

  @override
  TuningLogEntry undoneBy(String? undoneBy) => call(undoneBy: undoneBy);

  @override
  TuningLogEntry undoneAt(DateTime? undoneAt) => call(undoneAt: undoneAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TuningLogEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TuningLogEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TuningLogEntry call({
    Object? id = const $CopyWithPlaceholder(),
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? suggestionId = const $CopyWithPlaceholder(),
    Object? before = const $CopyWithPlaceholder(),
    Object? after = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
    Object? appliedBy = const $CopyWithPlaceholder(),
    Object? appliedAt = const $CopyWithPlaceholder(),
    Object? undoneBy = const $CopyWithPlaceholder(),
    Object? undoneAt = const $CopyWithPlaceholder(),
  }) {
    return TuningLogEntry(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      ruleId: ruleId == const $CopyWithPlaceholder() || ruleId == null
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String,
      suggestionId: suggestionId == const $CopyWithPlaceholder()
          ? _value.suggestionId
          // ignore: cast_nullable_to_non_nullable
          : suggestionId as String?,
      before: before == const $CopyWithPlaceholder() || before == null
          ? _value.before
          // ignore: cast_nullable_to_non_nullable
          : before as TriggerRuleInput,
      after: after == const $CopyWithPlaceholder() || after == null
          ? _value.after
          // ignore: cast_nullable_to_non_nullable
          : after as TriggerRuleInput,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
      appliedBy: appliedBy == const $CopyWithPlaceholder() || appliedBy == null
          ? _value.appliedBy
          // ignore: cast_nullable_to_non_nullable
          : appliedBy as String,
      appliedAt: appliedAt == const $CopyWithPlaceholder() || appliedAt == null
          ? _value.appliedAt
          // ignore: cast_nullable_to_non_nullable
          : appliedAt as DateTime,
      undoneBy: undoneBy == const $CopyWithPlaceholder()
          ? _value.undoneBy
          // ignore: cast_nullable_to_non_nullable
          : undoneBy as String?,
      undoneAt: undoneAt == const $CopyWithPlaceholder()
          ? _value.undoneAt
          // ignore: cast_nullable_to_non_nullable
          : undoneAt as DateTime?,
    );
  }
}

extension $TuningLogEntryCopyWith on TuningLogEntry {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTuningLogEntry.copyWith(...)` or `instanceOfTuningLogEntry.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TuningLogEntryCWProxy get copyWith => _$TuningLogEntryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TuningLogEntry _$TuningLogEntryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TuningLogEntry', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'ruleId',
          'before',
          'after',
          'appliedBy',
          'appliedAt',
        ],
      );
      final val = TuningLogEntry(
        id: $checkedConvert('id', (v) => v as String),
        ruleId: $checkedConvert('ruleId', (v) => v as String),
        suggestionId: $checkedConvert('suggestionId', (v) => v as String?),
        before: $checkedConvert(
          'before',
          (v) => TriggerRuleInput.fromJson(v as Map<String, dynamic>),
        ),
        after: $checkedConvert(
          'after',
          (v) => TriggerRuleInput.fromJson(v as Map<String, dynamic>),
        ),
        note: $checkedConvert('note', (v) => v as String?),
        appliedBy: $checkedConvert('appliedBy', (v) => v as String),
        appliedAt: $checkedConvert(
          'appliedAt',
          (v) => DateTime.parse(v as String),
        ),
        undoneBy: $checkedConvert('undoneBy', (v) => v as String?),
        undoneAt: $checkedConvert(
          'undoneAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$TuningLogEntryToJson(TuningLogEntry instance) =>
    <String, dynamic>{
      'id': instance.id,
      'ruleId': instance.ruleId,
      'suggestionId': ?instance.suggestionId,
      'before': instance.before.toJson(),
      'after': instance.after.toJson(),
      'note': ?instance.note,
      'appliedBy': instance.appliedBy,
      'appliedAt': instance.appliedAt.toIso8601String(),
      'undoneBy': ?instance.undoneBy,
      'undoneAt': ?instance.undoneAt?.toIso8601String(),
    };
