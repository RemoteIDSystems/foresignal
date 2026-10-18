// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_changes_severity.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationChangesSeverityCWProxy {
  SituationChangesSeverity before(Severity? before);

  SituationChangesSeverity after(Severity after);

  SituationChangesSeverity changed(bool changed);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationChangesSeverity(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationChangesSeverity(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationChangesSeverity call({
    Severity? before,
    Severity after,
    bool changed,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationChangesSeverity.copyWith(...)` or call `instanceOfSituationChangesSeverity.copyWith.fieldName(value)` for a single field.
class _$SituationChangesSeverityCWProxyImpl
    implements _$SituationChangesSeverityCWProxy {
  const _$SituationChangesSeverityCWProxyImpl(this._value);

  final SituationChangesSeverity _value;

  @override
  SituationChangesSeverity before(Severity? before) => call(before: before);

  @override
  SituationChangesSeverity after(Severity after) => call(after: after);

  @override
  SituationChangesSeverity changed(bool changed) => call(changed: changed);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationChangesSeverity(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationChangesSeverity(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationChangesSeverity call({
    Object? before = const $CopyWithPlaceholder(),
    Object? after = const $CopyWithPlaceholder(),
    Object? changed = const $CopyWithPlaceholder(),
  }) {
    return SituationChangesSeverity(
      before: before == const $CopyWithPlaceholder()
          ? _value.before
          // ignore: cast_nullable_to_non_nullable
          : before as Severity?,
      after: after == const $CopyWithPlaceholder() || after == null
          ? _value.after
          // ignore: cast_nullable_to_non_nullable
          : after as Severity,
      changed: changed == const $CopyWithPlaceholder() || changed == null
          ? _value.changed
          // ignore: cast_nullable_to_non_nullable
          : changed as bool,
    );
  }
}

extension $SituationChangesSeverityCopyWith on SituationChangesSeverity {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationChangesSeverity.copyWith(...)` or `instanceOfSituationChangesSeverity.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationChangesSeverityCWProxy get copyWith =>
      _$SituationChangesSeverityCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationChangesSeverity _$SituationChangesSeverityFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationChangesSeverity', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['after', 'changed']);
  final val = SituationChangesSeverity(
    before: $checkedConvert(
      'before',
      (v) => $enumDecodeNullable(_$SeverityEnumMap, v),
    ),
    after: $checkedConvert('after', (v) => $enumDecode(_$SeverityEnumMap, v)),
    changed: $checkedConvert('changed', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$SituationChangesSeverityToJson(
  SituationChangesSeverity instance,
) => <String, dynamic>{
  'before': ?_$SeverityEnumMap[instance.before],
  'after': _$SeverityEnumMap[instance.after]!,
  'changed': instance.changed,
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};
