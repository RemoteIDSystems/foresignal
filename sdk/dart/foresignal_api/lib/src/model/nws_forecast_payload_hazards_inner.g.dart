// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'nws_forecast_payload_hazards_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NwsForecastPayloadHazardsInnerCWProxy {
  NwsForecastPayloadHazardsInner type(String type);

  NwsForecastPayloadHazardsInner validTime(String? validTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NwsForecastPayloadHazardsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NwsForecastPayloadHazardsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  NwsForecastPayloadHazardsInner call({String type, String? validTime});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNwsForecastPayloadHazardsInner.copyWith(...)` or call `instanceOfNwsForecastPayloadHazardsInner.copyWith.fieldName(value)` for a single field.
class _$NwsForecastPayloadHazardsInnerCWProxyImpl
    implements _$NwsForecastPayloadHazardsInnerCWProxy {
  const _$NwsForecastPayloadHazardsInnerCWProxyImpl(this._value);

  final NwsForecastPayloadHazardsInner _value;

  @override
  NwsForecastPayloadHazardsInner type(String type) => call(type: type);

  @override
  NwsForecastPayloadHazardsInner validTime(String? validTime) =>
      call(validTime: validTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NwsForecastPayloadHazardsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NwsForecastPayloadHazardsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NwsForecastPayloadHazardsInner call({
    Object? type = const $CopyWithPlaceholder(),
    Object? validTime = const $CopyWithPlaceholder(),
  }) {
    return NwsForecastPayloadHazardsInner(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as String,
      validTime: validTime == const $CopyWithPlaceholder()
          ? _value.validTime
          // ignore: cast_nullable_to_non_nullable
          : validTime as String?,
    );
  }
}

extension $NwsForecastPayloadHazardsInnerCopyWith
    on NwsForecastPayloadHazardsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNwsForecastPayloadHazardsInner.copyWith(...)` or `instanceOfNwsForecastPayloadHazardsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NwsForecastPayloadHazardsInnerCWProxy get copyWith =>
      _$NwsForecastPayloadHazardsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NwsForecastPayloadHazardsInner _$NwsForecastPayloadHazardsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NwsForecastPayloadHazardsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type']);
  final val = NwsForecastPayloadHazardsInner(
    type: $checkedConvert('type', (v) => v as String),
    validTime: $checkedConvert('validTime', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$NwsForecastPayloadHazardsInnerToJson(
  NwsForecastPayloadHazardsInner instance,
) => <String, dynamic>{'type': instance.type, 'validTime': ?instance.validTime};
