// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'regional_index_def_components_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RegionalIndexDefComponentsInnerCWProxy {
  RegionalIndexDefComponentsInner key(String key);

  RegionalIndexDefComponentsInner ref(String ref);

  RegionalIndexDefComponentsInner transform(
    RegionalIndexDefComponentsInnerTransformEnum transform,
  );

  RegionalIndexDefComponentsInner window(
    RegionalIndexDefComponentsInnerWindowEnum window,
  );

  RegionalIndexDefComponentsInner direction(
    RegionalIndexDefComponentsInnerDirectionEnum direction,
  );

  RegionalIndexDefComponentsInner floor(num? floor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalIndexDefComponentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalIndexDefComponentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  RegionalIndexDefComponentsInner call({
    String key,
    String ref,
    RegionalIndexDefComponentsInnerTransformEnum transform,
    RegionalIndexDefComponentsInnerWindowEnum window,
    RegionalIndexDefComponentsInnerDirectionEnum direction,
    num? floor,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRegionalIndexDefComponentsInner.copyWith(...)` or call `instanceOfRegionalIndexDefComponentsInner.copyWith.fieldName(value)` for a single field.
class _$RegionalIndexDefComponentsInnerCWProxyImpl
    implements _$RegionalIndexDefComponentsInnerCWProxy {
  const _$RegionalIndexDefComponentsInnerCWProxyImpl(this._value);

  final RegionalIndexDefComponentsInner _value;

  @override
  RegionalIndexDefComponentsInner key(String key) => call(key: key);

  @override
  RegionalIndexDefComponentsInner ref(String ref) => call(ref: ref);

  @override
  RegionalIndexDefComponentsInner transform(
    RegionalIndexDefComponentsInnerTransformEnum transform,
  ) => call(transform: transform);

  @override
  RegionalIndexDefComponentsInner window(
    RegionalIndexDefComponentsInnerWindowEnum window,
  ) => call(window: window);

  @override
  RegionalIndexDefComponentsInner direction(
    RegionalIndexDefComponentsInnerDirectionEnum direction,
  ) => call(direction: direction);

  @override
  RegionalIndexDefComponentsInner floor(num? floor) => call(floor: floor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalIndexDefComponentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalIndexDefComponentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RegionalIndexDefComponentsInner call({
    Object? key = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? transform = const $CopyWithPlaceholder(),
    Object? window = const $CopyWithPlaceholder(),
    Object? direction = const $CopyWithPlaceholder(),
    Object? floor = const $CopyWithPlaceholder(),
  }) {
    return RegionalIndexDefComponentsInner(
      key: key == const $CopyWithPlaceholder() || key == null
          ? _value.key
          // ignore: cast_nullable_to_non_nullable
          : key as String,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      transform: transform == const $CopyWithPlaceholder() || transform == null
          ? _value.transform
          // ignore: cast_nullable_to_non_nullable
          : transform as RegionalIndexDefComponentsInnerTransformEnum,
      window: window == const $CopyWithPlaceholder() || window == null
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as RegionalIndexDefComponentsInnerWindowEnum,
      direction: direction == const $CopyWithPlaceholder() || direction == null
          ? _value.direction
          // ignore: cast_nullable_to_non_nullable
          : direction as RegionalIndexDefComponentsInnerDirectionEnum,
      floor: floor == const $CopyWithPlaceholder()
          ? _value.floor
          // ignore: cast_nullable_to_non_nullable
          : floor as num?,
    );
  }
}

extension $RegionalIndexDefComponentsInnerCopyWith
    on RegionalIndexDefComponentsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRegionalIndexDefComponentsInner.copyWith(...)` or `instanceOfRegionalIndexDefComponentsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RegionalIndexDefComponentsInnerCWProxy get copyWith =>
      _$RegionalIndexDefComponentsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegionalIndexDefComponentsInner _$RegionalIndexDefComponentsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RegionalIndexDefComponentsInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['key', 'ref', 'transform', 'window', 'direction'],
  );
  final val = RegionalIndexDefComponentsInner(
    key: $checkedConvert('key', (v) => v as String),
    ref: $checkedConvert('ref', (v) => v as String),
    transform: $checkedConvert(
      'transform',
      (v) =>
          $enumDecode(_$RegionalIndexDefComponentsInnerTransformEnumEnumMap, v),
    ),
    window: $checkedConvert(
      'window',
      (v) => $enumDecode(_$RegionalIndexDefComponentsInnerWindowEnumEnumMap, v),
    ),
    direction: $checkedConvert(
      'direction',
      (v) =>
          $enumDecode(_$RegionalIndexDefComponentsInnerDirectionEnumEnumMap, v),
    ),
    floor: $checkedConvert('floor', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$RegionalIndexDefComponentsInnerToJson(
  RegionalIndexDefComponentsInner instance,
) => <String, dynamic>{
  'key': instance.key,
  'ref': instance.ref,
  'transform':
      _$RegionalIndexDefComponentsInnerTransformEnumEnumMap[instance
          .transform]!,
  'window':
      _$RegionalIndexDefComponentsInnerWindowEnumEnumMap[instance.window]!,
  'direction':
      _$RegionalIndexDefComponentsInnerDirectionEnumEnumMap[instance
          .direction]!,
  'floor': ?instance.floor,
};

const _$RegionalIndexDefComponentsInnerTransformEnumEnumMap = {
  RegionalIndexDefComponentsInnerTransformEnum.log1p: 'log1p',
  RegionalIndexDefComponentsInnerTransformEnum.zscore: 'zscore',
  RegionalIndexDefComponentsInnerTransformEnum.pctrank: 'pctrank',
  RegionalIndexDefComponentsInnerTransformEnum.level: 'level',
};

const _$RegionalIndexDefComponentsInnerWindowEnumEnumMap = {
  RegionalIndexDefComponentsInnerWindowEnum.P7D: 'P7D',
  RegionalIndexDefComponentsInnerWindowEnum.P28D: 'P28D',
  RegionalIndexDefComponentsInnerWindowEnum.P90D: 'P90D',
};

const _$RegionalIndexDefComponentsInnerDirectionEnumEnumMap = {
  RegionalIndexDefComponentsInnerDirectionEnum.number1: 1,
  RegionalIndexDefComponentsInnerDirectionEnum.numberNegative1: -1,
};
