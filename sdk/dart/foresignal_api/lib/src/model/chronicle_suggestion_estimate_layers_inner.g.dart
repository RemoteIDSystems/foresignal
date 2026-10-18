// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_suggestion_estimate_layers_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSuggestionEstimateLayersInnerCWProxy {
  ChronicleSuggestionEstimateLayersInner name(String name);

  ChronicleSuggestionEstimateLayersInner rowsPerDay(num? rowsPerDay);

  ChronicleSuggestionEstimateLayersInner bytesPerDay(num? bytesPerDay);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSuggestionEstimateLayersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSuggestionEstimateLayersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSuggestionEstimateLayersInner call({
    String name,
    num? rowsPerDay,
    num? bytesPerDay,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSuggestionEstimateLayersInner.copyWith(...)` or call `instanceOfChronicleSuggestionEstimateLayersInner.copyWith.fieldName(value)` for a single field.
class _$ChronicleSuggestionEstimateLayersInnerCWProxyImpl
    implements _$ChronicleSuggestionEstimateLayersInnerCWProxy {
  const _$ChronicleSuggestionEstimateLayersInnerCWProxyImpl(this._value);

  final ChronicleSuggestionEstimateLayersInner _value;

  @override
  ChronicleSuggestionEstimateLayersInner name(String name) => call(name: name);

  @override
  ChronicleSuggestionEstimateLayersInner rowsPerDay(num? rowsPerDay) =>
      call(rowsPerDay: rowsPerDay);

  @override
  ChronicleSuggestionEstimateLayersInner bytesPerDay(num? bytesPerDay) =>
      call(bytesPerDay: bytesPerDay);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSuggestionEstimateLayersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSuggestionEstimateLayersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSuggestionEstimateLayersInner call({
    Object? name = const $CopyWithPlaceholder(),
    Object? rowsPerDay = const $CopyWithPlaceholder(),
    Object? bytesPerDay = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSuggestionEstimateLayersInner(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      rowsPerDay: rowsPerDay == const $CopyWithPlaceholder()
          ? _value.rowsPerDay
          // ignore: cast_nullable_to_non_nullable
          : rowsPerDay as num?,
      bytesPerDay: bytesPerDay == const $CopyWithPlaceholder()
          ? _value.bytesPerDay
          // ignore: cast_nullable_to_non_nullable
          : bytesPerDay as num?,
    );
  }
}

extension $ChronicleSuggestionEstimateLayersInnerCopyWith
    on ChronicleSuggestionEstimateLayersInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSuggestionEstimateLayersInner.copyWith(...)` or `instanceOfChronicleSuggestionEstimateLayersInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSuggestionEstimateLayersInnerCWProxy get copyWith =>
      _$ChronicleSuggestionEstimateLayersInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSuggestionEstimateLayersInner
_$ChronicleSuggestionEstimateLayersInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChronicleSuggestionEstimateLayersInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['name']);
      final val = ChronicleSuggestionEstimateLayersInner(
        name: $checkedConvert('name', (v) => v as String),
        rowsPerDay: $checkedConvert('rowsPerDay', (v) => v as num?),
        bytesPerDay: $checkedConvert('bytesPerDay', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleSuggestionEstimateLayersInnerToJson(
  ChronicleSuggestionEstimateLayersInner instance,
) => <String, dynamic>{
  'name': instance.name,
  'rowsPerDay': ?instance.rowsPerDay,
  'bytesPerDay': ?instance.bytesPerDay,
};
