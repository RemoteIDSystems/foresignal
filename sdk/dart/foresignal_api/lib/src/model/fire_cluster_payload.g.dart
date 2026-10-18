// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'fire_cluster_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FireClusterPayloadCWProxy {
  FireClusterPayload pixels(int pixels);

  FireClusterPayload frpTotalMw(num? frpTotalMw);

  FireClusterPayload frpMaxMw(num? frpMaxMw);

  FireClusterPayload firstSeenAt(DateTime? firstSeenAt);

  FireClusterPayload label(String label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FireClusterPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FireClusterPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  FireClusterPayload call({
    int pixels,
    num? frpTotalMw,
    num? frpMaxMw,
    DateTime? firstSeenAt,
    String label,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFireClusterPayload.copyWith(...)` or call `instanceOfFireClusterPayload.copyWith.fieldName(value)` for a single field.
class _$FireClusterPayloadCWProxyImpl implements _$FireClusterPayloadCWProxy {
  const _$FireClusterPayloadCWProxyImpl(this._value);

  final FireClusterPayload _value;

  @override
  FireClusterPayload pixels(int pixels) => call(pixels: pixels);

  @override
  FireClusterPayload frpTotalMw(num? frpTotalMw) =>
      call(frpTotalMw: frpTotalMw);

  @override
  FireClusterPayload frpMaxMw(num? frpMaxMw) => call(frpMaxMw: frpMaxMw);

  @override
  FireClusterPayload firstSeenAt(DateTime? firstSeenAt) =>
      call(firstSeenAt: firstSeenAt);

  @override
  FireClusterPayload label(String label) => call(label: label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FireClusterPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FireClusterPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FireClusterPayload call({
    Object? pixels = const $CopyWithPlaceholder(),
    Object? frpTotalMw = const $CopyWithPlaceholder(),
    Object? frpMaxMw = const $CopyWithPlaceholder(),
    Object? firstSeenAt = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
  }) {
    return FireClusterPayload(
      pixels: pixels == const $CopyWithPlaceholder() || pixels == null
          ? _value.pixels
          // ignore: cast_nullable_to_non_nullable
          : pixels as int,
      frpTotalMw: frpTotalMw == const $CopyWithPlaceholder()
          ? _value.frpTotalMw
          // ignore: cast_nullable_to_non_nullable
          : frpTotalMw as num?,
      frpMaxMw: frpMaxMw == const $CopyWithPlaceholder()
          ? _value.frpMaxMw
          // ignore: cast_nullable_to_non_nullable
          : frpMaxMw as num?,
      firstSeenAt: firstSeenAt == const $CopyWithPlaceholder()
          ? _value.firstSeenAt
          // ignore: cast_nullable_to_non_nullable
          : firstSeenAt as DateTime?,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String,
    );
  }
}

extension $FireClusterPayloadCopyWith on FireClusterPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFireClusterPayload.copyWith(...)` or `instanceOfFireClusterPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FireClusterPayloadCWProxy get copyWith =>
      _$FireClusterPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FireClusterPayload _$FireClusterPayloadFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FireClusterPayload', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['pixels', 'label']);
      final val = FireClusterPayload(
        pixels: $checkedConvert('pixels', (v) => (v as num).toInt()),
        frpTotalMw: $checkedConvert('frpTotalMw', (v) => v as num?),
        frpMaxMw: $checkedConvert('frpMaxMw', (v) => v as num?),
        firstSeenAt: $checkedConvert(
          'firstSeenAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        label: $checkedConvert('label', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$FireClusterPayloadToJson(FireClusterPayload instance) =>
    <String, dynamic>{
      'pixels': instance.pixels,
      'frpTotalMw': ?instance.frpTotalMw,
      'frpMaxMw': ?instance.frpMaxMw,
      'firstSeenAt': ?instance.firstSeenAt?.toIso8601String(),
      'label': instance.label,
    };
