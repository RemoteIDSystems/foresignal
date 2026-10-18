// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'indicator_signal_intel_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IndicatorSignalIntelEventCWProxy {
  IndicatorSignalIntelEvent type(String type);

  IndicatorSignalIntelEvent regionCodes(List<String>? regionCodes);

  IndicatorSignalIntelEvent placeId(String? placeId);

  IndicatorSignalIntelEvent bbox(String? bbox);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorSignalIntelEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorSignalIntelEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  IndicatorSignalIntelEvent call({
    String type,
    List<String>? regionCodes,
    String? placeId,
    String? bbox,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIndicatorSignalIntelEvent.copyWith(...)` or call `instanceOfIndicatorSignalIntelEvent.copyWith.fieldName(value)` for a single field.
class _$IndicatorSignalIntelEventCWProxyImpl
    implements _$IndicatorSignalIntelEventCWProxy {
  const _$IndicatorSignalIntelEventCWProxyImpl(this._value);

  final IndicatorSignalIntelEvent _value;

  @override
  IndicatorSignalIntelEvent type(String type) => call(type: type);

  @override
  IndicatorSignalIntelEvent regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  IndicatorSignalIntelEvent placeId(String? placeId) => call(placeId: placeId);

  @override
  IndicatorSignalIntelEvent bbox(String? bbox) => call(bbox: bbox);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorSignalIntelEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorSignalIntelEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IndicatorSignalIntelEvent call({
    Object? type = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
  }) {
    return IndicatorSignalIntelEvent(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as String,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as String?,
    );
  }
}

extension $IndicatorSignalIntelEventCopyWith on IndicatorSignalIntelEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIndicatorSignalIntelEvent.copyWith(...)` or `instanceOfIndicatorSignalIntelEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IndicatorSignalIntelEventCWProxy get copyWith =>
      _$IndicatorSignalIntelEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IndicatorSignalIntelEvent _$IndicatorSignalIntelEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IndicatorSignalIntelEvent', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type']);
  final val = IndicatorSignalIntelEvent(
    type: $checkedConvert('type', (v) => v as String),
    regionCodes: $checkedConvert(
      'regionCodes',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    placeId: $checkedConvert('placeId', (v) => v as String?),
    bbox: $checkedConvert('bbox', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$IndicatorSignalIntelEventToJson(
  IndicatorSignalIntelEvent instance,
) => <String, dynamic>{
  'type': instance.type,
  'regionCodes': ?instance.regionCodes,
  'placeId': ?instance.placeId,
  'bbox': ?instance.bbox,
};
