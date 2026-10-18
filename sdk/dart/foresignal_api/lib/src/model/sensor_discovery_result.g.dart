// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sensor_discovery_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SensorDiscoveryResultCWProxy {
  SensorDiscoveryResult bbox(List<num>? bbox);

  SensorDiscoveryResult placeId(String? placeId);

  SensorDiscoveryResult from(DateTime from);

  SensorDiscoveryResult to(DateTime to);

  SensorDiscoveryResult items(List<Sensor> items);

  SensorDiscoveryResult truncated(bool? truncated);

  SensorDiscoveryResult note(String? note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SensorDiscoveryResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SensorDiscoveryResult(...).copyWith(id: 12, name: "My name")
  /// ```
  SensorDiscoveryResult call({
    List<num>? bbox,
    String? placeId,
    DateTime from,
    DateTime to,
    List<Sensor> items,
    bool? truncated,
    String? note,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSensorDiscoveryResult.copyWith(...)` or call `instanceOfSensorDiscoveryResult.copyWith.fieldName(value)` for a single field.
class _$SensorDiscoveryResultCWProxyImpl
    implements _$SensorDiscoveryResultCWProxy {
  const _$SensorDiscoveryResultCWProxyImpl(this._value);

  final SensorDiscoveryResult _value;

  @override
  SensorDiscoveryResult bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  SensorDiscoveryResult placeId(String? placeId) => call(placeId: placeId);

  @override
  SensorDiscoveryResult from(DateTime from) => call(from: from);

  @override
  SensorDiscoveryResult to(DateTime to) => call(to: to);

  @override
  SensorDiscoveryResult items(List<Sensor> items) => call(items: items);

  @override
  SensorDiscoveryResult truncated(bool? truncated) =>
      call(truncated: truncated);

  @override
  SensorDiscoveryResult note(String? note) => call(note: note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SensorDiscoveryResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SensorDiscoveryResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SensorDiscoveryResult call({
    Object? bbox = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
  }) {
    return SensorDiscoveryResult(
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Sensor>,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
    );
  }
}

extension $SensorDiscoveryResultCopyWith on SensorDiscoveryResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSensorDiscoveryResult.copyWith(...)` or `instanceOfSensorDiscoveryResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SensorDiscoveryResultCWProxy get copyWith =>
      _$SensorDiscoveryResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SensorDiscoveryResult _$SensorDiscoveryResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SensorDiscoveryResult', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['from', 'to', 'items']);
  final val = SensorDiscoveryResult(
    bbox: $checkedConvert(
      'bbox',
      (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
    ),
    placeId: $checkedConvert('placeId', (v) => v as String?),
    from: $checkedConvert('from', (v) => DateTime.parse(v as String)),
    to: $checkedConvert('to', (v) => DateTime.parse(v as String)),
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => Sensor.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    truncated: $checkedConvert('truncated', (v) => v as bool?),
    note: $checkedConvert(
      'note',
      (v) =>
          v as String? ??
          'Foresignal cannot task public satellites or third-party sensors',
    ),
  );
  return val;
});

Map<String, dynamic> _$SensorDiscoveryResultToJson(
  SensorDiscoveryResult instance,
) => <String, dynamic>{
  'bbox': ?instance.bbox,
  'placeId': ?instance.placeId,
  'from': instance.from.toIso8601String(),
  'to': instance.to.toIso8601String(),
  'items': instance.items.map((e) => e.toJson()).toList(),
  'truncated': ?instance.truncated,
  'note': ?instance.note,
};
