// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_summary_gaps_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSummaryGapsInnerCWProxy {
  ChronicleSummaryGapsInner from(DateTime from);

  ChronicleSummaryGapsInner to(DateTime to);

  ChronicleSummaryGapsInner reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSummaryGapsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSummaryGapsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSummaryGapsInner call({DateTime from, DateTime to, String? reason});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSummaryGapsInner.copyWith(...)` or call `instanceOfChronicleSummaryGapsInner.copyWith.fieldName(value)` for a single field.
class _$ChronicleSummaryGapsInnerCWProxyImpl
    implements _$ChronicleSummaryGapsInnerCWProxy {
  const _$ChronicleSummaryGapsInnerCWProxyImpl(this._value);

  final ChronicleSummaryGapsInner _value;

  @override
  ChronicleSummaryGapsInner from(DateTime from) => call(from: from);

  @override
  ChronicleSummaryGapsInner to(DateTime to) => call(to: to);

  @override
  ChronicleSummaryGapsInner reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSummaryGapsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSummaryGapsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSummaryGapsInner call({
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSummaryGapsInner(
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $ChronicleSummaryGapsInnerCopyWith on ChronicleSummaryGapsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSummaryGapsInner.copyWith(...)` or `instanceOfChronicleSummaryGapsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSummaryGapsInnerCWProxy get copyWith =>
      _$ChronicleSummaryGapsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSummaryGapsInner _$ChronicleSummaryGapsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleSummaryGapsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['from', 'to']);
  final val = ChronicleSummaryGapsInner(
    from: $checkedConvert('from', (v) => DateTime.parse(v as String)),
    to: $checkedConvert('to', (v) => DateTime.parse(v as String)),
    reason: $checkedConvert('reason', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ChronicleSummaryGapsInnerToJson(
  ChronicleSummaryGapsInner instance,
) => <String, dynamic>{
  'from': instance.from.toIso8601String(),
  'to': instance.to.toIso8601String(),
  'reason': ?instance.reason,
};
