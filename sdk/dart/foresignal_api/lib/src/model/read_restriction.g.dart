// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'read_restriction.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReadRestrictionCWProxy {
  ReadRestriction sources(List<String>? sources);

  ReadRestriction kinds(List<String>? kinds);

  ReadRestriction bbox(List<num>? bbox);

  ReadRestriction endpoints(List<String>? endpoints);

  ReadRestriction federation(bool? federation);

  ReadRestriction includeExercise(bool? includeExercise);

  ReadRestriction area(Geometry? area);

  ReadRestriction placeIds(List<String>? placeIds);

  ReadRestriction excludePrivate(bool? excludePrivate);

  ReadRestriction maxRequestsPerMinute(int? maxRequestsPerMinute);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReadRestriction(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReadRestriction(...).copyWith(id: 12, name: "My name")
  /// ```
  ReadRestriction call({
    List<String>? sources,
    List<String>? kinds,
    List<num>? bbox,
    List<String>? endpoints,
    bool? federation,
    bool? includeExercise,
    Geometry? area,
    List<String>? placeIds,
    bool? excludePrivate,
    int? maxRequestsPerMinute,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReadRestriction.copyWith(...)` or call `instanceOfReadRestriction.copyWith.fieldName(value)` for a single field.
class _$ReadRestrictionCWProxyImpl implements _$ReadRestrictionCWProxy {
  const _$ReadRestrictionCWProxyImpl(this._value);

  final ReadRestriction _value;

  @override
  ReadRestriction sources(List<String>? sources) => call(sources: sources);

  @override
  ReadRestriction kinds(List<String>? kinds) => call(kinds: kinds);

  @override
  ReadRestriction bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  ReadRestriction endpoints(List<String>? endpoints) =>
      call(endpoints: endpoints);

  @override
  ReadRestriction federation(bool? federation) => call(federation: federation);

  @override
  ReadRestriction includeExercise(bool? includeExercise) =>
      call(includeExercise: includeExercise);

  @override
  ReadRestriction area(Geometry? area) => call(area: area);

  @override
  ReadRestriction placeIds(List<String>? placeIds) => call(placeIds: placeIds);

  @override
  ReadRestriction excludePrivate(bool? excludePrivate) =>
      call(excludePrivate: excludePrivate);

  @override
  ReadRestriction maxRequestsPerMinute(int? maxRequestsPerMinute) =>
      call(maxRequestsPerMinute: maxRequestsPerMinute);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReadRestriction(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReadRestriction(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReadRestriction call({
    Object? sources = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? endpoints = const $CopyWithPlaceholder(),
    Object? federation = const $CopyWithPlaceholder(),
    Object? includeExercise = const $CopyWithPlaceholder(),
    Object? area = const $CopyWithPlaceholder(),
    Object? placeIds = const $CopyWithPlaceholder(),
    Object? excludePrivate = const $CopyWithPlaceholder(),
    Object? maxRequestsPerMinute = const $CopyWithPlaceholder(),
  }) {
    return ReadRestriction(
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
      endpoints: endpoints == const $CopyWithPlaceholder()
          ? _value.endpoints
          // ignore: cast_nullable_to_non_nullable
          : endpoints as List<String>?,
      federation: federation == const $CopyWithPlaceholder()
          ? _value.federation
          // ignore: cast_nullable_to_non_nullable
          : federation as bool?,
      includeExercise: includeExercise == const $CopyWithPlaceholder()
          ? _value.includeExercise
          // ignore: cast_nullable_to_non_nullable
          : includeExercise as bool?,
      area: area == const $CopyWithPlaceholder()
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as Geometry?,
      placeIds: placeIds == const $CopyWithPlaceholder()
          ? _value.placeIds
          // ignore: cast_nullable_to_non_nullable
          : placeIds as List<String>?,
      excludePrivate: excludePrivate == const $CopyWithPlaceholder()
          ? _value.excludePrivate
          // ignore: cast_nullable_to_non_nullable
          : excludePrivate as bool?,
      maxRequestsPerMinute: maxRequestsPerMinute == const $CopyWithPlaceholder()
          ? _value.maxRequestsPerMinute
          // ignore: cast_nullable_to_non_nullable
          : maxRequestsPerMinute as int?,
    );
  }
}

extension $ReadRestrictionCopyWith on ReadRestriction {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReadRestriction.copyWith(...)` or `instanceOfReadRestriction.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReadRestrictionCWProxy get copyWith => _$ReadRestrictionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReadRestriction _$ReadRestrictionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReadRestriction', json, ($checkedConvert) {
      final val = ReadRestriction(
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
        endpoints: $checkedConvert(
          'endpoints',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        federation: $checkedConvert('federation', (v) => v as bool? ?? false),
        includeExercise: $checkedConvert(
          'includeExercise',
          (v) => v as bool? ?? false,
        ),
        area: $checkedConvert(
          'area',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        placeIds: $checkedConvert(
          'placeIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        excludePrivate: $checkedConvert(
          'excludePrivate',
          (v) => v as bool? ?? true,
        ),
        maxRequestsPerMinute: $checkedConvert(
          'maxRequestsPerMinute',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ReadRestrictionToJson(ReadRestriction instance) =>
    <String, dynamic>{
      'sources': ?instance.sources,
      'kinds': ?instance.kinds,
      'bbox': ?instance.bbox,
      'endpoints': ?instance.endpoints,
      'federation': ?instance.federation,
      'includeExercise': ?instance.includeExercise,
      'area': ?instance.area?.toJson(),
      'placeIds': ?instance.placeIds,
      'excludePrivate': ?instance.excludePrivate,
      'maxRequestsPerMinute': ?instance.maxRequestsPerMinute,
    };
