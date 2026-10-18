// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'uv_forecast_payload_hours_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UvForecastPayloadHoursInnerCWProxy {
  UvForecastPayloadHoursInner time(DateTime time);

  UvForecastPayloadHoursInner uv(num uv);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UvForecastPayloadHoursInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UvForecastPayloadHoursInner(...).copyWith(id: 12, name: "My name")
  /// ```
  UvForecastPayloadHoursInner call({DateTime time, num uv});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfUvForecastPayloadHoursInner.copyWith(...)` or call `instanceOfUvForecastPayloadHoursInner.copyWith.fieldName(value)` for a single field.
class _$UvForecastPayloadHoursInnerCWProxyImpl
    implements _$UvForecastPayloadHoursInnerCWProxy {
  const _$UvForecastPayloadHoursInnerCWProxyImpl(this._value);

  final UvForecastPayloadHoursInner _value;

  @override
  UvForecastPayloadHoursInner time(DateTime time) => call(time: time);

  @override
  UvForecastPayloadHoursInner uv(num uv) => call(uv: uv);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UvForecastPayloadHoursInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UvForecastPayloadHoursInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  UvForecastPayloadHoursInner call({
    Object? time = const $CopyWithPlaceholder(),
    Object? uv = const $CopyWithPlaceholder(),
  }) {
    return UvForecastPayloadHoursInner(
      time: time == const $CopyWithPlaceholder() || time == null
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as DateTime,
      uv: uv == const $CopyWithPlaceholder() || uv == null
          ? _value.uv
          // ignore: cast_nullable_to_non_nullable
          : uv as num,
    );
  }
}

extension $UvForecastPayloadHoursInnerCopyWith on UvForecastPayloadHoursInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfUvForecastPayloadHoursInner.copyWith(...)` or `instanceOfUvForecastPayloadHoursInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UvForecastPayloadHoursInnerCWProxy get copyWith =>
      _$UvForecastPayloadHoursInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UvForecastPayloadHoursInner _$UvForecastPayloadHoursInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('UvForecastPayloadHoursInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['time', 'uv']);
  final val = UvForecastPayloadHoursInner(
    time: $checkedConvert('time', (v) => DateTime.parse(v as String)),
    uv: $checkedConvert('uv', (v) => v as num),
  );
  return val;
});

Map<String, dynamic> _$UvForecastPayloadHoursInnerToJson(
  UvForecastPayloadHoursInner instance,
) => <String, dynamic>{
  'time': instance.time.toIso8601String(),
  'uv': instance.uv,
};
