// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cpa_batch_request_filter.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CpaBatchRequestFilterCWProxy {
  CpaBatchRequestFilter kinds(List<String>? kinds);

  CpaBatchRequestFilter sources(List<String>? sources);

  CpaBatchRequestFilter bbox(List<num>? bbox);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CpaBatchRequestFilter(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CpaBatchRequestFilter(...).copyWith(id: 12, name: "My name")
  /// ```
  CpaBatchRequestFilter call({
    List<String>? kinds,
    List<String>? sources,
    List<num>? bbox,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCpaBatchRequestFilter.copyWith(...)` or call `instanceOfCpaBatchRequestFilter.copyWith.fieldName(value)` for a single field.
class _$CpaBatchRequestFilterCWProxyImpl
    implements _$CpaBatchRequestFilterCWProxy {
  const _$CpaBatchRequestFilterCWProxyImpl(this._value);

  final CpaBatchRequestFilter _value;

  @override
  CpaBatchRequestFilter kinds(List<String>? kinds) => call(kinds: kinds);

  @override
  CpaBatchRequestFilter sources(List<String>? sources) =>
      call(sources: sources);

  @override
  CpaBatchRequestFilter bbox(List<num>? bbox) => call(bbox: bbox);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CpaBatchRequestFilter(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CpaBatchRequestFilter(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CpaBatchRequestFilter call({
    Object? kinds = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
  }) {
    return CpaBatchRequestFilter(
      kinds: kinds == const $CopyWithPlaceholder()
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>?,
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
    );
  }
}

extension $CpaBatchRequestFilterCopyWith on CpaBatchRequestFilter {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCpaBatchRequestFilter.copyWith(...)` or `instanceOfCpaBatchRequestFilter.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CpaBatchRequestFilterCWProxy get copyWith =>
      _$CpaBatchRequestFilterCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CpaBatchRequestFilter _$CpaBatchRequestFilterFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CpaBatchRequestFilter', json, ($checkedConvert) {
  final val = CpaBatchRequestFilter(
    kinds: $checkedConvert(
      'kinds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    sources: $checkedConvert(
      'sources',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    bbox: $checkedConvert(
      'bbox',
      (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$CpaBatchRequestFilterToJson(
  CpaBatchRequestFilter instance,
) => <String, dynamic>{
  'kinds': ?instance.kinds,
  'sources': ?instance.sources,
  'bbox': ?instance.bbox,
};
