// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'scorecard_entry_skill.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ScorecardEntrySkillCWProxy {
  ScorecardEntrySkill value(num value);

  ScorecardEntrySkill reference(String reference);

  ScorecardEntrySkill nShared(int nShared);

  ScorecardEntrySkill interval(StatInterval? interval);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ScorecardEntrySkill(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ScorecardEntrySkill(...).copyWith(id: 12, name: "My name")
  /// ```
  ScorecardEntrySkill call({
    num value,
    String reference,
    int nShared,
    StatInterval? interval,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfScorecardEntrySkill.copyWith(...)` or call `instanceOfScorecardEntrySkill.copyWith.fieldName(value)` for a single field.
class _$ScorecardEntrySkillCWProxyImpl implements _$ScorecardEntrySkillCWProxy {
  const _$ScorecardEntrySkillCWProxyImpl(this._value);

  final ScorecardEntrySkill _value;

  @override
  ScorecardEntrySkill value(num value) => call(value: value);

  @override
  ScorecardEntrySkill reference(String reference) => call(reference: reference);

  @override
  ScorecardEntrySkill nShared(int nShared) => call(nShared: nShared);

  @override
  ScorecardEntrySkill interval(StatInterval? interval) =>
      call(interval: interval);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ScorecardEntrySkill(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ScorecardEntrySkill(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ScorecardEntrySkill call({
    Object? value = const $CopyWithPlaceholder(),
    Object? reference = const $CopyWithPlaceholder(),
    Object? nShared = const $CopyWithPlaceholder(),
    Object? interval = const $CopyWithPlaceholder(),
  }) {
    return ScorecardEntrySkill(
      value: value == const $CopyWithPlaceholder() || value == null
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num,
      reference: reference == const $CopyWithPlaceholder() || reference == null
          ? _value.reference
          // ignore: cast_nullable_to_non_nullable
          : reference as String,
      nShared: nShared == const $CopyWithPlaceholder() || nShared == null
          ? _value.nShared
          // ignore: cast_nullable_to_non_nullable
          : nShared as int,
      interval: interval == const $CopyWithPlaceholder()
          ? _value.interval
          // ignore: cast_nullable_to_non_nullable
          : interval as StatInterval?,
    );
  }
}

extension $ScorecardEntrySkillCopyWith on ScorecardEntrySkill {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfScorecardEntrySkill.copyWith(...)` or `instanceOfScorecardEntrySkill.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ScorecardEntrySkillCWProxy get copyWith =>
      _$ScorecardEntrySkillCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ScorecardEntrySkill _$ScorecardEntrySkillFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ScorecardEntrySkill', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['value', 'reference', 'nShared']);
      final val = ScorecardEntrySkill(
        value: $checkedConvert('value', (v) => v as num),
        reference: $checkedConvert('reference', (v) => v as String),
        nShared: $checkedConvert('nShared', (v) => (v as num).toInt()),
        interval: $checkedConvert(
          'interval',
          (v) => v == null
              ? null
              : StatInterval.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ScorecardEntrySkillToJson(
  ScorecardEntrySkill instance,
) => <String, dynamic>{
  'value': instance.value,
  'reference': instance.reference,
  'nShared': instance.nShared,
  'interval': ?instance.interval?.toJson(),
};
