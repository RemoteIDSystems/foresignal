// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'metric_projection.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MetricProjectionCWProxy {
  MetricProjection name(String name);

  MetricProjection source_(String source_);

  MetricProjection kind(String? kind);

  MetricProjection path(String path);

  MetricProjection labels(List<MetricProjectionLabelsInner>? labels);

  MetricProjection unit(String? unit);

  MetricProjection help(String? help);

  MetricProjection maxSeries(int? maxSeries);

  MetricProjection allowPrivate(bool? allowPrivate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MetricProjection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MetricProjection(...).copyWith(id: 12, name: "My name")
  /// ```
  MetricProjection call({
    String name,
    String source_,
    String? kind,
    String path,
    List<MetricProjectionLabelsInner>? labels,
    String? unit,
    String? help,
    int? maxSeries,
    bool? allowPrivate,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMetricProjection.copyWith(...)` or call `instanceOfMetricProjection.copyWith.fieldName(value)` for a single field.
class _$MetricProjectionCWProxyImpl implements _$MetricProjectionCWProxy {
  const _$MetricProjectionCWProxyImpl(this._value);

  final MetricProjection _value;

  @override
  MetricProjection name(String name) => call(name: name);

  @override
  MetricProjection source_(String source_) => call(source_: source_);

  @override
  MetricProjection kind(String? kind) => call(kind: kind);

  @override
  MetricProjection path(String path) => call(path: path);

  @override
  MetricProjection labels(List<MetricProjectionLabelsInner>? labels) =>
      call(labels: labels);

  @override
  MetricProjection unit(String? unit) => call(unit: unit);

  @override
  MetricProjection help(String? help) => call(help: help);

  @override
  MetricProjection maxSeries(int? maxSeries) => call(maxSeries: maxSeries);

  @override
  MetricProjection allowPrivate(bool? allowPrivate) =>
      call(allowPrivate: allowPrivate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MetricProjection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MetricProjection(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MetricProjection call({
    Object? name = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? path = const $CopyWithPlaceholder(),
    Object? labels = const $CopyWithPlaceholder(),
    Object? unit = const $CopyWithPlaceholder(),
    Object? help = const $CopyWithPlaceholder(),
    Object? maxSeries = const $CopyWithPlaceholder(),
    Object? allowPrivate = const $CopyWithPlaceholder(),
  }) {
    return MetricProjection(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as String,
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String?,
      path: path == const $CopyWithPlaceholder() || path == null
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String,
      labels: labels == const $CopyWithPlaceholder()
          ? _value.labels
          // ignore: cast_nullable_to_non_nullable
          : labels as List<MetricProjectionLabelsInner>?,
      unit: unit == const $CopyWithPlaceholder()
          ? _value.unit
          // ignore: cast_nullable_to_non_nullable
          : unit as String?,
      help: help == const $CopyWithPlaceholder()
          ? _value.help
          // ignore: cast_nullable_to_non_nullable
          : help as String?,
      maxSeries: maxSeries == const $CopyWithPlaceholder()
          ? _value.maxSeries
          // ignore: cast_nullable_to_non_nullable
          : maxSeries as int?,
      allowPrivate: allowPrivate == const $CopyWithPlaceholder()
          ? _value.allowPrivate
          // ignore: cast_nullable_to_non_nullable
          : allowPrivate as bool?,
    );
  }
}

extension $MetricProjectionCopyWith on MetricProjection {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMetricProjection.copyWith(...)` or `instanceOfMetricProjection.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MetricProjectionCWProxy get copyWith => _$MetricProjectionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MetricProjection _$MetricProjectionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MetricProjection', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['name', 'source', 'path']);
  final val = MetricProjection(
    name: $checkedConvert('name', (v) => v as String),
    source_: $checkedConvert('source', (v) => v as String),
    kind: $checkedConvert('kind', (v) => v as String?),
    path: $checkedConvert('path', (v) => v as String),
    labels: $checkedConvert(
      'labels',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) =>
                MetricProjectionLabelsInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    unit: $checkedConvert('unit', (v) => v as String?),
    help: $checkedConvert('help', (v) => v as String?),
    maxSeries: $checkedConvert('maxSeries', (v) => (v as num?)?.toInt() ?? 500),
    allowPrivate: $checkedConvert('allowPrivate', (v) => v as bool? ?? false),
  );
  return val;
}, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$MetricProjectionToJson(MetricProjection instance) =>
    <String, dynamic>{
      'name': instance.name,
      'source': instance.source_,
      'kind': ?instance.kind,
      'path': instance.path,
      'labels': ?instance.labels?.map((e) => e.toJson()).toList(),
      'unit': ?instance.unit,
      'help': ?instance.help,
      'maxSeries': ?instance.maxSeries,
      'allowPrivate': ?instance.allowPrivate,
    };
