// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'metric_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MetricSettingsCWProxy {
  MetricSettings projections(List<MetricProjection> projections);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MetricSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MetricSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  MetricSettings call({List<MetricProjection> projections});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMetricSettings.copyWith(...)` or call `instanceOfMetricSettings.copyWith.fieldName(value)` for a single field.
class _$MetricSettingsCWProxyImpl implements _$MetricSettingsCWProxy {
  const _$MetricSettingsCWProxyImpl(this._value);

  final MetricSettings _value;

  @override
  MetricSettings projections(List<MetricProjection> projections) =>
      call(projections: projections);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MetricSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MetricSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MetricSettings call({Object? projections = const $CopyWithPlaceholder()}) {
    return MetricSettings(
      projections:
          projections == const $CopyWithPlaceholder() || projections == null
          ? _value.projections
          // ignore: cast_nullable_to_non_nullable
          : projections as List<MetricProjection>,
    );
  }
}

extension $MetricSettingsCopyWith on MetricSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMetricSettings.copyWith(...)` or `instanceOfMetricSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MetricSettingsCWProxy get copyWith => _$MetricSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MetricSettings _$MetricSettingsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MetricSettings', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['projections']);
      final val = MetricSettings(
        projections: $checkedConvert(
          'projections',
          (v) => (v as List<dynamic>)
              .map((e) => MetricProjection.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$MetricSettingsToJson(MetricSettings instance) =>
    <String, dynamic>{
      'projections': instance.projections.map((e) => e.toJson()).toList(),
    };
