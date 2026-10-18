// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'regional_index_def_floors_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RegionalIndexDefFloorsInnerCWProxy {
  RegionalIndexDefFloorsInner id(String? id);

  RegionalIndexDefFloorsInner description(String description);

  RegionalIndexDefFloorsInner condition(Map<String, Object>? condition);

  RegionalIndexDefFloorsInner minValue(num minValue);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalIndexDefFloorsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalIndexDefFloorsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  RegionalIndexDefFloorsInner call({
    String? id,
    String description,
    Map<String, Object>? condition,
    num minValue,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRegionalIndexDefFloorsInner.copyWith(...)` or call `instanceOfRegionalIndexDefFloorsInner.copyWith.fieldName(value)` for a single field.
class _$RegionalIndexDefFloorsInnerCWProxyImpl
    implements _$RegionalIndexDefFloorsInnerCWProxy {
  const _$RegionalIndexDefFloorsInnerCWProxyImpl(this._value);

  final RegionalIndexDefFloorsInner _value;

  @override
  RegionalIndexDefFloorsInner id(String? id) => call(id: id);

  @override
  RegionalIndexDefFloorsInner description(String description) =>
      call(description: description);

  @override
  RegionalIndexDefFloorsInner condition(Map<String, Object>? condition) =>
      call(condition: condition);

  @override
  RegionalIndexDefFloorsInner minValue(num minValue) =>
      call(minValue: minValue);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalIndexDefFloorsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalIndexDefFloorsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RegionalIndexDefFloorsInner call({
    Object? id = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? condition = const $CopyWithPlaceholder(),
    Object? minValue = const $CopyWithPlaceholder(),
  }) {
    return RegionalIndexDefFloorsInner(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      description:
          description == const $CopyWithPlaceholder() || description == null
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String,
      condition: condition == const $CopyWithPlaceholder()
          ? _value.condition
          // ignore: cast_nullable_to_non_nullable
          : condition as Map<String, Object>?,
      minValue: minValue == const $CopyWithPlaceholder() || minValue == null
          ? _value.minValue
          // ignore: cast_nullable_to_non_nullable
          : minValue as num,
    );
  }
}

extension $RegionalIndexDefFloorsInnerCopyWith on RegionalIndexDefFloorsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRegionalIndexDefFloorsInner.copyWith(...)` or `instanceOfRegionalIndexDefFloorsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RegionalIndexDefFloorsInnerCWProxy get copyWith =>
      _$RegionalIndexDefFloorsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegionalIndexDefFloorsInner _$RegionalIndexDefFloorsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RegionalIndexDefFloorsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['description', 'minValue']);
  final val = RegionalIndexDefFloorsInner(
    id: $checkedConvert('id', (v) => v as String?),
    description: $checkedConvert('description', (v) => v as String),
    condition: $checkedConvert(
      'condition',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    minValue: $checkedConvert('minValue', (v) => v as num),
  );
  return val;
});

Map<String, dynamic> _$RegionalIndexDefFloorsInnerToJson(
  RegionalIndexDefFloorsInner instance,
) => <String, dynamic>{
  'id': ?instance.id,
  'description': instance.description,
  'condition': ?instance.condition,
  'minValue': instance.minValue,
};
