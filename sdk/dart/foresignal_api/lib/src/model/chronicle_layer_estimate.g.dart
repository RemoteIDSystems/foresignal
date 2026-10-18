// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_layer_estimate.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleLayerEstimateCWProxy {
  ChronicleLayerEstimate rowsPerDay(num rowsPerDay);

  ChronicleLayerEstimate bytesPerDay(num bytesPerDay);

  ChronicleLayerEstimate basis(String? basis);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerEstimate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerEstimate(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleLayerEstimate call({num rowsPerDay, num bytesPerDay, String? basis});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleLayerEstimate.copyWith(...)` or call `instanceOfChronicleLayerEstimate.copyWith.fieldName(value)` for a single field.
class _$ChronicleLayerEstimateCWProxyImpl
    implements _$ChronicleLayerEstimateCWProxy {
  const _$ChronicleLayerEstimateCWProxyImpl(this._value);

  final ChronicleLayerEstimate _value;

  @override
  ChronicleLayerEstimate rowsPerDay(num rowsPerDay) =>
      call(rowsPerDay: rowsPerDay);

  @override
  ChronicleLayerEstimate bytesPerDay(num bytesPerDay) =>
      call(bytesPerDay: bytesPerDay);

  @override
  ChronicleLayerEstimate basis(String? basis) => call(basis: basis);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerEstimate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerEstimate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleLayerEstimate call({
    Object? rowsPerDay = const $CopyWithPlaceholder(),
    Object? bytesPerDay = const $CopyWithPlaceholder(),
    Object? basis = const $CopyWithPlaceholder(),
  }) {
    return ChronicleLayerEstimate(
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

extension $ChronicleLayerEstimateCopyWith on ChronicleLayerEstimate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleLayerEstimate.copyWith(...)` or `instanceOfChronicleLayerEstimate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleLayerEstimateCWProxy get copyWith =>
      _$ChronicleLayerEstimateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleLayerEstimate _$ChronicleLayerEstimateFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleLayerEstimate', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['rowsPerDay', 'bytesPerDay']);
  final val = ChronicleLayerEstimate(
    rowsPerDay: $checkedConvert('rowsPerDay', (v) => v as num),
    bytesPerDay: $checkedConvert('bytesPerDay', (v) => v as num),
    basis: $checkedConvert('basis', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ChronicleLayerEstimateToJson(
  ChronicleLayerEstimate instance,
) => <String, dynamic>{
  'rowsPerDay': instance.rowsPerDay,
  'bytesPerDay': instance.bytesPerDay,
  'basis': ?instance.basis,
};
