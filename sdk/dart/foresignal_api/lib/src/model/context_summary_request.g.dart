// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'context_summary_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ContextSummaryRequestCWProxy {
  ContextSummaryRequest viewport(WorkspaceViewport viewport);

  ContextSummaryRequest timeRange(TimeRange timeRange);

  ContextSummaryRequest asOf(DateTime? asOf);

  ContextSummaryRequest filters(Map<String, Object>? filters);

  ContextSummaryRequest layers(List<String>? layers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ContextSummaryRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ContextSummaryRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  ContextSummaryRequest call({
    WorkspaceViewport viewport,
    TimeRange timeRange,
    DateTime? asOf,
    Map<String, Object>? filters,
    List<String>? layers,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfContextSummaryRequest.copyWith(...)` or call `instanceOfContextSummaryRequest.copyWith.fieldName(value)` for a single field.
class _$ContextSummaryRequestCWProxyImpl
    implements _$ContextSummaryRequestCWProxy {
  const _$ContextSummaryRequestCWProxyImpl(this._value);

  final ContextSummaryRequest _value;

  @override
  ContextSummaryRequest viewport(WorkspaceViewport viewport) =>
      call(viewport: viewport);

  @override
  ContextSummaryRequest timeRange(TimeRange timeRange) =>
      call(timeRange: timeRange);

  @override
  ContextSummaryRequest asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  ContextSummaryRequest filters(Map<String, Object>? filters) =>
      call(filters: filters);

  @override
  ContextSummaryRequest layers(List<String>? layers) => call(layers: layers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ContextSummaryRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ContextSummaryRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ContextSummaryRequest call({
    Object? viewport = const $CopyWithPlaceholder(),
    Object? timeRange = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? filters = const $CopyWithPlaceholder(),
    Object? layers = const $CopyWithPlaceholder(),
  }) {
    return ContextSummaryRequest(
      viewport: viewport == const $CopyWithPlaceholder() || viewport == null
          ? _value.viewport
          // ignore: cast_nullable_to_non_nullable
          : viewport as WorkspaceViewport,
      timeRange: timeRange == const $CopyWithPlaceholder() || timeRange == null
          ? _value.timeRange
          // ignore: cast_nullable_to_non_nullable
          : timeRange as TimeRange,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      filters: filters == const $CopyWithPlaceholder()
          ? _value.filters
          // ignore: cast_nullable_to_non_nullable
          : filters as Map<String, Object>?,
      layers: layers == const $CopyWithPlaceholder()
          ? _value.layers
          // ignore: cast_nullable_to_non_nullable
          : layers as List<String>?,
    );
  }
}

extension $ContextSummaryRequestCopyWith on ContextSummaryRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfContextSummaryRequest.copyWith(...)` or `instanceOfContextSummaryRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ContextSummaryRequestCWProxy get copyWith =>
      _$ContextSummaryRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ContextSummaryRequest _$ContextSummaryRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ContextSummaryRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['viewport', 'timeRange']);
  final val = ContextSummaryRequest(
    viewport: $checkedConvert(
      'viewport',
      (v) => WorkspaceViewport.fromJson(v as Map<String, dynamic>),
    ),
    timeRange: $checkedConvert(
      'timeRange',
      (v) => TimeRange.fromJson(v as Map<String, dynamic>),
    ),
    asOf: $checkedConvert(
      'asOf',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    filters: $checkedConvert(
      'filters',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    layers: $checkedConvert(
      'layers',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ContextSummaryRequestToJson(
  ContextSummaryRequest instance,
) => <String, dynamic>{
  'viewport': instance.viewport.toJson(),
  'timeRange': instance.timeRange.toJson(),
  'asOf': ?instance.asOf?.toIso8601String(),
  'filters': ?instance.filters,
  'layers': ?instance.layers,
};
