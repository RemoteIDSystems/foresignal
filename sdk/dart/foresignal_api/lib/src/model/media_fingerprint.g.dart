// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'media_fingerprint.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MediaFingerprintCWProxy {
  MediaFingerprint id(String id);

  MediaFingerprint pdq(String pdq);

  MediaFingerprint quality(int quality);

  MediaFingerprint matchable(bool matchable);

  MediaFingerprint firstSeenAt(DateTime firstSeenAt);

  MediaFingerprint firstPostRef(String firstPostRef);

  MediaFingerprint useCount(int useCount);

  MediaFingerprint platforms(List<String> platforms);

  MediaFingerprint narrativeIds(List<String> narrativeIds);

  MediaFingerprint mediaId(String? mediaId);

  MediaFingerprint reuseFindingIds(List<String>? reuseFindingIds);

  MediaFingerprint uses(List<MediaFingerprintUsesInner>? uses);

  MediaFingerprint narratives(List<ObjectEnvelope>? narratives);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaFingerprint(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaFingerprint(...).copyWith(id: 12, name: "My name")
  /// ```
  MediaFingerprint call({
    String id,
    String pdq,
    int quality,
    bool matchable,
    DateTime firstSeenAt,
    String firstPostRef,
    int useCount,
    List<String> platforms,
    List<String> narrativeIds,
    String? mediaId,
    List<String>? reuseFindingIds,
    List<MediaFingerprintUsesInner>? uses,
    List<ObjectEnvelope>? narratives,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMediaFingerprint.copyWith(...)` or call `instanceOfMediaFingerprint.copyWith.fieldName(value)` for a single field.
class _$MediaFingerprintCWProxyImpl implements _$MediaFingerprintCWProxy {
  const _$MediaFingerprintCWProxyImpl(this._value);

  final MediaFingerprint _value;

  @override
  MediaFingerprint id(String id) => call(id: id);

  @override
  MediaFingerprint pdq(String pdq) => call(pdq: pdq);

  @override
  MediaFingerprint quality(int quality) => call(quality: quality);

  @override
  MediaFingerprint matchable(bool matchable) => call(matchable: matchable);

  @override
  MediaFingerprint firstSeenAt(DateTime firstSeenAt) =>
      call(firstSeenAt: firstSeenAt);

  @override
  MediaFingerprint firstPostRef(String firstPostRef) =>
      call(firstPostRef: firstPostRef);

  @override
  MediaFingerprint useCount(int useCount) => call(useCount: useCount);

  @override
  MediaFingerprint platforms(List<String> platforms) =>
      call(platforms: platforms);

  @override
  MediaFingerprint narrativeIds(List<String> narrativeIds) =>
      call(narrativeIds: narrativeIds);

  @override
  MediaFingerprint mediaId(String? mediaId) => call(mediaId: mediaId);

  @override
  MediaFingerprint reuseFindingIds(List<String>? reuseFindingIds) =>
      call(reuseFindingIds: reuseFindingIds);

  @override
  MediaFingerprint uses(List<MediaFingerprintUsesInner>? uses) =>
      call(uses: uses);

  @override
  MediaFingerprint narratives(List<ObjectEnvelope>? narratives) =>
      call(narratives: narratives);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaFingerprint(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaFingerprint(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MediaFingerprint call({
    Object? id = const $CopyWithPlaceholder(),
    Object? pdq = const $CopyWithPlaceholder(),
    Object? quality = const $CopyWithPlaceholder(),
    Object? matchable = const $CopyWithPlaceholder(),
    Object? firstSeenAt = const $CopyWithPlaceholder(),
    Object? firstPostRef = const $CopyWithPlaceholder(),
    Object? useCount = const $CopyWithPlaceholder(),
    Object? platforms = const $CopyWithPlaceholder(),
    Object? narrativeIds = const $CopyWithPlaceholder(),
    Object? mediaId = const $CopyWithPlaceholder(),
    Object? reuseFindingIds = const $CopyWithPlaceholder(),
    Object? uses = const $CopyWithPlaceholder(),
    Object? narratives = const $CopyWithPlaceholder(),
  }) {
    return MediaFingerprint(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      pdq: pdq == const $CopyWithPlaceholder() || pdq == null
          ? _value.pdq
          // ignore: cast_nullable_to_non_nullable
          : pdq as String,
      quality: quality == const $CopyWithPlaceholder() || quality == null
          ? _value.quality
          // ignore: cast_nullable_to_non_nullable
          : quality as int,
      matchable: matchable == const $CopyWithPlaceholder() || matchable == null
          ? _value.matchable
          // ignore: cast_nullable_to_non_nullable
          : matchable as bool,
      firstSeenAt:
          firstSeenAt == const $CopyWithPlaceholder() || firstSeenAt == null
          ? _value.firstSeenAt
          // ignore: cast_nullable_to_non_nullable
          : firstSeenAt as DateTime,
      firstPostRef:
          firstPostRef == const $CopyWithPlaceholder() || firstPostRef == null
          ? _value.firstPostRef
          // ignore: cast_nullable_to_non_nullable
          : firstPostRef as String,
      useCount: useCount == const $CopyWithPlaceholder() || useCount == null
          ? _value.useCount
          // ignore: cast_nullable_to_non_nullable
          : useCount as int,
      platforms: platforms == const $CopyWithPlaceholder() || platforms == null
          ? _value.platforms
          // ignore: cast_nullable_to_non_nullable
          : platforms as List<String>,
      narrativeIds:
          narrativeIds == const $CopyWithPlaceholder() || narrativeIds == null
          ? _value.narrativeIds
          // ignore: cast_nullable_to_non_nullable
          : narrativeIds as List<String>,
      mediaId: mediaId == const $CopyWithPlaceholder()
          ? _value.mediaId
          // ignore: cast_nullable_to_non_nullable
          : mediaId as String?,
      reuseFindingIds: reuseFindingIds == const $CopyWithPlaceholder()
          ? _value.reuseFindingIds
          // ignore: cast_nullable_to_non_nullable
          : reuseFindingIds as List<String>?,
      uses: uses == const $CopyWithPlaceholder()
          ? _value.uses
          // ignore: cast_nullable_to_non_nullable
          : uses as List<MediaFingerprintUsesInner>?,
      narratives: narratives == const $CopyWithPlaceholder()
          ? _value.narratives
          // ignore: cast_nullable_to_non_nullable
          : narratives as List<ObjectEnvelope>?,
    );
  }
}

extension $MediaFingerprintCopyWith on MediaFingerprint {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMediaFingerprint.copyWith(...)` or `instanceOfMediaFingerprint.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MediaFingerprintCWProxy get copyWith => _$MediaFingerprintCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MediaFingerprint _$MediaFingerprintFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MediaFingerprint', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'pdq',
          'quality',
          'matchable',
          'firstSeenAt',
          'firstPostRef',
          'useCount',
          'platforms',
          'narrativeIds',
        ],
      );
      final val = MediaFingerprint(
        id: $checkedConvert('id', (v) => v as String),
        pdq: $checkedConvert('pdq', (v) => v as String),
        quality: $checkedConvert('quality', (v) => (v as num).toInt()),
        matchable: $checkedConvert('matchable', (v) => v as bool),
        firstSeenAt: $checkedConvert(
          'firstSeenAt',
          (v) => DateTime.parse(v as String),
        ),
        firstPostRef: $checkedConvert('firstPostRef', (v) => v as String),
        useCount: $checkedConvert('useCount', (v) => (v as num).toInt()),
        platforms: $checkedConvert(
          'platforms',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        narrativeIds: $checkedConvert(
          'narrativeIds',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        mediaId: $checkedConvert('mediaId', (v) => v as String?),
        reuseFindingIds: $checkedConvert(
          'reuseFindingIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        uses: $checkedConvert(
          'uses',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => MediaFingerprintUsesInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        narratives: $checkedConvert(
          'narratives',
          (v) => (v as List<dynamic>?)
              ?.map((e) => ObjectEnvelope.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$MediaFingerprintToJson(MediaFingerprint instance) =>
    <String, dynamic>{
      'id': instance.id,
      'pdq': instance.pdq,
      'quality': instance.quality,
      'matchable': instance.matchable,
      'firstSeenAt': instance.firstSeenAt.toIso8601String(),
      'firstPostRef': instance.firstPostRef,
      'useCount': instance.useCount,
      'platforms': instance.platforms,
      'narrativeIds': instance.narrativeIds,
      'mediaId': ?instance.mediaId,
      'reuseFindingIds': ?instance.reuseFindingIds,
      'uses': ?instance.uses?.map((e) => e.toJson()).toList(),
      'narratives': ?instance.narratives?.map((e) => e.toJson()).toList(),
    };
