// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_summary_layers_inner_series_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSummaryLayersInnerSeriesInnerCWProxy {
  ChronicleSummaryLayersInnerSeriesInner t(DateTime t);

  ChronicleSummaryLayersInnerSeriesInner count(int count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSummaryLayersInnerSeriesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSummaryLayersInnerSeriesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSummaryLayersInnerSeriesInner call({DateTime t, int count});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSummaryLayersInnerSeriesInner.copyWith(...)` or call `instanceOfChronicleSummaryLayersInnerSeriesInner.copyWith.fieldName(value)` for a single field.
class _$ChronicleSummaryLayersInnerSeriesInnerCWProxyImpl
    implements _$ChronicleSummaryLayersInnerSeriesInnerCWProxy {
  const _$ChronicleSummaryLayersInnerSeriesInnerCWProxyImpl(this._value);

  final ChronicleSummaryLayersInnerSeriesInner _value;

  @override
  ChronicleSummaryLayersInnerSeriesInner t(DateTime t) => call(t: t);

  @override
  ChronicleSummaryLayersInnerSeriesInner count(int count) => call(count: count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSummaryLayersInnerSeriesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSummaryLayersInnerSeriesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSummaryLayersInnerSeriesInner call({
    Object? t = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSummaryLayersInnerSeriesInner(
      t: t == const $CopyWithPlaceholder() || t == null
          ? _value.t
          // ignore: cast_nullable_to_non_nullable
          : t as DateTime,
      count: count == const $CopyWithPlaceholder() || count == null
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int,
    );
  }
}

extension $ChronicleSummaryLayersInnerSeriesInnerCopyWith
    on ChronicleSummaryLayersInnerSeriesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSummaryLayersInnerSeriesInner.copyWith(...)` or `instanceOfChronicleSummaryLayersInnerSeriesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSummaryLayersInnerSeriesInnerCWProxy get copyWith =>
      _$ChronicleSummaryLayersInnerSeriesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSummaryLayersInnerSeriesInner
_$ChronicleSummaryLayersInnerSeriesInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChronicleSummaryLayersInnerSeriesInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['t', 'count']);
      final val = ChronicleSummaryLayersInnerSeriesInner(
        t: $checkedConvert('t', (v) => DateTime.parse(v as String)),
        count: $checkedConvert('count', (v) => (v as num).toInt()),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleSummaryLayersInnerSeriesInnerToJson(
  ChronicleSummaryLayersInnerSeriesInner instance,
) => <String, dynamic>{
  't': instance.t.toIso8601String(),
  'count': instance.count,
};
