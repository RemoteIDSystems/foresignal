// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_summary_layers_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSummaryLayersInnerCWProxy {
  ChronicleSummaryLayersInner layerId(String layerId);

  ChronicleSummaryLayersInner name(String name);

  ChronicleSummaryLayersInner rows(int rows);

  ChronicleSummaryLayersInner series(
    List<ChronicleSummaryLayersInnerSeriesInner>? series,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSummaryLayersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSummaryLayersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSummaryLayersInner call({
    String layerId,
    String name,
    int rows,
    List<ChronicleSummaryLayersInnerSeriesInner>? series,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSummaryLayersInner.copyWith(...)` or call `instanceOfChronicleSummaryLayersInner.copyWith.fieldName(value)` for a single field.
class _$ChronicleSummaryLayersInnerCWProxyImpl
    implements _$ChronicleSummaryLayersInnerCWProxy {
  const _$ChronicleSummaryLayersInnerCWProxyImpl(this._value);

  final ChronicleSummaryLayersInner _value;

  @override
  ChronicleSummaryLayersInner layerId(String layerId) => call(layerId: layerId);

  @override
  ChronicleSummaryLayersInner name(String name) => call(name: name);

  @override
  ChronicleSummaryLayersInner rows(int rows) => call(rows: rows);

  @override
  ChronicleSummaryLayersInner series(
    List<ChronicleSummaryLayersInnerSeriesInner>? series,
  ) => call(series: series);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSummaryLayersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSummaryLayersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSummaryLayersInner call({
    Object? layerId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? rows = const $CopyWithPlaceholder(),
    Object? series = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSummaryLayersInner(
      layerId: layerId == const $CopyWithPlaceholder() || layerId == null
          ? _value.layerId
          // ignore: cast_nullable_to_non_nullable
          : layerId as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      rows: rows == const $CopyWithPlaceholder() || rows == null
          ? _value.rows
          // ignore: cast_nullable_to_non_nullable
          : rows as int,
      series: series == const $CopyWithPlaceholder()
          ? _value.series
          // ignore: cast_nullable_to_non_nullable
          : series as List<ChronicleSummaryLayersInnerSeriesInner>?,
    );
  }
}

extension $ChronicleSummaryLayersInnerCopyWith on ChronicleSummaryLayersInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSummaryLayersInner.copyWith(...)` or `instanceOfChronicleSummaryLayersInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSummaryLayersInnerCWProxy get copyWith =>
      _$ChronicleSummaryLayersInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSummaryLayersInner _$ChronicleSummaryLayersInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleSummaryLayersInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['layerId', 'name', 'rows']);
  final val = ChronicleSummaryLayersInner(
    layerId: $checkedConvert('layerId', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    rows: $checkedConvert('rows', (v) => (v as num).toInt()),
    series: $checkedConvert(
      'series',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => ChronicleSummaryLayersInnerSeriesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleSummaryLayersInnerToJson(
  ChronicleSummaryLayersInner instance,
) => <String, dynamic>{
  'layerId': instance.layerId,
  'name': instance.name,
  'rows': instance.rows,
  'series': ?instance.series?.map((e) => e.toJson()).toList(),
};
