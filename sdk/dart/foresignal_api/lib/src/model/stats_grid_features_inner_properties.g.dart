// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stats_grid_features_inner_properties.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StatsGridFeaturesInnerPropertiesCWProxy {
  StatsGridFeaturesInnerProperties cell(String? cell);

  StatsGridFeaturesInnerProperties value(int value);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsGridFeaturesInnerProperties(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsGridFeaturesInnerProperties(...).copyWith(id: 12, name: "My name")
  /// ```
  StatsGridFeaturesInnerProperties call({String? cell, int value});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStatsGridFeaturesInnerProperties.copyWith(...)` or call `instanceOfStatsGridFeaturesInnerProperties.copyWith.fieldName(value)` for a single field.
class _$StatsGridFeaturesInnerPropertiesCWProxyImpl
    implements _$StatsGridFeaturesInnerPropertiesCWProxy {
  const _$StatsGridFeaturesInnerPropertiesCWProxyImpl(this._value);

  final StatsGridFeaturesInnerProperties _value;

  @override
  StatsGridFeaturesInnerProperties cell(String? cell) => call(cell: cell);

  @override
  StatsGridFeaturesInnerProperties value(int value) => call(value: value);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsGridFeaturesInnerProperties(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsGridFeaturesInnerProperties(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StatsGridFeaturesInnerProperties call({
    Object? cell = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
  }) {
    return StatsGridFeaturesInnerProperties(
      cell: cell == const $CopyWithPlaceholder()
          ? _value.cell
          // ignore: cast_nullable_to_non_nullable
          : cell as String?,
      value: value == const $CopyWithPlaceholder() || value == null
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as int,
    );
  }
}

extension $StatsGridFeaturesInnerPropertiesCopyWith
    on StatsGridFeaturesInnerProperties {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStatsGridFeaturesInnerProperties.copyWith(...)` or `instanceOfStatsGridFeaturesInnerProperties.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StatsGridFeaturesInnerPropertiesCWProxy get copyWith =>
      _$StatsGridFeaturesInnerPropertiesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StatsGridFeaturesInnerProperties _$StatsGridFeaturesInnerPropertiesFromJson(
  Map<String, dynamic> json,
) =>
    $checkedCreate('StatsGridFeaturesInnerProperties', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['value']);
      final val = StatsGridFeaturesInnerProperties(
        cell: $checkedConvert('cell', (v) => v as String?),
        value: $checkedConvert('value', (v) => (v as num).toInt()),
      );
      return val;
    });

Map<String, dynamic> _$StatsGridFeaturesInnerPropertiesToJson(
  StatsGridFeaturesInnerProperties instance,
) => <String, dynamic>{'cell': ?instance.cell, 'value': instance.value};
