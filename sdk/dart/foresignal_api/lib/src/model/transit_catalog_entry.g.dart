// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'transit_catalog_entry.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TransitCatalogEntryCWProxy {
  TransitCatalogEntry id(String id);

  TransitCatalogEntry provider(String provider);

  TransitCatalogEntry countryCode(String? countryCode);

  TransitCatalogEntry entityType(String entityType);

  TransitCatalogEntry directDownloadUrl(String directDownloadUrl);

  TransitCatalogEntry licenseUrl(String? licenseUrl);

  TransitCatalogEntry staticReference(String? staticReference);

  TransitCatalogEntry enabled(bool enabled);

  TransitCatalogEntry sourceId(String? sourceId);

  TransitCatalogEntry sourceIds(List<String>? sourceIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TransitCatalogEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TransitCatalogEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  TransitCatalogEntry call({
    String id,
    String provider,
    String? countryCode,
    String entityType,
    String directDownloadUrl,
    String? licenseUrl,
    String? staticReference,
    bool enabled,
    String? sourceId,
    List<String>? sourceIds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTransitCatalogEntry.copyWith(...)` or call `instanceOfTransitCatalogEntry.copyWith.fieldName(value)` for a single field.
class _$TransitCatalogEntryCWProxyImpl implements _$TransitCatalogEntryCWProxy {
  const _$TransitCatalogEntryCWProxyImpl(this._value);

  final TransitCatalogEntry _value;

  @override
  TransitCatalogEntry id(String id) => call(id: id);

  @override
  TransitCatalogEntry provider(String provider) => call(provider: provider);

  @override
  TransitCatalogEntry countryCode(String? countryCode) =>
      call(countryCode: countryCode);

  @override
  TransitCatalogEntry entityType(String entityType) =>
      call(entityType: entityType);

  @override
  TransitCatalogEntry directDownloadUrl(String directDownloadUrl) =>
      call(directDownloadUrl: directDownloadUrl);

  @override
  TransitCatalogEntry licenseUrl(String? licenseUrl) =>
      call(licenseUrl: licenseUrl);

  @override
  TransitCatalogEntry staticReference(String? staticReference) =>
      call(staticReference: staticReference);

  @override
  TransitCatalogEntry enabled(bool enabled) => call(enabled: enabled);

  @override
  TransitCatalogEntry sourceId(String? sourceId) => call(sourceId: sourceId);

  @override
  TransitCatalogEntry sourceIds(List<String>? sourceIds) =>
      call(sourceIds: sourceIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TransitCatalogEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TransitCatalogEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TransitCatalogEntry call({
    Object? id = const $CopyWithPlaceholder(),
    Object? provider = const $CopyWithPlaceholder(),
    Object? countryCode = const $CopyWithPlaceholder(),
    Object? entityType = const $CopyWithPlaceholder(),
    Object? directDownloadUrl = const $CopyWithPlaceholder(),
    Object? licenseUrl = const $CopyWithPlaceholder(),
    Object? staticReference = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? sourceIds = const $CopyWithPlaceholder(),
  }) {
    return TransitCatalogEntry(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      provider: provider == const $CopyWithPlaceholder() || provider == null
          ? _value.provider
          // ignore: cast_nullable_to_non_nullable
          : provider as String,
      countryCode: countryCode == const $CopyWithPlaceholder()
          ? _value.countryCode
          // ignore: cast_nullable_to_non_nullable
          : countryCode as String?,
      entityType:
          entityType == const $CopyWithPlaceholder() || entityType == null
          ? _value.entityType
          // ignore: cast_nullable_to_non_nullable
          : entityType as String,
      directDownloadUrl:
          directDownloadUrl == const $CopyWithPlaceholder() ||
              directDownloadUrl == null
          ? _value.directDownloadUrl
          // ignore: cast_nullable_to_non_nullable
          : directDownloadUrl as String,
      licenseUrl: licenseUrl == const $CopyWithPlaceholder()
          ? _value.licenseUrl
          // ignore: cast_nullable_to_non_nullable
          : licenseUrl as String?,
      staticReference: staticReference == const $CopyWithPlaceholder()
          ? _value.staticReference
          // ignore: cast_nullable_to_non_nullable
          : staticReference as String?,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      sourceId: sourceId == const $CopyWithPlaceholder()
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String?,
      sourceIds: sourceIds == const $CopyWithPlaceholder()
          ? _value.sourceIds
          // ignore: cast_nullable_to_non_nullable
          : sourceIds as List<String>?,
    );
  }
}

extension $TransitCatalogEntryCopyWith on TransitCatalogEntry {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTransitCatalogEntry.copyWith(...)` or `instanceOfTransitCatalogEntry.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TransitCatalogEntryCWProxy get copyWith =>
      _$TransitCatalogEntryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TransitCatalogEntry _$TransitCatalogEntryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TransitCatalogEntry', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'provider',
      'entityType',
      'directDownloadUrl',
      'enabled',
    ],
  );
  final val = TransitCatalogEntry(
    id: $checkedConvert('id', (v) => v as String),
    provider: $checkedConvert('provider', (v) => v as String),
    countryCode: $checkedConvert('countryCode', (v) => v as String?),
    entityType: $checkedConvert('entityType', (v) => v as String),
    directDownloadUrl: $checkedConvert('directDownloadUrl', (v) => v as String),
    licenseUrl: $checkedConvert('licenseUrl', (v) => v as String?),
    staticReference: $checkedConvert('staticReference', (v) => v as String?),
    enabled: $checkedConvert('enabled', (v) => v as bool),
    sourceId: $checkedConvert('sourceId', (v) => v as String?),
    sourceIds: $checkedConvert(
      'sourceIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$TransitCatalogEntryToJson(
  TransitCatalogEntry instance,
) => <String, dynamic>{
  'id': instance.id,
  'provider': instance.provider,
  'countryCode': ?instance.countryCode,
  'entityType': instance.entityType,
  'directDownloadUrl': instance.directDownloadUrl,
  'licenseUrl': ?instance.licenseUrl,
  'staticReference': ?instance.staticReference,
  'enabled': instance.enabled,
  'sourceId': ?instance.sourceId,
  'sourceIds': ?instance.sourceIds,
};
