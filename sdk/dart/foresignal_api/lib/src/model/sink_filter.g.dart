// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sink_filter.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SinkFilterCWProxy {
  SinkFilter sources(List<String>? sources);

  SinkFilter kinds(List<String>? kinds);

  SinkFilter bbox(List<num>? bbox);

  SinkFilter placeId(String? placeId);

  SinkFilter watchlistId(String? watchlistId);

  SinkFilter includePrivate(bool? includePrivate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkFilter(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkFilter(...).copyWith(id: 12, name: "My name")
  /// ```
  SinkFilter call({
    List<String>? sources,
    List<String>? kinds,
    List<num>? bbox,
    String? placeId,
    String? watchlistId,
    bool? includePrivate,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSinkFilter.copyWith(...)` or call `instanceOfSinkFilter.copyWith.fieldName(value)` for a single field.
class _$SinkFilterCWProxyImpl implements _$SinkFilterCWProxy {
  const _$SinkFilterCWProxyImpl(this._value);

  final SinkFilter _value;

  @override
  SinkFilter sources(List<String>? sources) => call(sources: sources);

  @override
  SinkFilter kinds(List<String>? kinds) => call(kinds: kinds);

  @override
  SinkFilter bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  SinkFilter placeId(String? placeId) => call(placeId: placeId);

  @override
  SinkFilter watchlistId(String? watchlistId) => call(watchlistId: watchlistId);

  @override
  SinkFilter includePrivate(bool? includePrivate) =>
      call(includePrivate: includePrivate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkFilter(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkFilter(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SinkFilter call({
    Object? sources = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
    Object? watchlistId = const $CopyWithPlaceholder(),
    Object? includePrivate = const $CopyWithPlaceholder(),
  }) {
    return SinkFilter(
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      kinds: kinds == const $CopyWithPlaceholder()
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
      watchlistId: watchlistId == const $CopyWithPlaceholder()
          ? _value.watchlistId
          // ignore: cast_nullable_to_non_nullable
          : watchlistId as String?,
      includePrivate: includePrivate == const $CopyWithPlaceholder()
          ? _value.includePrivate
          // ignore: cast_nullable_to_non_nullable
          : includePrivate as bool?,
    );
  }
}

extension $SinkFilterCopyWith on SinkFilter {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSinkFilter.copyWith(...)` or `instanceOfSinkFilter.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SinkFilterCWProxy get copyWith => _$SinkFilterCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SinkFilter _$SinkFilterFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SinkFilter', json, ($checkedConvert) {
      final val = SinkFilter(
        sources: $checkedConvert(
          'sources',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        kinds: $checkedConvert(
          'kinds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        bbox: $checkedConvert(
          'bbox',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        placeId: $checkedConvert('placeId', (v) => v as String?),
        watchlistId: $checkedConvert('watchlistId', (v) => v as String?),
        includePrivate: $checkedConvert(
          'includePrivate',
          (v) => v as bool? ?? false,
        ),
      );
      return val;
    });

Map<String, dynamic> _$SinkFilterToJson(SinkFilter instance) =>
    <String, dynamic>{
      'sources': ?instance.sources,
      'kinds': ?instance.kinds,
      'bbox': ?instance.bbox,
      'placeId': ?instance.placeId,
      'watchlistId': ?instance.watchlistId,
      'includePrivate': ?instance.includePrivate,
    };
