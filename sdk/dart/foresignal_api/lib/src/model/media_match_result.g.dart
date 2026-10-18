// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'media_match_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MediaMatchResultCWProxy {
  MediaMatchResult query(MediaMatchResultQuery query);

  MediaMatchResult maxDistance(int maxDistance);

  MediaMatchResult matches(List<MediaMatch> matches);

  MediaMatchResult referenceWindowStart(DateTime? referenceWindowStart);

  MediaMatchResult caveats(List<Caveat> caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaMatchResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaMatchResult(...).copyWith(id: 12, name: "My name")
  /// ```
  MediaMatchResult call({
    MediaMatchResultQuery query,
    int maxDistance,
    List<MediaMatch> matches,
    DateTime? referenceWindowStart,
    List<Caveat> caveats,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMediaMatchResult.copyWith(...)` or call `instanceOfMediaMatchResult.copyWith.fieldName(value)` for a single field.
class _$MediaMatchResultCWProxyImpl implements _$MediaMatchResultCWProxy {
  const _$MediaMatchResultCWProxyImpl(this._value);

  final MediaMatchResult _value;

  @override
  MediaMatchResult query(MediaMatchResultQuery query) => call(query: query);

  @override
  MediaMatchResult maxDistance(int maxDistance) =>
      call(maxDistance: maxDistance);

  @override
  MediaMatchResult matches(List<MediaMatch> matches) => call(matches: matches);

  @override
  MediaMatchResult referenceWindowStart(DateTime? referenceWindowStart) =>
      call(referenceWindowStart: referenceWindowStart);

  @override
  MediaMatchResult caveats(List<Caveat> caveats) => call(caveats: caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaMatchResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaMatchResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MediaMatchResult call({
    Object? query = const $CopyWithPlaceholder(),
    Object? maxDistance = const $CopyWithPlaceholder(),
    Object? matches = const $CopyWithPlaceholder(),
    Object? referenceWindowStart = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
  }) {
    return MediaMatchResult(
      query: query == const $CopyWithPlaceholder() || query == null
          ? _value.query
          // ignore: cast_nullable_to_non_nullable
          : query as MediaMatchResultQuery,
      maxDistance:
          maxDistance == const $CopyWithPlaceholder() || maxDistance == null
          ? _value.maxDistance
          // ignore: cast_nullable_to_non_nullable
          : maxDistance as int,
      matches: matches == const $CopyWithPlaceholder() || matches == null
          ? _value.matches
          // ignore: cast_nullable_to_non_nullable
          : matches as List<MediaMatch>,
      referenceWindowStart: referenceWindowStart == const $CopyWithPlaceholder()
          ? _value.referenceWindowStart
          // ignore: cast_nullable_to_non_nullable
          : referenceWindowStart as DateTime?,
      caveats: caveats == const $CopyWithPlaceholder() || caveats == null
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>,
    );
  }
}

extension $MediaMatchResultCopyWith on MediaMatchResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMediaMatchResult.copyWith(...)` or `instanceOfMediaMatchResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MediaMatchResultCWProxy get copyWith => _$MediaMatchResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MediaMatchResult _$MediaMatchResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MediaMatchResult', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['query', 'maxDistance', 'matches', 'caveats'],
      );
      final val = MediaMatchResult(
        query: $checkedConvert(
          'query',
          (v) => MediaMatchResultQuery.fromJson(v as Map<String, dynamic>),
        ),
        maxDistance: $checkedConvert('maxDistance', (v) => (v as num).toInt()),
        matches: $checkedConvert(
          'matches',
          (v) => (v as List<dynamic>)
              .map((e) => MediaMatch.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        referenceWindowStart: $checkedConvert(
          'referenceWindowStart',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        caveats: $checkedConvert(
          'caveats',
          (v) => (v as List<dynamic>)
              .map((e) => Caveat.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$MediaMatchResultToJson(MediaMatchResult instance) =>
    <String, dynamic>{
      'query': instance.query.toJson(),
      'maxDistance': instance.maxDistance,
      'matches': instance.matches.map((e) => e.toJson()).toList(),
      'referenceWindowStart': ?instance.referenceWindowStart?.toIso8601String(),
      'caveats': instance.caveats.map((e) => e.toJson()).toList(),
    };
