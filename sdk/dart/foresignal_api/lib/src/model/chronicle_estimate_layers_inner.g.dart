// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_estimate_layers_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleEstimateLayersInnerCWProxy {
  ChronicleEstimateLayersInner layerId(String layerId);

  ChronicleEstimateLayersInner name(String? name);

  ChronicleEstimateLayersInner capture(CaptureMode? capture);

  ChronicleEstimateLayersInner rowsPerDay(num rowsPerDay);

  ChronicleEstimateLayersInner bytesPerDay(num bytesPerDay);

  ChronicleEstimateLayersInner basis(String? basis);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleEstimateLayersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleEstimateLayersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleEstimateLayersInner call({
    String layerId,
    String? name,
    CaptureMode? capture,
    num rowsPerDay,
    num bytesPerDay,
    String? basis,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleEstimateLayersInner.copyWith(...)` or call `instanceOfChronicleEstimateLayersInner.copyWith.fieldName(value)` for a single field.
class _$ChronicleEstimateLayersInnerCWProxyImpl
    implements _$ChronicleEstimateLayersInnerCWProxy {
  const _$ChronicleEstimateLayersInnerCWProxyImpl(this._value);

  final ChronicleEstimateLayersInner _value;

  @override
  ChronicleEstimateLayersInner layerId(String layerId) =>
      call(layerId: layerId);

  @override
  ChronicleEstimateLayersInner name(String? name) => call(name: name);

  @override
  ChronicleEstimateLayersInner capture(CaptureMode? capture) =>
      call(capture: capture);

  @override
  ChronicleEstimateLayersInner rowsPerDay(num rowsPerDay) =>
      call(rowsPerDay: rowsPerDay);

  @override
  ChronicleEstimateLayersInner bytesPerDay(num bytesPerDay) =>
      call(bytesPerDay: bytesPerDay);

  @override
  ChronicleEstimateLayersInner basis(String? basis) => call(basis: basis);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleEstimateLayersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleEstimateLayersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleEstimateLayersInner call({
    Object? layerId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? capture = const $CopyWithPlaceholder(),
    Object? rowsPerDay = const $CopyWithPlaceholder(),
    Object? bytesPerDay = const $CopyWithPlaceholder(),
    Object? basis = const $CopyWithPlaceholder(),
  }) {
    return ChronicleEstimateLayersInner(
      layerId: layerId == const $CopyWithPlaceholder() || layerId == null
          ? _value.layerId
          // ignore: cast_nullable_to_non_nullable
          : layerId as String,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      capture: capture == const $CopyWithPlaceholder()
          ? _value.capture
          // ignore: cast_nullable_to_non_nullable
          : capture as CaptureMode?,
      rowsPerDay:
          rowsPerDay == const $CopyWithPlaceholder() || rowsPerDay == null
          ? _value.rowsPerDay
          // ignore: cast_nullable_to_non_nullable
          : rowsPerDay as num,
      bytesPerDay:
          bytesPerDay == const $CopyWithPlaceholder() || bytesPerDay == null
          ? _value.bytesPerDay
          // ignore: cast_nullable_to_non_nullable
          : bytesPerDay as num,
      basis: basis == const $CopyWithPlaceholder()
          ? _value.basis
          // ignore: cast_nullable_to_non_nullable
          : basis as String?,
    );
  }
}

extension $ChronicleEstimateLayersInnerCopyWith
    on ChronicleEstimateLayersInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleEstimateLayersInner.copyWith(...)` or `instanceOfChronicleEstimateLayersInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleEstimateLayersInnerCWProxy get copyWith =>
      _$ChronicleEstimateLayersInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleEstimateLayersInner _$ChronicleEstimateLayersInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleEstimateLayersInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['layerId', 'rowsPerDay', 'bytesPerDay'],
  );
  final val = ChronicleEstimateLayersInner(
    layerId: $checkedConvert('layerId', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String?),
    capture: $checkedConvert(
      'capture',
      (v) => v == null ? null : CaptureMode.fromJson(v as Map<String, dynamic>),
    ),
    rowsPerDay: $checkedConvert('rowsPerDay', (v) => v as num),
    bytesPerDay: $checkedConvert('bytesPerDay', (v) => v as num),
    basis: $checkedConvert('basis', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ChronicleEstimateLayersInnerToJson(
  ChronicleEstimateLayersInner instance,
) => <String, dynamic>{
  'layerId': instance.layerId,
  'name': ?instance.name,
  'capture': ?instance.capture?.toJson(),
  'rowsPerDay': instance.rowsPerDay,
  'bytesPerDay': instance.bytesPerDay,
  'basis': ?instance.basis,
};
