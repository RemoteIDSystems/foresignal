// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_changes_key_values_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationChangesKeyValuesInnerCWProxy {
  SituationChangesKeyValuesInner ref(String ref);

  SituationChangesKeyValuesInner before(num? before);

  SituationChangesKeyValuesInner after(num? after);

  SituationChangesKeyValuesInner withheld(bool? withheld);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationChangesKeyValuesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationChangesKeyValuesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationChangesKeyValuesInner call({
    String ref,
    num? before,
    num? after,
    bool? withheld,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationChangesKeyValuesInner.copyWith(...)` or call `instanceOfSituationChangesKeyValuesInner.copyWith.fieldName(value)` for a single field.
class _$SituationChangesKeyValuesInnerCWProxyImpl
    implements _$SituationChangesKeyValuesInnerCWProxy {
  const _$SituationChangesKeyValuesInnerCWProxyImpl(this._value);

  final SituationChangesKeyValuesInner _value;

  @override
  SituationChangesKeyValuesInner ref(String ref) => call(ref: ref);

  @override
  SituationChangesKeyValuesInner before(num? before) => call(before: before);

  @override
  SituationChangesKeyValuesInner after(num? after) => call(after: after);

  @override
  SituationChangesKeyValuesInner withheld(bool? withheld) =>
      call(withheld: withheld);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationChangesKeyValuesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationChangesKeyValuesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationChangesKeyValuesInner call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? before = const $CopyWithPlaceholder(),
    Object? after = const $CopyWithPlaceholder(),
    Object? withheld = const $CopyWithPlaceholder(),
  }) {
    return SituationChangesKeyValuesInner(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      before: before == const $CopyWithPlaceholder()
          ? _value.before
          // ignore: cast_nullable_to_non_nullable
          : before as num?,
      after: after == const $CopyWithPlaceholder()
          ? _value.after
          // ignore: cast_nullable_to_non_nullable
          : after as num?,
      withheld: withheld == const $CopyWithPlaceholder()
          ? _value.withheld
          // ignore: cast_nullable_to_non_nullable
          : withheld as bool?,
    );
  }
}

extension $SituationChangesKeyValuesInnerCopyWith
    on SituationChangesKeyValuesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationChangesKeyValuesInner.copyWith(...)` or `instanceOfSituationChangesKeyValuesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationChangesKeyValuesInnerCWProxy get copyWith =>
      _$SituationChangesKeyValuesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationChangesKeyValuesInner _$SituationChangesKeyValuesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationChangesKeyValuesInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['ref']);
  final val = SituationChangesKeyValuesInner(
    ref: $checkedConvert('ref', (v) => v as String),
    before: $checkedConvert('before', (v) => v as num?),
    after: $checkedConvert('after', (v) => v as num?),
    withheld: $checkedConvert('withheld', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$SituationChangesKeyValuesInnerToJson(
  SituationChangesKeyValuesInner instance,
) => <String, dynamic>{
  'ref': instance.ref,
  'before': ?instance.before,
  'after': ?instance.after,
  'withheld': ?instance.withheld,
};
