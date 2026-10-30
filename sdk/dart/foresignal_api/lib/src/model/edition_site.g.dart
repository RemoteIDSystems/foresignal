// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edition_site.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EditionSiteCWProxy {
  EditionSite briefingId(String briefingId);

  EditionSite url(String url);

  EditionSite feedKeyUrl(String? feedKeyUrl);

  EditionSite shareUrl(String? shareUrl);

  EditionSite latestRunId(String? latestRunId);

  EditionSite updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EditionSite(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EditionSite(...).copyWith(id: 12, name: "My name")
  /// ```
  EditionSite call({
    String briefingId,
    String url,
    String? feedKeyUrl,
    String? shareUrl,
    String? latestRunId,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEditionSite.copyWith(...)` or call `instanceOfEditionSite.copyWith.fieldName(value)` for a single field.
class _$EditionSiteCWProxyImpl implements _$EditionSiteCWProxy {
  const _$EditionSiteCWProxyImpl(this._value);

  final EditionSite _value;

  @override
  EditionSite briefingId(String briefingId) => call(briefingId: briefingId);

  @override
  EditionSite url(String url) => call(url: url);

  @override
  EditionSite feedKeyUrl(String? feedKeyUrl) => call(feedKeyUrl: feedKeyUrl);

  @override
  EditionSite shareUrl(String? shareUrl) => call(shareUrl: shareUrl);

  @override
  EditionSite latestRunId(String? latestRunId) =>
      call(latestRunId: latestRunId);

  @override
  EditionSite updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EditionSite(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EditionSite(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EditionSite call({
    Object? briefingId = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? feedKeyUrl = const $CopyWithPlaceholder(),
    Object? shareUrl = const $CopyWithPlaceholder(),
    Object? latestRunId = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return EditionSite(
      briefingId:
          briefingId == const $CopyWithPlaceholder() || briefingId == null
          ? _value.briefingId
          // ignore: cast_nullable_to_non_nullable
          : briefingId as String,
      url: url == const $CopyWithPlaceholder() || url == null
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String,
      feedKeyUrl: feedKeyUrl == const $CopyWithPlaceholder()
          ? _value.feedKeyUrl
          // ignore: cast_nullable_to_non_nullable
          : feedKeyUrl as String?,
      shareUrl: shareUrl == const $CopyWithPlaceholder()
          ? _value.shareUrl
          // ignore: cast_nullable_to_non_nullable
          : shareUrl as String?,
      latestRunId: latestRunId == const $CopyWithPlaceholder()
          ? _value.latestRunId
          // ignore: cast_nullable_to_non_nullable
          : latestRunId as String?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $EditionSiteCopyWith on EditionSite {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEditionSite.copyWith(...)` or `instanceOfEditionSite.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EditionSiteCWProxy get copyWith => _$EditionSiteCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EditionSite _$EditionSiteFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EditionSite', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['briefingId', 'url']);
      final val = EditionSite(
        briefingId: $checkedConvert('briefingId', (v) => v as String),
        url: $checkedConvert('url', (v) => v as String),
        feedKeyUrl: $checkedConvert('feedKeyUrl', (v) => v as String?),
        shareUrl: $checkedConvert('shareUrl', (v) => v as String?),
        latestRunId: $checkedConvert('latestRunId', (v) => v as String?),
        updatedAt: $checkedConvert(
          'updatedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EditionSiteToJson(EditionSite instance) =>
    <String, dynamic>{
      'briefingId': instance.briefingId,
      'url': instance.url,
      'feedKeyUrl': ?instance.feedKeyUrl,
      'shareUrl': ?instance.shareUrl,
      'latestRunId': ?instance.latestRunId,
      'updatedAt': ?instance.updatedAt?.toIso8601String(),
    };
