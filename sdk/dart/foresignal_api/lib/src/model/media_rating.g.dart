// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'media_rating.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MediaRatingCWProxy {
  MediaRating outletId(String outletId);

  MediaRating provider(MediaRatingProviderEnum provider);

  MediaRating dimension(Object? dimension);

  MediaRating valueNorm(num? valueNorm);

  MediaRating valueRaw(String? valueRaw);

  MediaRating label(String? label);

  MediaRating ratedAt(DateTime? ratedAt);

  MediaRating licence(String licence);

  MediaRating attribution(String attribution);

  MediaRating url(String? url);

  MediaRating egress(MediaRatingEgressEnum egress);

  MediaRating importedAt(DateTime importedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaRating(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaRating(...).copyWith(id: 12, name: "My name")
  /// ```
  MediaRating call({
    String outletId,
    MediaRatingProviderEnum provider,
    Object? dimension,
    num? valueNorm,
    String? valueRaw,
    String? label,
    DateTime? ratedAt,
    String licence,
    String attribution,
    String? url,
    MediaRatingEgressEnum egress,
    DateTime importedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMediaRating.copyWith(...)` or call `instanceOfMediaRating.copyWith.fieldName(value)` for a single field.
class _$MediaRatingCWProxyImpl implements _$MediaRatingCWProxy {
  const _$MediaRatingCWProxyImpl(this._value);

  final MediaRating _value;

  @override
  MediaRating outletId(String outletId) => call(outletId: outletId);

  @override
  MediaRating provider(MediaRatingProviderEnum provider) =>
      call(provider: provider);

  @override
  MediaRating dimension(Object? dimension) => call(dimension: dimension);

  @override
  MediaRating valueNorm(num? valueNorm) => call(valueNorm: valueNorm);

  @override
  MediaRating valueRaw(String? valueRaw) => call(valueRaw: valueRaw);

  @override
  MediaRating label(String? label) => call(label: label);

  @override
  MediaRating ratedAt(DateTime? ratedAt) => call(ratedAt: ratedAt);

  @override
  MediaRating licence(String licence) => call(licence: licence);

  @override
  MediaRating attribution(String attribution) => call(attribution: attribution);

  @override
  MediaRating url(String? url) => call(url: url);

  @override
  MediaRating egress(MediaRatingEgressEnum egress) => call(egress: egress);

  @override
  MediaRating importedAt(DateTime importedAt) => call(importedAt: importedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaRating(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaRating(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MediaRating call({
    Object? outletId = const $CopyWithPlaceholder(),
    Object? provider = const $CopyWithPlaceholder(),
    Object? dimension = const $CopyWithPlaceholder(),
    Object? valueNorm = const $CopyWithPlaceholder(),
    Object? valueRaw = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? ratedAt = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? egress = const $CopyWithPlaceholder(),
    Object? importedAt = const $CopyWithPlaceholder(),
  }) {
    return MediaRating(
      outletId: outletId == const $CopyWithPlaceholder() || outletId == null
          ? _value.outletId
          // ignore: cast_nullable_to_non_nullable
          : outletId as String,
      provider: provider == const $CopyWithPlaceholder() || provider == null
          ? _value.provider
          // ignore: cast_nullable_to_non_nullable
          : provider as MediaRatingProviderEnum,
      dimension: dimension == const $CopyWithPlaceholder()
          ? _value.dimension
          // ignore: cast_nullable_to_non_nullable
          : dimension as Object?,
      valueNorm: valueNorm == const $CopyWithPlaceholder()
          ? _value.valueNorm
          // ignore: cast_nullable_to_non_nullable
          : valueNorm as num?,
      valueRaw: valueRaw == const $CopyWithPlaceholder()
          ? _value.valueRaw
          // ignore: cast_nullable_to_non_nullable
          : valueRaw as String?,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
      ratedAt: ratedAt == const $CopyWithPlaceholder()
          ? _value.ratedAt
          // ignore: cast_nullable_to_non_nullable
          : ratedAt as DateTime?,
      licence: licence == const $CopyWithPlaceholder() || licence == null
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as String,
      attribution:
          attribution == const $CopyWithPlaceholder() || attribution == null
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as String,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      egress: egress == const $CopyWithPlaceholder() || egress == null
          ? _value.egress
          // ignore: cast_nullable_to_non_nullable
          : egress as MediaRatingEgressEnum,
      importedAt:
          importedAt == const $CopyWithPlaceholder() || importedAt == null
          ? _value.importedAt
          // ignore: cast_nullable_to_non_nullable
          : importedAt as DateTime,
    );
  }
}

extension $MediaRatingCopyWith on MediaRating {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMediaRating.copyWith(...)` or `instanceOfMediaRating.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MediaRatingCWProxy get copyWith => _$MediaRatingCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MediaRating _$MediaRatingFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MediaRating', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'outletId',
          'provider',
          'dimension',
          'licence',
          'attribution',
          'egress',
          'importedAt',
        ],
      );
      final val = MediaRating(
        outletId: $checkedConvert('outletId', (v) => v as String),
        provider: $checkedConvert(
          'provider',
          (v) => $enumDecode(_$MediaRatingProviderEnumEnumMap, v),
        ),
        dimension: $checkedConvert('dimension', (v) => v),
        valueNorm: $checkedConvert('valueNorm', (v) => v as num?),
        valueRaw: $checkedConvert('valueRaw', (v) => v as String?),
        label: $checkedConvert('label', (v) => v as String?),
        ratedAt: $checkedConvert(
          'ratedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        licence: $checkedConvert('licence', (v) => v as String),
        attribution: $checkedConvert('attribution', (v) => v as String),
        url: $checkedConvert('url', (v) => v as String?),
        egress: $checkedConvert(
          'egress',
          (v) => $enumDecode(_$MediaRatingEgressEnumEnumMap, v),
        ),
        importedAt: $checkedConvert(
          'importedAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$MediaRatingToJson(MediaRating instance) =>
    <String, dynamic>{
      'outletId': instance.outletId,
      'provider': _$MediaRatingProviderEnumEnumMap[instance.provider]!,
      'dimension': instance.dimension,
      'valueNorm': ?instance.valueNorm,
      'valueRaw': ?instance.valueRaw,
      'label': ?instance.label,
      'ratedAt': ?instance.ratedAt?.toIso8601String(),
      'licence': instance.licence,
      'attribution': instance.attribution,
      'url': ?instance.url,
      'egress': _$MediaRatingEgressEnumEnumMap[instance.egress]!,
      'importedAt': instance.importedAt.toIso8601String(),
    };

const _$MediaRatingProviderEnumEnumMap = {
  MediaRatingProviderEnum.wikidata: 'wikidata',
  MediaRatingProviderEnum.partisanWeb: 'partisan-web',
  MediaRatingProviderEnum.allsides: 'allsides',
  MediaRatingProviderEnum.mbfc: 'mbfc',
  MediaRatingProviderEnum.operator_: 'operator',
};

const _$MediaRatingEgressEnumEnumMap = {
  MediaRatingEgressEnum.OPEN: 'OPEN',
  MediaRatingEgressEnum.LOCAL_ONLY: 'LOCAL_ONLY',
};
