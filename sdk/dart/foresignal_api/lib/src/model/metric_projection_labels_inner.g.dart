// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'metric_projection_labels_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MetricProjectionLabelsInnerCWProxy {
  MetricProjectionLabelsInner name(String name);

  MetricProjectionLabelsInner path(String path);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MetricProjectionLabelsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MetricProjectionLabelsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  MetricProjectionLabelsInner call({String name, String path});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMetricProjectionLabelsInner.copyWith(...)` or call `instanceOfMetricProjectionLabelsInner.copyWith.fieldName(value)` for a single field.
class _$MetricProjectionLabelsInnerCWProxyImpl
    implements _$MetricProjectionLabelsInnerCWProxy {
  const _$MetricProjectionLabelsInnerCWProxyImpl(this._value);

  final MetricProjectionLabelsInner _value;

  @override
  MetricProjectionLabelsInner name(String name) => call(name: name);

  @override
  MetricProjectionLabelsInner path(String path) => call(path: path);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MetricProjectionLabelsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MetricProjectionLabelsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MetricProjectionLabelsInner call({
    Object? name = const $CopyWithPlaceholder(),
    Object? path = const $CopyWithPlaceholder(),
  }) {
    return MetricProjectionLabelsInner(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      path: path == const $CopyWithPlaceholder() || path == null
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String,
    );
  }
}

extension $MetricProjectionLabelsInnerCopyWith on MetricProjectionLabelsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMetricProjectionLabelsInner.copyWith(...)` or `instanceOfMetricProjectionLabelsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MetricProjectionLabelsInnerCWProxy get copyWith =>
      _$MetricProjectionLabelsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MetricProjectionLabelsInner _$MetricProjectionLabelsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MetricProjectionLabelsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['name', 'path']);
  final val = MetricProjectionLabelsInner(
    name: $checkedConvert('name', (v) => v as String),
    path: $checkedConvert('path', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$MetricProjectionLabelsInnerToJson(
  MetricProjectionLabelsInner instance,
) => <String, dynamic>{'name': instance.name, 'path': instance.path};
