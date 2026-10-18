// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'regional_index_value_components_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RegionalIndexValueComponentsInnerCWProxy {
  RegionalIndexValueComponentsInner key(String key);

  RegionalIndexValueComponentsInner raw(num? raw);

  RegionalIndexValueComponentsInner normalised(num normalised);

  RegionalIndexValueComponentsInner weight(num weight);

  RegionalIndexValueComponentsInner contribution(num contribution);

  RegionalIndexValueComponentsInner missing(bool? missing);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalIndexValueComponentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalIndexValueComponentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  RegionalIndexValueComponentsInner call({
    String key,
    num? raw,
    num normalised,
    num weight,
    num contribution,
    bool? missing,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRegionalIndexValueComponentsInner.copyWith(...)` or call `instanceOfRegionalIndexValueComponentsInner.copyWith.fieldName(value)` for a single field.
class _$RegionalIndexValueComponentsInnerCWProxyImpl
    implements _$RegionalIndexValueComponentsInnerCWProxy {
  const _$RegionalIndexValueComponentsInnerCWProxyImpl(this._value);

  final RegionalIndexValueComponentsInner _value;

  @override
  RegionalIndexValueComponentsInner key(String key) => call(key: key);

  @override
  RegionalIndexValueComponentsInner raw(num? raw) => call(raw: raw);

  @override
  RegionalIndexValueComponentsInner normalised(num normalised) =>
      call(normalised: normalised);

  @override
  RegionalIndexValueComponentsInner weight(num weight) => call(weight: weight);

  @override
  RegionalIndexValueComponentsInner contribution(num contribution) =>
      call(contribution: contribution);

  @override
  RegionalIndexValueComponentsInner missing(bool? missing) =>
      call(missing: missing);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalIndexValueComponentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalIndexValueComponentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RegionalIndexValueComponentsInner call({
    Object? key = const $CopyWithPlaceholder(),
    Object? raw = const $CopyWithPlaceholder(),
    Object? normalised = const $CopyWithPlaceholder(),
    Object? weight = const $CopyWithPlaceholder(),
    Object? contribution = const $CopyWithPlaceholder(),
    Object? missing = const $CopyWithPlaceholder(),
  }) {
    return RegionalIndexValueComponentsInner(
      key: key == const $CopyWithPlaceholder() || key == null
          ? _value.key
          // ignore: cast_nullable_to_non_nullable
          : key as String,
      raw: raw == const $CopyWithPlaceholder()
          ? _value.raw
          // ignore: cast_nullable_to_non_nullable
          : raw as num?,
      normalised:
          normalised == const $CopyWithPlaceholder() || normalised == null
          ? _value.normalised
          // ignore: cast_nullable_to_non_nullable
          : normalised as num,
      weight: weight == const $CopyWithPlaceholder() || weight == null
          ? _value.weight
          // ignore: cast_nullable_to_non_nullable
          : weight as num,
      contribution:
          contribution == const $CopyWithPlaceholder() || contribution == null
          ? _value.contribution
          // ignore: cast_nullable_to_non_nullable
          : contribution as num,
      missing: missing == const $CopyWithPlaceholder()
          ? _value.missing
          // ignore: cast_nullable_to_non_nullable
          : missing as bool?,
    );
  }
}

extension $RegionalIndexValueComponentsInnerCopyWith
    on RegionalIndexValueComponentsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRegionalIndexValueComponentsInner.copyWith(...)` or `instanceOfRegionalIndexValueComponentsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RegionalIndexValueComponentsInnerCWProxy get copyWith =>
      _$RegionalIndexValueComponentsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegionalIndexValueComponentsInner _$RegionalIndexValueComponentsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RegionalIndexValueComponentsInner', json, (
  $checkedConvert,
) {
  $checkKeys(
    json,
    requiredKeys: const ['key', 'normalised', 'weight', 'contribution'],
  );
  final val = RegionalIndexValueComponentsInner(
    key: $checkedConvert('key', (v) => v as String),
    raw: $checkedConvert('raw', (v) => v as num?),
    normalised: $checkedConvert('normalised', (v) => v as num),
    weight: $checkedConvert('weight', (v) => v as num),
    contribution: $checkedConvert('contribution', (v) => v as num),
    missing: $checkedConvert('missing', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$RegionalIndexValueComponentsInnerToJson(
  RegionalIndexValueComponentsInner instance,
) => <String, dynamic>{
  'key': instance.key,
  'raw': ?instance.raw,
  'normalised': instance.normalised,
  'weight': instance.weight,
  'contribution': instance.contribution,
  'missing': ?instance.missing,
};
